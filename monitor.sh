#!/bin/bash

# Function to open a new terminal tab/window
open_terminal() {
    TITLE=$1
    CMD=$2
    
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --tab --title="$TITLE" -- bash -c "$CMD; exec bash"
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -e bash -c "$CMD; exec bash" &
    elif command -v xfce4-terminal &> /dev/null; then
        xfce4-terminal --tab --title="$TITLE" --command="bash -c '$CMD; exec bash'"
    else
        echo "No suitable terminal emulator found (gnome-terminal, konsole, xfce4-terminal)."
        echo "Running command in background: $TITLE"
        $CMD &
    fi
}

echo ">>> Opening Monitoring Terminals..."

# 1. Kafka Topics (Use Kafka UI at http://localhost:8090)
# open_terminal "Kafka: Parking Events" "docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic parking-events"
# open_terminal "Kafka: Payment Events" "docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment-events"
# open_terminal "Kafka: Session Updates" "docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic session.updates"
# open_terminal "Kafka: Alerts" "docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic alert.incident"

# 2. Service Logs
open_terminal "Logs: Parking Controller" "docker logs -f parking-controller"
open_terminal "Logs: API Gateway" "docker logs -f driver-api-gateway"
open_terminal "Logs: Alert Generator" "docker logs -f alert-generator"

# 3. Database View
open_terminal "DB: Parking Sessions" "watch -n 2 \"docker exec -it postgres psql -U admin -d smart_parking -c 'SELECT id, plate, sensor_id, status, start_time, end_time, amount, alerted FROM parking_sessions ORDER BY start_time DESC;'\""

echo ">>> Monitoring started."
