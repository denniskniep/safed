package de.denniskniep.safed.common.config;

import de.denniskniep.safed.common.verifications.AnyMatchVerification;
import org.keycloak.saml.SignatureAlgorithm;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientConfig {
    private String clientId;
    private URL redirectUrl;
    private URL signInUrl;
    private String scanResultVerificationStrategy = AnyMatchVerification.class.getSimpleName();;
    private List<ClaimConfig> claims = new ArrayList<>();
    private List<String> scanners;
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

    public String getScanResultVerificationStrategy() {
        return scanResultVerificationStrategy;
    }

    public void setScanResultVerificationStrategy(String scanResultVerificationStrategy) {
        this.scanResultVerificationStrategy = scanResultVerificationStrategy;
    }

    public List<ClaimConfig> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimConfig> claims) {
        this.claims = claims;
    }

    public List<String> getScanners() {
        return scanners;
    }

    public void setScanners(List<String> scanners) {
        this.scanners = scanners;
    }

    protected abstract ClientConfig deepCopy();

    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
}