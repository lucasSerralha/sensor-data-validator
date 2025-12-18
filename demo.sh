#!/bin/bash

# Colors
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}>>> Starting Smart Parking System Demo...${NC}"

# 1. Build (Handled by Docker Compose)
echo -e "${GREEN}>>> Building Services via Docker Compose...${NC}"

# 2. Start Infrastructure
echo -e "${GREEN}>>> Stopping any existing containers...${NC}"
docker-compose down --remove-orphans

# Check and kill any process on port 9092 (Kafka) - REMOVED to avoid killing IDE
# if lsof -i :9092 -t >/dev/null; then
#     echo -e "${GREEN}>>> Killing process on port 9092...${NC}"
#     kill -9 $(lsof -i :9092 -t)
# fi

echo -e "${GREEN}>>> Starting Infrastructure (Kafka, Postgres, Monitoring)...${NC}"
docker-compose up -d --build

# 3. Start Services (using java -jar for simplicity in this script, or docker-compose if images built)
# Ideally, we would build docker images here. For the 10-minute demo requirement, running local jars is faster if docker build is slow.
# BUT the requirement asked for "Subir microserviços em containers".
# So let's assume we use the root docker-compose if we add the services there, OR we run the individual docker-composes.
# Given the complexity, let's stick to the root docker-compose for INFRA and run apps locally OR add apps to root compose.
# The user asked for "Subir microserviços em containers".
# Let's add the apps to the root docker-compose for the demo script to be "one click".

# Wait for Infra
echo -e "${GREEN}>>> Waiting for Infrastructure...${NC}"
sleep 30

# 4. Open Dashboard
# 4. Open Monitoring Terminals
echo -e "${GREEN}>>> Opening Monitoring Terminals...${NC}"
./monitor.sh &

# 5. Trigger Simulation
echo -e "${GREEN}>>> Triggering Simulation (Sensor A1)...${NC}"
curl -X POST "http://localhost:8081/api/simulation/trigger?id=A1&time=600"

echo -e "${GREEN}>>> Demo Running! Check http://localhost:3000 for Grafana (admin/admin)${NC}"
