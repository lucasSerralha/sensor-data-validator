dock#!/bin/bash

# Colors
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}>>> Starting Smart Parking System Demo...${NC}"

# 1. Build (Handled by Docker Compose)
echo -e "${GREEN}>>> Building Services via Docker Compose...${NC}"

# 2. Start Infrastructure
echo -e "${GREEN}>>> Stopping any existing containers...${NC}"
docker-compose down --remove-orphans


echo -e "${GREEN}>>> Starting Infrastructure (Kafka, Postgres, Monitoring)...${NC}"
docker-compose up -d --build
# Wait for Infra
echo -e "${GREEN}>>> Waiting for Infrastructure...${NC}"
sleep 30

# 4. Open Dashboard
# 4. Open Monitoring Terminals
echo -e "${GREEN}>>> Opening Monitoring Terminals...${NC}"
./monitor.sh &

# 5. Trigger Simulation
echo -e "${GREEN}>>> Triggering Simulation (Sensor A1)...${NC}"
curl -X POST "http://localhost:8081/api/simulation/trigger?id=A1&time=180"

echo -e "${GREEN}>>> Demo Running! Check http://localhost:3000 for Grafana (admin/admin)${NC}"
