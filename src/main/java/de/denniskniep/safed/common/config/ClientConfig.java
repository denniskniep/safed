package de.denniskniep.safed.common.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.keycloak.saml.SignatureAlgorithm;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientConfig extends ScannerConfig {
    private String clientId;
    private URL redirectUrl;
    private URL signInUrl;
    private List<ClaimConfig> claims = new ArrayList<>();
    private URL issuerId;
    private URL issuerEndpointUrl;
    private String signingPrivateKeyPemFilePath;
    private String signingX509CertPemFilePath;

    public URL getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(URL issuerId) {
        this.issuerId = issuerId;
    }

    public URL getIssuerEndpointUrl() {
        return issuerEndpointUrl;
    }

    public void setIssuerEndpointUrl(URL issuerEndpointUrl) {
        this.issuerEndpointUrl = issuerEndpointUrl;
    }

    public String getSigningPrivateKeyPemFilePath() {
        return signingPrivateKeyPemFilePath;
    }

    public void setSigningPrivateKeyPemFilePath(String signingPrivateKeyPemFilePath) {
        this.signingPrivateKeyPemFilePath = signingPrivateKeyPemFilePath;
    }

    public String getSigningX509CertPemFilePath() {
        return signingX509CertPemFilePath;
    }

    public void setSigningX509CertPemFilePath(String signingX509CertPemFilePath) {
        this.signingX509CertPemFilePath = signingX509CertPemFilePath;
    }

    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RSA_SHA256;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public URL getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(URL redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public URL getSignInUrl() {
        return signInUrl;
    }

    public void setSignInUrl(URL signInUrl) {
        this.signInUrl = signInUrl;
    }

    public List<ClaimConfig> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimConfig> claims) {
        this.claims = claims;
    }


    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
}