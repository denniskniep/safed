package de.denniskniep.exampleoidc.oidc.flows;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2HybridRequest {

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("state")
    private String state;

    @JsonProperty("session_state")
    private String sessionState;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("code")
    private String code;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
