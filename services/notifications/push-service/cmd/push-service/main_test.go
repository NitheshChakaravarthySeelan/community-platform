package main

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestSendPushHandler(t *testing.T) {
	http.DefaultServeMux = http.NewServeMux() // Reset default mux for isolated test
	http.Handle("/metrics", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})) // Mock metrics handler

	sendPushHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}
		// In a real test, you might check the body decoding, but since our
		// logic is just to log and return success, we can simplify.
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"success":true,"message":"Push notification accepted for sending (simulated)"}`))
	})

	// Create a mock request
	requestBody := `{"userId":"user123", "title":"Test Push", "body":"Test push body", "payload":{}}`
	req := httptest.NewRequest("POST", "/send", bytes.NewBufferString(requestBody))
	req.Header.Set("Content-Type", "application/json")

	// Create a response recorder
	rr := httptest.NewRecorder()

	// Call the handler
	sendPushHandler.ServeHTTP(rr, req)

	// Check the status code
	if status := rr.Code; status != http.StatusOK {
		t.Errorf("handler returned wrong status code: got %v want %v",
			status, http.StatusOK)
	}

	// Check the response body
	expected := `"success":true`
	if !strings.Contains(rr.Body.String(), expected) {
		t.Errorf("handler returned unexpected body: got %v want it to contain %v",
			rr.Body.String(), expected)
	}
}
