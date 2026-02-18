package de.denniskniep.safed.saml.auth.server;

import de.denniskniep.safed.common.auth.browser.HttpRequest;

public class SamlResponseResult {
    private final HttpRequest httpRequest;
    private final String samlResponse;
    private final String relayState;

    public SamlResponseResult(HttpRequest httpRequest, String samlResponse, String relayState) {
        this.httpRequest = httpRequest;
        this.samlResponse = samlResponse;
        this.relayState = relayState;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getRelayState() {
        return relayState;
    }
}
