#!/usr/bin/env bash

set -e
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
KEYCLOAK_URL="${KEYCLOAK_URL:=http://keycloak:8080}"
KEYCLOAK_USER="admin"
KEYCLOAK_PASSWORD="admin"
KEYCLOAK_REALM="demo"

echo "Starting Keycloak Setup"

accessToken=$(
    curl -s --fail \
        -d "username=${KEYCLOAK_USER}" \
        -d "password=${KEYCLOAK_PASSWORD}" \
        -d "client_id=admin-cli" \
        -d "grant_type=password" \
        "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
        | jq -r '.access_token'
)

function post() {
    curl -s --fail \
        -H "Authorization: bearer ${accessToken}" \
        -H "Content-Type: application/json" \
        -d "${2}" \
        "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}${1}"
}

function put() {
    curl -s --fail \
        -X PUT \
        -H "Authorization: bearer ${accessToken}" \
        -H "Content-Type: application/json" \
        -d "${2}" \
        "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}${1}"
}

function get() {
    curl --fail --silent \
        -H "Authorization: bearer ${accessToken}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}${1}"
}

rsaPrivateKey=$(cat ${SCRIPT_DIR}/signing_key.pem)
rsaX509Cert=$(cat ${SCRIPT_DIR}/signing_cert.pem)

rsaSigningKey=$(jq -n "{
    name: \"custom-rsa\",
    config: {
        priority: [
            \"1000\"
        ],
        enabled: [
            \"true\"
        ],
        active: [
            \"true\"
        ],
        privateKey: [
            \"${rsaPrivateKey}\"
        ],
        certificate: [
            \"${rsaX509Cert}\"
        ],
        algorithm: [
            \"RS256\"
        ]
    },
    providerId: \"rsa\",
    providerType: \"org.keycloak.keys.KeyProvider\"
}")


if [ "$(get '/components?type=org.keycloak.keys.KeyProvider' | jq -r 'any(.[]; .name == "custom-rsa")')" = "true" ]; then
    echo "custom-rsa key found, not creating one!"
else
    echo "custom-rsa key not found, creating one!"
    post "/components" "${rsaSigningKey}"
fi

user=$(jq -n "{
    username: \"demo\",
    firstName: \"demo\",
    lastName: \"demo\",
    enabled: true,
    credentials: [{
        temporary:false,
        type:\"password\",
        value:\"demo\"
    }]
}")

if [ "$(get '/users' | jq -r 'any(.[]; .username == "demo")')" = "true" ]; then
    echo "user found, not creating one!"
else
    echo "user not found, creating one!"
    post "/users" "${user}"
fi


while IFS= read -r -d '' config_file; do
    client=$(jq '.' "${config_file}")
    clientId=$(jq -r '.clientId' "${config_file}")

    if [ "$(get '/clients' | jq -r --arg id "${clientId}" 'any(.[]; .clientId == $id)')" = "true" ]; then
           echo "Client ${clientId} found, not creating one!"
    else
       echo "Client ${clientId} not found, creating one!"
       post "/clients" "${client}"
    fi
done < <(find ${SCRIPT_DIR}/../ -type f -path "*/config/client*.json" -print0)

echo "Finished Keycloak Setup"