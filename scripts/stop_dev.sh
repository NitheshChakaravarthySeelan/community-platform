#!/bin/bash

# CheckoutX Stop Script
# Stops all services started by start_dev.sh

LOG_DIR=".logs"

if [ ! -d "$LOG_DIR" ]; then
    echo "❌ No .logs directory found. Are the services running?"
    exit 1
fi

echo "🛑 Stopping CheckoutX Services..."

for pid_file in $LOG_DIR/*.pid; do
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        name=$(basename "$pid_file" .pid)
        echo "  -> Stopping $name (PID: $pid)..."
        kill $pid 2>/dev/null
        rm "$pid_file"
    fi
done

# Cleanup remaining processes just in case
pkill -f "mvn spring-boot:run" 2>/dev/null
pkill -f "uvicorn checkout_orchestrator" 2>/dev/null
pkill -f "next-server" 2>/dev/null

echo "✅ All services stopped."
