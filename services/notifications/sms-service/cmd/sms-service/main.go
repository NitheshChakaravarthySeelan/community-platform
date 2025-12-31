package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/google/uuid"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/segmentio/kafka-go"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/metric"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.24.0"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

var (
	serviceName    = os.Getenv("SERVICE_NAME")
	servicePort    = os.Getenv("PORT")
	otelExporter   = os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
	kafkaBrokerURL = os.Getenv("KAFKA_BROKER_URL")
	kafkaTopic     = os.Getenv("KAFKA_TOPIC")
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
	meterProvider := sdkmetric.NewMeterProvider()
	otel.SetMeterProvider(meterProvider)
	log.Printf("OpenTelemetry MeterProvider initialized.")
	return meterProvider, nil
}

// startKafkaConsumer connects to Kafka and starts consuming messages from the notifications topic.
func startKafkaConsumer() {
	if kafkaBrokerURL == "" || kafkaTopic == "" {
		log.Println("KAFKA_BROKER_URL or KAFKA_TOPIC not set. Kafka consumer will not start.")
		return
	}

	log.Printf("Starting Kafka consumer for topic '%s' on broker '%s'", kafkaTopic, kafkaBrokerURL)

	r := kafka.NewReader(kafka.ReaderConfig{
		Brokers:        []string{kafkaBrokerURL},
		Topic:          kafkaTopic,
		GroupID:        serviceName, // Use the service name as the consumer group ID
		MinBytes:       10e3,        // 10KB
		MaxBytes:       10e6,        // 10MB
		CommitInterval: time.Second, // Flush commits to Kafka every second
	})

	ctx := context.Background()
	for {
		m, err := r.ReadMessage(ctx)
		if err != nil {
			log.Printf("Error reading from Kafka: %v", err)
			time.Sleep(5 * time.Second) // Wait before retrying
			continue
		}
		// As per the LLD, just log the received notification request
		log.Printf("[%s] KAFKA: Received notification request on partition %d at offset %d: %s", serviceName, m.Partition, m.Offset, string(m.Value))
	}
}

type SendSmsRequest struct {
	To      string `json:"to"`
	Body    string `json:"body"`
}

type SendSmsResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
	SmsID   string `json:"smsId,omitempty"`
}

func main() {
	// Set defaults for environment variables
	if serviceName == "" {
		serviceName = "sms-service"
	}
	if servicePort == "" {
		servicePort = "8080"
	}
	if otelExporter == "" {
		otelExporter = "localhost:4317"
	}
	if kafkaBrokerURL == "" {
		kafkaBrokerURL = "localhost:9092"
	}
	if kafkaTopic == "" {
		kafkaTopic = "notifications"
	}

	ctx := context.Background()

	tracerProvider, err := initTracerProvider(ctx)
	if err != nil {
		log.Fatalf("failed to initialize TracerProvider: %v", err)
	}
	defer func() {
		if err := tracerProvider.Shutdown(ctx); err != nil {
			log.Printf("Error shutting down TracerProvider: %v", err)
		}
	}()

	meterProvider, err := initMeterProvider(ctx)
	if err != nil {
		log.Fatalf("failed to initialize MeterProvider: %v", err)
	}
	defer func() {
		if err := meterProvider.Shutdown(ctx); err != nil {
			log.Printf("Error shutting down MeterProvider: %v", err)
		}
	}()

	// Start the Kafka consumer in a separate goroutine
	go startKafkaConsumer()

	tracer := otel.Tracer(serviceName + "-tracer")
	meter := otel.Meter(serviceName + "_meter")
	requestCounter, err := meter.Int64Counter(serviceName + "_requests_total", metric.WithDescription("Total number of notification requests"))
	if err != nil {
		log.Fatalf("failed to create request counter: %v", err)
	}

	// --- Setup HTTP Handlers ---
	sendSmsHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ctx, span := tracer.Start(r.Context(), "handleSendRequest")
		defer span.End()

		span.SetAttributes(attribute.String("endpoint", "/send"))
		requestCounter.Add(ctx, 1, metric.WithAttributes(attribute.String("endpoint", "/send")))

		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}

		var req SendSmsRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			span.RecordError(err)
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		log.Printf("[%s] HTTP: Received request to send SMS to %s", serviceName, req.To)
		smsID := uuid.New().String()

		resp := SendSmsResponse{
			Success: true,
			Message: fmt.Sprintf("SMS to %s accepted for sending (simulated)", req.To),
			SmsID:   smsID,
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(resp)
	})
	http.Handle("/send", sendSmsHandler)

	http.Handle("/health", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, span := tracer.Start(r.Context(), "handleHealthCheck")
		defer span.End()
		span.SetAttributes(attribute.String("endpoint", "/health"))
		fmt.Fprintf(w, "OK")
	}))

	http.Handle("/metrics", promhttp.Handler())

	fmt.Printf("Server starting on port %s...\n", servicePort)
	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%s", servicePort), nil))
}
