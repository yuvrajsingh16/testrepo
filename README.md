# OnCall Agent Demo App

A deliberately buggy Java Spring Boot application for demonstrating an **on-call agent workflow**:

```
Alert (New Relic)  Agent reads exception/logs  Understands issue  Raises PR  Creates ServiceNow ticket
```

## Current Behavior (Order Processing)

- The order processing flow validates the user profile.
- If a user has a **null/blank shipping address** (e.g., Bob Martinez, ID `1002`), the application returns a **controlled error response**.
- The error is captured via `NewRelic.noticeError(...)` with contextual attributes.

## Historical Bug (Now Fixed)

Previously, `DemoApplication.java` could throw a `NullPointerException` when `formatShippingLabel()` called `.toUpperCase()` on a null address field.

- Root cause (historical): `user.address.toUpperCase(...)` executed when `user.address == null`.
- Fix: add null/blank validation and throw a controlled `IllegalStateException` (handled by `processOrder(...)`).

## Quick Start

### Run Locally (no New Relic)

```bash
./mvnw spring-boot:run
# Open http://localhost:8085
# Trigger error path: select "Bob Martinez"  click "Process Order"  returns controlled error payload
```

### Run with New Relic

```bash
# 1. Set your New Relic license key
export NEW_RELIC_LICENSE_KEY="your-key-here"

# 2. Run with Docker
docker-compose up --build

# 3. Trigger the error path
bash scripts/trigger-npe.sh http://localhost:8085 5
```

> Note: The script name is retained for backwards compatibility with the demo workflow, even though the app
> no longer triggers an uncaught NPE in the default branch.

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
2. Trigger the error path via UI or `scripts/trigger-npe.sh`
3. New Relic captures the handled exception via `NewRelic.noticeError(...)`
4. Alert fires based on the NRQL condition in `terraform/newrelic-alerts.tf`
5. On-call agent receives the alert and:
   - Reads exception details / custom attributes
   - Reads application logs
   - Identifies the root cause (missing address validation scenario)
   - Raises a Pull Request (if needed)
   - Creates a ServiceNow incident ticket

## Project Structure

```
 src/main/java/com/demo/oncall/
    DemoApplication.java        # Single Java file  order processing behavior
 src/main/resources/
    application.properties       # App config (port 8085)
    logback-spring.xml           # Logging config (console + file)
 newrelic.yml                     # New Relic Java agent configuration
 terraform/
    newrelic-alerts.tf           # Alert policy, NRQL conditions, workflow
 scripts/
    trigger-npe.sh              # Script to trigger the error path via curl
 Dockerfile                       # Multi-stage build with NR agent
 docker-compose.yml               # One-command deployment
 pom.xml                          # Maven build (Spring Boot 3.2 + NR API)
```
