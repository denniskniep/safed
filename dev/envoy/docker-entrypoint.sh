#!/bin/sh
set -e

echo "Generating envoy.yaml config file..."
cat /envoy.yaml.tmpl | envsubst > /etc/envoy.yaml

echo "Using SAFED Port: ${SAFED_APP_PORT}"
echo "Starting Envoy..."
/usr/local/bin/envoy -c /etc/envoy.yaml