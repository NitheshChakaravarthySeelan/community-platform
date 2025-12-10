package main

import (

	"context"

	"fmt"

	"log"

	"net/http"

	"os"

	"encoding/json" // Ensure json is explicitly imported

	"github.com/google/uuid" // Add uuid for generating unique IDs

	"github.com/prometheus/client_golang/prometheus/promhttp" // Add promhttp for metrics endpoint



	"go.opentelemetry.io/otel"

	"go.opentelemetry.io/otel/attribute"

	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"

	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"

	"go.opentelemetry.io/otel/propagation"

	"go.opentelemetry.io/otel/sdk/resource"

	sdktrace "go.opentelemetry.io/otel/sdk/trace"

	semconv "go.opentelemetry.io/otel/semconv/v1.24.0"



	"google.golang.org/grpc"

	"google.golang.org/grpc/credentials/insecure"



	"go.opentelemetry.io/otel/metric"

	sdkmetric "go.opentelemetry.io/otel/sdk/metric"

)

var (
	serviceName  = os.Getenv("SERVICE_NAME")
	servicePort  = os.Getenv("PORT")
	otelExporter = os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
)

// initTracerProvider initializes an OTLP trace exporter and sets up the SDK's TracerProvider.
func initTracerProvider(ctx context.Context) (*sdktrace.TracerProvider, error) {
	if otelExporter == "" {
		log.Println("OTEL_EXPORTER_OTLP_ENDPOINT is not set, using no-op TracerProvider.")
		return sdktrace.NewTracerProvider(), nil
	}

	conn, err := grpc.DialContext(ctx, otelExporter,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithBlock(),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to create gRPC connection to OTLP collector: %w", err)
	}

	traceClient := otlptracegrpc.NewClient(otlptracegrpc.WithGRPCConn(conn))
	traceExporter, err := otlptrace.New(ctx, traceClient)
	if err != nil {
		return nil, fmt.Errorf("failed to create OTLP trace exporter: %w", err)
	}

	res, err := resource.New(ctx,
		resource.WithAttributes(
			semconv.ServiceNameKey.String(serviceName),
			attribute.String("environment", "development"),
		),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to create resource: %w", err)
	}

	bsp := sdktrace.NewBatchSpanProcessor(traceExporter)
	tracerProvider := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithResource(res),
		sdktrace.WithSpanProcessor(bsp),
	)
	otel.SetTracerProvider(tracerProvider)
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(propagation.TraceContext{}, propagation.Baggage{}))

	log.Printf("OpenTelemetry TracerProvider initialized, exporting to %s", otelExporter)
	return tracerProvider, nil
}

// initMeterProvider initializes an OTLP metric exporter and sets up the SDK's MeterProvider.
func initMeterProvider(ctx context.Context) (*sdkmetric.MeterProvider, error) {
	if otelExporter == "" {
		log.Println("OTEL_EXPORTER_OTLP_ENDPOINT is not set, using no-op MeterProvider.")
		return sdkmetric.NewMeterProvider(), nil
	}

	// You would typically use a push exporter for metrics, e.g., OTLP.
	// For this example, we'll just use a simple MeterProvider setup.
	meterProvider := sdkmetric.NewMeterProvider()
	otel.SetMeterProvider(meterProvider)

	log.Printf("OpenTelemetry MeterProvider initialized.")
	return meterProvider, nil
}

type SendEmailRequest struct {
	To      string `json:"to"`
	Subject string `json:"subject"`
	Body    string `json:"body"`
}

type SendEmailResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
	EmailID string `json:"emailId,omitempty"`
}

func main() {
	if serviceName == "" {
		serviceName = "email-service"
	}
	if servicePort == "" {
		servicePort = "8080"
	}
	if otelExporter == "" {
		otelExporter = "localhost:4317" // Default OTLP gRPC collector endpoint
	}

	// Main context for graceful shutdown
	ctx := context.Background() 
	
	// Initialize OpenTelemetry TracerProvider
	tracerProvider, err := initTracerProvider(ctx)
	if err != nil {
		log.Fatalf("failed to initialize TracerProvider: %v", err)
	}
	defer func() {
		if err := tracerProvider.Shutdown(ctx); err != nil {
			log.Printf("Error shutting down TracerProvider: %v", err)
		}
	}()

	// Initialize OpenTelemetry MeterProvider
	meterProvider, err := initMeterProvider(ctx)
	if err != nil {
		log.Fatalf("failed to initialize MeterProvider: %v", err)
	}
	defer func() {
		if err := meterProvider.Shutdown(ctx); err != nil {
			log.Printf("Error shutting down MeterProvider: %v", err)
		}
	}()

	// Get a Tracer and Meter instance
	tracer := otel.Tracer("email-service-tracer")
	meter := otel.Meter("email-service-meter")
	requestCounter, err := meter.Int64Counter("email_requests_total", metric.WithDescription("Total number of email requests"))
	if err != nil {
		log.Fatalf("failed to create request counter: %v", err)
	}

	// Wrap HTTP handlers with OpenTelemetry middleware
	helloHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, span := tracer.Start(r.Context(), "handleHelloRequest")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/"))
		requestCounter.Add(r.Context(), 1, metric.WithAttributes(attribute.String("endpoint", "/")))

		fmt.Fprintf(w, "Hello from Email Service!")
	})
	http.Handle("/", helloHandler) // Use standard http.Handle

	sendEmailHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ctx, span := tracer.Start(r.Context(), "handleSendEmailRequest")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/send-email"))
		requestCounter.Add(ctx, 1, metric.WithAttributes(attribute.String("endpoint", "/send-email")))

		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}

		var req SendEmailRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			span.RecordError(err)
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		// --- Placeholder for actual email sending logic ---
		// In a real application, you would integrate with an actual email provider
		// (e.g., SendGrid, Mailgun, AWS SES) here.
		// For now, we just simulate success.
		log.Printf("Simulating sending email to %s with subject '%s'", req.To, req.Subject)
		emailID := uuid.New().String() // Generate a dummy email ID
		// --- End Placeholder ---

		resp := SendEmailResponse{
			Success: true,
			Message: fmt.Sprintf("Email to %s sent successfully (simulated)", req.To),
			EmailID: emailID,
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(resp)
	})
	http.Handle("/send-email", sendEmailHandler) // Use standard http.Handle

	healthHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, span := tracer.Start(r.Context(), "handleHealthCheck")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/health"))
		fmt.Fprintf(w, "OK")
	})
	http.Handle("/health", healthHandler) // Use standard http.Handle

	// Prometheus metrics endpoint
	http.Handle("/metrics", promhttp.Handler())

	fmt.Printf("Server starting on port %s...\n", servicePort)
	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%s", servicePort), nil))
}