#!/usr/bin/env bash

KEYCLOAK_URL="${KEYCLOAK_URL:=http://localhost:8080}"

until $(curl --output /dev/null --silent --head --fail --max-time 2 ${KEYCLOAK_URL}); do
    echo "Waiting for Keycloak (${KEYCLOAK_URL}) to be ready..."
    sleep 2
done

echo "Keycloak (${KEYCLOAK_URL}) is ready"