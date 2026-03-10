## Summary
SAFED (**S**ecurity **A**ssessment for **Fed**erations) is a security testing tool that checks how securely applications handle federated logins via OIDC, OAuth, and SAML. 
It works by intercepting and manipulating federation requests to simulate attacks, then comparing results against a baseline.

## Target Audience
* You are running an IDP (Identity Provider) and want to make sure that all connected applications are federated securely!
* You are developing or using an application and want to make sure that the application can be connected securely to an IDP (e.g. execute it one time or continuously in a CI/CD Pipeline). 
* Maybe more use cases ?!

## How it works
### High level Flow
1. **Baseline Gathering**: Execute 2 successful federation requests to establish normal behavior
2. **Checking Baseline**: Validates that baseline is successful and control test fails
3. **Scanner Execution**: Run all attack scanners that manipulate federation requests
4. **Comparison & Verification**: Compare scanner results against baseline using verification strategies
5. **Report Generation**: Classify results as "VULNERABLE" (attack succeeded) or "OK" (attack blocked)

The assessments are started by CommandLineRunner `src/main/java/de/denniskniep/safed/SafedCli.java`. 
The class `src/main/java/de/denniskniep/safed/common/assessment/Assessment.java` is responsible to orchestrates the assessment.

### Federation Automation (OIDC / SAML)
Selenium-based Chrome automation with BiDi network interception (`src/main/java/de/denniskniep/safed/common/auth/browser/BrowserAuthenticationFlow.java`)
- Uses headless Chrome with isolated profile per run
- BiDi protocol captures network requests/responses
- Phase 1 (Initialize): Captures federation request from IdP
- Phase 2 (Answer): Submits manipulated federation response
- Handle Backchannel requests if required (see `OidcController`)

### Scanner
The scanners allow pluggable manipulation of federation configs, requests, responses, etc.

Scanners can be found here:
- SAML Scanners: `src/main/java/de/denniskniep/safed/saml/scans/scanner/`
- OIDC Scanners: `src/main/java/de/denniskniep/safed/oidc/scans/scanner/`
- Mtls Scanners: `src/main/java/de/denniskniep/safed/mtls/scans/scanner`


### Verification Strategies
Multiple verification strategies determine if an attack succeeded, by comparing baseline results with scan results (`src/main/java/de/denniskniep/safed/common/verifications`)
- `DiffVerification` - Compares visible text differences between baseline and scan
- `UrlAndStatusCodeVerification` - Compares final URL and HTTP status code

## Webhook
The webhook mechanism allows SAFED to automatically send assessment reports to a configured HTTP endpoint.

### Webhook Configuration
Configure the webhook in your `application.yaml` or environment variables:

#### application.yaml
```yaml
webhook:
  enabled: true
  url: "https://your-webhook-endpoint.com/api/reports"
  authHeaderName: "Authorization"
  authHeaderValue: "Bearer <api-key>"
```

#### Environment Variables

```bash
WEBHOOK_ENABLED=true
WEBHOOK_URL=https://your-webhook-endpoint.com/api/reports
WEBHOOK_AUTHHEADERNAME="Authorization"
WEBHOOK_AUTHHEADERVALUE="Bearer <api-key>"
```

### Webhook Behavior

When enabled, the webhook will:
1. Send a POST request to the configured URL after all assessments are completed
2. Include all reports as JSON in the request body (Content-Type: application/json)
3. Add the authentication header if configured

The webhook sends a JSON array of `Report` objects

### Webhook Example Payload

```json
[
  {
    "clientId": "example-client",
    "durationInMs": 1234,
    "status": "SUCCESS",
    "errors": [],
    "firstScan": { ... },
    "secondScan": { ... },
    "isVulnerableTestScan": { ... },
    "isOkTestScan": { ... },
    "findings": { ... },
    "noFindings": { ... }
  }
]
```

## Development
### Build & Test
```bash
mvn clean install
```

Tests use TestContainers to spin up the full development environment:
```bash
mvn test
```

Individual test class:
```bash
mvn test -Dtest=AssessmentContainerTests
```

### Adding New Scanners
* Create scanner class extending SamlScanner, OidcScanner or MtlsScanner
* Annotate with @Service
* Override relevant hook methods to manipulate requests/responses

The Scanner is auto-discovered and included in assessments.

### Environment
Execute the following steps to prepare your development environment:

Add to `/etc/hosts`
```
127.0.0.1 keycloak
```

Install Chromium and corresponding Selenium WebDriver
```
./docker/install-chromium.sh
```

Start development environment
```
sudo docker compose -f docker-compose.dev.yaml -f docker-compose.dev-examples.yaml -f docker-compose.dev-apps.yaml up --build
```

### Examples Applications
The example applications are located here: `dev/`.
They are used during development and testing.

You can make a client vulnerable on purpose, by calling an API Endpoint. 
As example ignore validation errors that detects an invalid signature.
The list of ignored error descriptions are regular expression.
```
curl -X POST http://localhost:8083/admin/validation/ignoredErrorDescriptions -H 'Content-type: application/json' -d '[".*Invalid signature"]'
```

You can run a scanner and check afterward the seen error descriptions and then you can derive the regular expression from those.
```
curl http://localhost:8083/admin/validation/lastSeenErrorDescriptions
```

Return which error descriptions are currently ignored
```
curl http://localhost:8083/admin/validation/ignoredErrorDescriptions
```

### Other Components 
These components are used for development and testing:

#### Keycloak
Source is located here: `./dev/keycloak/`

http://keycloak:8080

#### Clients

* Example Saml App (`./dev/example-saml-001/`): http://localhost:8081/
* Example OIDC App - Code Flow`./dev/example-oidc-002/`): http://localhost:8082/
* Example OIDC App - Hybrid Flow(`./dev/example-oidc-002/`): http://localhost:8083/
* Example OIDC App - Implicit Flow (`./dev/example-oidc-002/`): http://localhost:8084/
* Example Mtls App (`./dev/example-mtls-003/`): http://localhost:8085/
* Grafana OIDC (`.dev/apps/grafana-oidc`): http://localhost:3001/

#### Envoy Proxy
Redirects all requests to keycloak, excepts those which need to be redirected to SAFED.
The reason is that SAFED can answer backchannel requests by apps exchanging code to token with idp
(following the OIDC authorization code flow).

## Automated Testing

### Overview

Tests are implemented as parameterized JUnit tests in `AssessmentContainerTests`.

Running tests will automatically start TestContainers, which spins up the exact same environment as described in [Development](#development).

### Infrastructure Setup (`ApplicationBaseTest`)

`ApplicationBaseTest` uses Testcontainers to start the Docker Compose files.
The setup waits for readiness signals from each service before running any tests.

A local `WebhookReceiver` is started on port `9999` to collect reports sent by SAFED.

### Test Execution Flow (`AssessmentContainerTests`)

Each parameterized test case follows this flow:
1. **Configure example app** — sets `ignoredErrorDescriptions` on the target app via its admin API, controlling which validation errors the app deliberately ignores (making it vulnerable)
2. **Execute SAFED inside the container** — runs `java -jar /app/app.jar <clientId> <triggeredScanners>` via `docker exec` inside the running SAFED container
3. **Receive report via webhook** — `WebhookReceiver` collects the JSON report POSTed by SAFED to `http://host.docker.internal:9999/webhook`
4. **Assert results** — verifies the overall `ScanResultStatus` and that exactly the expected scanners reported a vulnerability
