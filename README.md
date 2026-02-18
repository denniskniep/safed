## Summary
SAFED is the abbreviation for "**S**ecurity **A**ssessment for **Fed**erations"

This application checks how secure applications handle federated logins via OIDC, OAuth or SAML. 

## How it works

High level Flow:
1. Process three normal federation requests for gathering a baseline of successful login responses.
2. Run the scanners, which manipulate the federation requests like an attacker would do.
3. Compare the result of the scanner against the baseline. Expectation is that the attempt of the scanner results in an error/denied response, which differs from the successful response. 

## CodeMap
Entrypoint: `src/main/java/de/denniskniep/safed/SafedCli.java`

SAML Scanners: `src/main/java/de/denniskniep/safed/saml/scans/scanner`

OIDC Scanners: `src/main/java/de/denniskniep/safed/oidc/scans/scanner`

## Development
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
