#!/bin/bash

# Requires: curl, jq
# First run $ chmod +x scripts/smoke_tests.sh
# Then start the application and docker-compose and run: $ scripts/smoke_tests.sh

response=$(curl -s http://localhost:8090/payment --request "POST" \
   --header "Content-Type: application/json" \
   --data '{
       "card_number": "2222405343248877",
       "expiry_month":4,
       "expiry_year":2025,
       "currency":"GBP",
       "amount":100,
       "cvv":123
      }')
id=$(echo "$response" | jq -r '.id')
status=$(echo "$response" | jq -r '.status')
if [[ -z "$response" || "$status" != "Authorized" ]]; then
  echo -e "Error: first transaction failed $response"
  exit 1
fi


response=$(curl -s http://localhost:8090/payment/$id)
if [[ -z "$response" || "$response" == "null" ]]; then
  echo -e "Error: Could not retrieve first payment"
  exit 1
fi

response=$(curl -s http://localhost:8090/payment --request "POST" \
  --header "Content-Type: application/json" \
  --data '{
      "card_number": "2222405343248112",
      "expiry_month":1,
      "expiry_year":2026,
      "currency":"USD",
      "amount":60000,
      "cvv":456
    }')

id=$(echo "$response" | jq -r '.id')
status=$(echo "$response" | jq -r '.status')
if [[ -z "$response" || "$status" != "Declined" ]]; then
  echo -e "Error: second transaction failed $response"
  exit 1
fi

response=$(curl -s http://localhost:8090/payment/$id)
if [[ -z "$response" || "$response" == "null" ]]; then
  echo -e "Error: Could not retrieve second payment"
  exit 1
fi

echo "Test successful"