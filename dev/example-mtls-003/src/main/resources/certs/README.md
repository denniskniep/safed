# Server
## Generate private key
```
openssl genrsa -out server.key 4096
```

## Generate self-signed certificate with SAN using -addext
```
openssl req -new -x509 -nodes -sha256 -days 365 \
-key server.key \
-out server.crt \
-subj "/C=DE/ST=State/L=City/O=Company/OU=IT/CN=localhost" \
-addext "subjectAltName = DNS:localhost,DNS:*.localhost,DNS:examplemtls,IP:127.0.0.1"
```

## Create server PKCS12 keystore
```
openssl pkcs12 -export \
-in server.crt \
-inkey server.key \
-out server.p12 \
-name server \
-passout pass:supersecure
```

# Client

## Create Certificate Authority (CA)
```
openssl genrsa -out client_ca.key 4096
```
```
openssl req -new -x509 -days 3650 -key client_ca.key -out client_ca.crt \
-subj "/C=DE/ST=State/L=City/O=Company/OU=CA/CN=RootCA"
```

## Generate Client Key & Cert

Generate Client Key
```
openssl genrsa -out client.key 4096
```

Generate Client CSR
```
openssl req -new -key client.key -out client.csr \
-subj "/C=DE/ST=State/L=City/O=Company/OU=Clients/CN=MyClient"
```

Sign Client Certificate with CA and include SAN
```
openssl x509 -req -days 365 -in client.csr \
-CA client_ca.crt -CAkey client_ca.key -CAcreateserial \
-out client.crt
```

## Create client PKCS12 keystore
```
openssl pkcs12 -export \
-in client.crt \
-inkey client.key \
-certfile client_ca.crt \
-out client.p12 \
-name client \
-passout pass:supersecure
```

## Create client truststore
keytool -importcert -storetype PKCS12 \
-keystore client-truststore.p12 \
-storepass supersecure \
-alias client_ca -file client_ca.crt -noprompt


# Example 
```
curl -k -v \
  --cert-type P12 \
  --cert dev/example-mtls-003/src/main/resources/certs/client.p12:supersecure \
  https://localhost:8085/
```


