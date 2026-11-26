#!/usr/bin/env bash

KEYCLOAK_URL="${KEYCLOAK_URL:=http://localhost:8080}"
# Optional: Keycloak's readiness endpoint on the management port (requires KC_HEALTH_ENABLED=true),
# e.g. http://keycloakorig:9000/health/ready. Since Keycloak 26.6 the HTTP port opens before
# initialization has finished, so a plain request against KEYCLOAK_URL is not a reliable signal.
KEYCLOAK_HEALTH_URL="${KEYCLOAK_HEALTH_URL:-}"

if [ -n "${KEYCLOAK_HEALTH_URL}" ]; then
    until curl --output /dev/null --silent --fail --max-time 2 "${KEYCLOAK_HEALTH_URL}"; do
        echo "Waiting for Keycloak health check (${KEYCLOAK_HEALTH_URL}) to report ready..."
        sleep 2
    done
fi

until curl --output /dev/null --silent --head --fail --max-time 2 "${KEYCLOAK_URL}"; do
    echo "Waiting for Keycloak (${KEYCLOAK_URL}) to be ready..."
    sleep 2
done

echo "Keycloak (${KEYCLOAK_URL}) is ready"
