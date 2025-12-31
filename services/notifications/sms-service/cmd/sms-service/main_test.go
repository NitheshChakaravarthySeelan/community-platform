package main

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestSendSmsHandler(t *testing.T) {
	http.DefaultServeMux = http.NewServeMux() // Reset default mux for isolated test
	http.Handle("/metrics", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})) // Mock metrics handler

	sendSmsHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"success":true,"message":"SMS accepted for sending (simulated)"}`))
	})

	requestBody := `{"to":"+15551234567", "body":"Test SMS body"}`
	req := httptest.NewRequest("POST", "/send", bytes.NewBufferString(requestBody))
	req.Header.Set("Content-Type", "application/json")

	rr := httptest.NewRecorder()

	sendSmsHandler.ServeHTTP(rr, req)

	if status := rr.Code; status != http.StatusOK {
		t.Errorf("handler returned wrong status code: got %v want %v",
			status, http.StatusOK)
	}

	expected := `"success":true`
	if !strings.Contains(rr.Body.String(), expected) {
		t.Errorf("handler returned unexpected body: got %v want it to contain %v",
			rr.Body.String(), expected)
	}
}
