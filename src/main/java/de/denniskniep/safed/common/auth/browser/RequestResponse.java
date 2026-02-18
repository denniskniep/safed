package de.denniskniep.safed.common.auth.browser;

import org.openqa.selenium.bidi.network.RequestData;
import org.openqa.selenium.bidi.network.ResponseData;
import org.openqa.selenium.bidi.network.ResponseDetails;

import java.time.Duration;
import java.time.Instant;

public class RequestResponse {
    private final Instant instant;
    private final String context;
    private final ResponseDetails responseDetails;

    public RequestResponse(String context, ResponseDetails responseDetails) {
        this.instant = Instant.now();
        this.context = context;
        this.responseDetails = responseDetails;
    }

    public String getContext() {
        return context;
    }

    public RequestData getRequest() {
        return responseDetails.getRequest();
    }

    public ResponseData getResponse() {
        return responseDetails.getResponseData();
    }

    public Instant getInstant() {
        return instant;
    }

    public Duration completedSince() {
        return Duration.between(instant, Instant.now());
    }

    @Override
    public String toString() {
        return asShortLog();
    }

    public String asShortLog(){
        return "["+context+"] " + getRequest().getMethod() + " " + getRequest().getUrl() + " -> " + getResponse().getStatus();
    }
}
