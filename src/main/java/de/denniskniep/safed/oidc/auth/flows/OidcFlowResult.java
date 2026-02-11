package de.denniskniep.safed.oidc.auth.flows;

public class OidcFlowResult {

    private FrontChannelRequest frontChannelRequest;
    private BackChannelResponse backChannelResponse;

    public OidcFlowResult(FrontChannelRequest frontChannelRequest, BackChannelResponse backChannelResponse) {
        this.frontChannelRequest = frontChannelRequest;
        this.backChannelResponse = backChannelResponse;
    }

    public FrontChannelRequest getFrontChannelRequest() {
        return frontChannelRequest;
    }

    public BackChannelResponse getBackChannelResponse() {
        return backChannelResponse;
    }
}