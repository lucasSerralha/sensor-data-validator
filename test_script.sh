#!/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${GREEN}>>> Starting Post-Demo Test Script...${NC}"

# API Endpoints
PAYMENT_API="http://localhost:8080/api/payments/pay"
SIMULATION_API="http://localhost:8081/api/simulation/trigger"

# Function to trigger simulation
trigger_simulation() {
    local spot=$1
    local time=$2
    echo -e "${BLUE}>>> Triggering simulation for $spot ($time seconds)...${NC}"
    curl -X POST "$SIMULATION_API?id=$spot&time=$time"
    echo ""
}

# Function to make payment
make_payment() {
    local plate=$1
    local spot=$2
    local amount=$3
    echo -e "${BLUE}>>> Making payment for $spot (Plate: $plate, Amount: $amount)...${NC}"
    curl -X POST "$PAYMENT_API?plate=$plate&parkingSpot=$spot&amount=$amount"
    echo ""
}

# 1. Pay for A1 for 1 minute (assuming A1 was triggered by demo.sh)
echo -e "${GREEN}>>> Step 1: Paying for A1...${NC}"
make_payment "AA-01-AA" "A1" "0.20"

# 2. Generate 5 new sessions of 5 minutes (300s) for A2, A3, A4, A5, A6
echo -e "${GREEN}>>> Step 2: Starting sessions for A2-A6...${NC}"
trigger_simulation "A2" 300
trigger_simulation "A3" 300
trigger_simulation "A4" 300
trigger_simulation "A5" 300
trigger_simulation "A6" 300

# 3. Pay for them with different times
echo -e "${GREEN}>>> Step 3: Processing payments...${NC}"

# A2: Pay for 1 minute
make_payment "AA-02-BB" "A2" "0.10"

# A3: Pay for 5 minutes
make_payment "AA-03-CC" "A3" "0.50"

# A4: Wait 1 minute, then pay for 4 minutes
echo -e "${BLUE}>>> Waiting 55 seconds before paying for A4...${NC}"
sleep 55
make_payment "AA-04-DD" "A4" "0.40"

# A5, A6: No payment (Testing alerts/violations)
echo -e "${BLUE}>>> A5 and A6 will NOT be paid (expecting alerts)...${NC}"

echo -e "${GREEN}>>> Test Script Completed!${NC}"
