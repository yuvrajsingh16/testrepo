#!/usr/bin/env bash
# Triggers the NullPointerException by submitting an order for Bob Martinez (userId=1002)
# whose shipping address is null in the database.

BASE_URL="${1:-http://localhost:8085}"
REPEAT="${2:-1}"

echo "=== OnCall Demo: Triggering NullPointerException ==="
echo "Target: $BASE_URL"
echo "Repeat: $REPEAT time(s)"
echo ""

for i in $(seq 1 "$REPEAT"); do
  echo "--- Request $i/$REPEAT ---"
  curl -s -X POST "$BASE_URL/api/process-order" \
    -d "userId=1002&product=Laptop+Pro+16&quantity=1" \
    -H "Content-Type: application/x-www-form-urlencoded" | python3 -m json.tool 2>/dev/null || echo "(raw response above)"
  echo ""
  sleep 1
done

echo "=== Done. Check New Relic for the alert. ==="
