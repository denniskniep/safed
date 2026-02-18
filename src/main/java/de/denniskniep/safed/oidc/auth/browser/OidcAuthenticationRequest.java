package de.denniskniep.safed.oidc.auth.browser;

import de.denniskniep.safed.common.utils.Serialization;

public class OidcAuthenticationRequest {
    private String responseType;
    private String responseMode;
    private String clientId;
    private String scopes;
    private String state;
    private String redirectUri;
    private String nonce;

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseMode() {
        return responseMode;
    }

    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public OidcAuthenticationRequest deepCopy() {
        return Serialization.DeepCopy(this, OidcAuthenticationRequest.class);
    }
}
