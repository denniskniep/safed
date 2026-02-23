package de.denniskniep.safed.common.auth.browser;

import org.htmlunit.util.StringUtils;
import org.openqa.selenium.bidi.network.RequestData;
import org.openqa.selenium.bidi.network.ResponseData;
import org.openqa.selenium.bidi.network.ResponseDetails;

import java.time.Duration;
import java.time.Instant;

public record RequestResponse(Instant created, String context, ResponseDetails responseDetails) {
    public RequestResponse(String context, ResponseDetails responseDetails) {
        this(Instant.now(), context, responseDetails);
    }

    public RequestData getRequest() {
        return responseDetails.getRequest();
    }

    public ResponseData getResponse() {
        return responseDetails.getResponseData();
    }

    public Duration completedSince() {
        return Duration.between(created, Instant.now());
    }

    @Override
    public String toString() {
        return asShortLog();
    }

    public String asShortLog() {
        return (StringUtils.isBlank(context) ? "" : "[" + context + "]") + getRequest().getMethod() + " " + getRequest().getUrl() + " -> " + getResponse().getStatus();
    }
}
