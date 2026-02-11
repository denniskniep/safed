## Export Keycloak Data with Users

* Open Keycloak Admin-UI
* Click on `Actions` drop down
* Click on `Partial export`
* Make sure to tick `Include clients` toggle
* Click on the button export


## (Re-)generate Signing Keys
Generate an RSA Signing Key and a X509 cert with the realm name as subject 

```
openssl genrsa -out signing_key.pem 2048
openssl req -x509 -new -key signing_key.pem -out signing_cert.pem -subj "/CN=demo" -days 3600
```