package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"

	"github.com/google/uuid"
	"github.com/prometheus/client_golang/prometheus/promhttp"

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

type SendPushRequest struct {
	DeviceID string `json:"deviceId"`
	Title    string `json:"title"`
	Body     string `json:"body"`
}

type SendPushResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
	PushID  string `json:"pushId,omitempty"`
}

func main() {
	if serviceName == "" {
		serviceName = "push-service" // Changed service name
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
	tracer := otel.Tracer("push-service-tracer") // Changed tracer name
	meter := otel.Meter("push-service-meter")   // Changed meter name
	requestCounter, err := meter.Int64Counter("push_requests_total", metric.WithDescription("Total number of push notification requests")) // Changed metric name
	if err != nil {
		log.Fatalf("failed to create request counter: %v", err)
	}

	// Wrap HTTP handlers with OpenTelemetry middleware
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		_, span := tracer.Start(r.Context(), "handleHelloRequest")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/"))
		requestCounter.Add(r.Context(), 1, metric.WithAttributes(attribute.String("endpoint", "/")))

		fmt.Fprintf(w, "Hello from Push Service!")
	})

	http.HandleFunc("/send-push", func(w http.ResponseWriter, r *http.Request) { // Changed endpoint to /send-push
		ctx, span := tracer.Start(r.Context(), "handleSendPushRequest") // Changed span name
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/send-push"))
		requestCounter.Add(ctx, 1, metric.WithAttributes(attribute.String("endpoint", "/send-push")))

		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}

		var req SendPushRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			span.RecordError(err)
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		// --- Placeholder for actual push notification sending logic ---
		// In a real application, you would integrate with an actual push notification provider
		// (e.g., FCM, APNS) here.
		// For now, we just simulate success.
		log.Printf("Simulating sending push notification to DeviceID %s with title '%s'", req.DeviceID, req.Title)
		pushID := uuid.New().String() // Generate a dummy push ID
		// --- End Placeholder ---

		resp := SendPushResponse{
			Success: true,
			Message: fmt.Sprintf("Push notification to DeviceID %s sent successfully (simulated)", req.DeviceID),
			PushID:  pushID,
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(resp)
	})

	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		_, span := tracer.Start(r.Context(), "handleHealthCheck")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/health"))
		fmt.Fprintf(w, "OK")
	})

	// Prometheus metrics endpoint
	http.Handle("/metrics", promhttp.Handler())

	fmt.Printf("Server starting on port %s...\n", servicePort)
	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%s", servicePort), nil))
}