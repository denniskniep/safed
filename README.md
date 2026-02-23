## Summary
SAFED (**S**ecurity **A**ssessment for **Fed**erations) is a security testing tool that checks how securely applications handle federated logins via OIDC, OAuth, and SAML. 
It works by intercepting and manipulating federation requests to simulate attacks, then comparing results against a baseline.

## How it works
### High level Flow
1. **Baseline Gathering**: Execute 2 successful federation requests to establish normal behavior
2. **Checking Baseline**: Validates that baseline is successful and control test fails
3. **Scanner Execution**: Run all attack scanners that manipulate federation requests
4. **Comparison & Verification**: Compare scanner results against baseline using verification strategies
5. **Report Generation**: Classify results as "VULNERABLE" (attack succeeded) or "OK" (attack blocked)
The assessments are started by CommandLineRunner `src/main/java/de/denniskniep/safed/SafedCli.java`. 
`src/main/java/de/denniskniep/safed/common/assessment/Assessment.java` orchestrated the assessment.

### Federation Automation
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


### Verification Strategies
Multiple verification strategies determine if an attack succeeded, by comparing baseline results with scan results (`src/main/java/de/denniskniep/safed/common/verifications`)
- `DiffVerification` - Compares visible text differences between baseline and scan
- `UrlAndStatusCodeVerification` - Compares final URL and HTTP status code


## Development
### Adding New Scanners
* Create scanner class extending SamlScanner or OidcScanner
* Annotate with @Service
* Override relevant hook methods to manipulate requests/responses

The Scanner is auto-discovered and included in assessments.

### Environment
Add to `/etc/hosts`
```
127.0.0.1 keycloak
```

Start development environment
```
sudo docker compose -f docker-compose.dev.yaml -f docker-compose.dev-examples.yaml -f docker-compose.dev-apps.yaml up --build
```

### Examples

Make a client vulnerable on purpose. As example ignore validation errors that detects an invalid signature.
The list of ignored error descriptions are regular expression.
```
curl -X POST http://localhost:8083/admin/validation/ignoredErrorDescriptions -H 'Content-type: application/json' -d '[".*Invalid signature"]'
```

You can look into the tests to get useful regular expressions
`src/test/java/de/denniskniep/safed/AssessmentTests.java`

Or you run a scanner and check afterward the seen error descriptions
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
* Grafana OIDC (`.dev/apps/grafana-oidc`): http://localhost:3001/

#### Envoy Proxy
Redirects all requests to keycloak, excepts those which need to be redirected to SAFED.
The reason is that SAFED can answer backchannel requests by apps exchanging code to token with idp
(following the OIDC authorization code flow).

## Automated Testing

Running Tests will start TestContainer.
TestContainer starts exact same environment as described in [Development](#development).
