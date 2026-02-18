package de.denniskniep.safed.saml.auth.browser;

public class SamlRequestData {
    private String id;
    private String relayState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }
}
