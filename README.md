# OnCall Agent Demo App

A deliberately buggy Java Spring Boot application for demonstrating an **on-call agent workflow**:

```
Alert (New Relic) → Agent reads exception/logs → Understands issue → Raises PR → Creates ServiceNow ticket
```

## The Bug

`DemoApplication.java` has a `NullPointerException` in the order processing flow. When a user with a **null shipping address** (Bob Martinez, ID `1002`) places an order, `formatShippingLabel()` calls `.toUpperCase()` on a null address field.

**Stack trace produced:**
```
java.lang.NullPointerException: Cannot invoke "String.toUpperCase()" because "user.address" is null
  at com.demo.oncall.DemoApplication.formatShippingLabel(DemoApplication.java:...)
  at com.demo.oncall.DemoApplication.processOrder(DemoApplication.java:...)
```

## Quick Start

### Run Locally (no New Relic)

```bash
./mvnw spring-boot:run
# Open http://localhost:8085
# Select "Bob Martinez" → click "Process Order" → triggers NPE
```

### Run with New Relic

```bash
# 1. Set your New Relic license key
export NEW_RELIC_LICENSE_KEY="your-key-here"

# 2. Run with Docker
docker-compose up --build

# 3. Trigger the bug
bash scripts/trigger-npe.sh http://localhost:8085 5
```

### Deploy New Relic Alerts (Terraform)

```bash
cd terraform
terraform init
terraform apply \
  -var="newrelic_account_id=YOUR_ACCOUNT_ID" \
  -var="newrelic_api_key=YOUR_API_KEY" \
  -var="notification_email=oncall@yourteam.com"
```

## Demo Flow

1. **App runs** on port 8085 with New Relic APM agent attached
2. **Trigger the bug** via UI or `scripts/trigger-npe.sh`
3. **New Relic captures** the NullPointerException with full stack trace and custom attributes
4. **Alert fires** based on the NRQL condition in `terraform/newrelic-alerts.tf`
5. **On-call agent receives** the alert and:
   - Reads the exception details from New Relic API
   - Reads application logs
   - Identifies the root cause: null address in `formatShippingLabel()`
   - Creates a fix (adds null-check guard)
   - Raises a Pull Request
   - Creates a ServiceNow incident ticket

## Project Structure

```
├── src/main/java/com/demo/oncall/
│   └── DemoApplication.java        # Single Java file — the app + the bug
├── src/main/resources/
│   ├── application.properties       # App config (port 8085)
│   └── logback-spring.xml           # Logging config (console + file)
├── newrelic.yml                     # New Relic Java agent configuration
├── terraform/
│   └── newrelic-alerts.tf           # Alert policy, NRQL conditions, workflow
├── scripts/
│   └── trigger-npe.sh              # Script to trigger the NPE via curl
├── Dockerfile                       # Multi-stage build with NR agent
├── docker-compose.yml               # One-command deployment
└── pom.xml                          # Maven build (Spring Boot 3.2 + NR API)
```
