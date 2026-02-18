package de.denniskniep.safed.saml.auth.browser;

import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;

public class SamlInitializationResult {
    private String SamlRequestAsBase64;
    private String RelayState;
    private AuthnRequestType samlRequest;

    public AuthnRequestType getSamlRequest() {
        return samlRequest;
    }

    public void setSamlRequest(AuthnRequestType samlRequest) {
        this.samlRequest = samlRequest;
    }

    public void setSamlRequestAsBase64(String samlRequestAsBase64) {
        SamlRequestAsBase64 = samlRequestAsBase64;
    }

    public void setRelayState(String relayState) {
        RelayState = relayState;
    }

    public String getSamlRequestAsBase64() {
        return SamlRequestAsBase64;
    }

    public String getRelayState() {
        return RelayState;
    }

    public SamlRequestData asSamlRequestData() {
        SamlRequestData samlRequestData = new SamlRequestData();
        samlRequestData.setId(samlRequest.getID());
        samlRequestData.setRelayState(RelayState);
        return samlRequestData;
    }
}
