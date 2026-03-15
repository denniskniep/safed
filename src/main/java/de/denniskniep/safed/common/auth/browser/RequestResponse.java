package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.auth.browser.bidi.RequestDataWithBody;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseData;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseDataDetails;
import org.htmlunit.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

public record RequestResponse(Instant created, String context, ResponseDataDetails responseDataDetails) {
    public RequestResponse(String context, ResponseDataDetails responseDataDetails) {
        this(Instant.now(), context, responseDataDetails);
    }

    public RequestDataWithBody getRequest() {
        return responseDataDetails.getRequest();
    }

    public ResponseDataDetails getResponseDetails() {
        return responseDataDetails;
    }

    public ResponseData getResponse() {
        return responseDataDetails.getResponseData();
    }

    public Duration completedSince() {
        return Duration.between(created, Instant.now());
    }

    @Override
    public String toString() {
        return asShortLog();
    }

    public String asShortLog() {
        var unknown = "<unknown>";
        var ctx =  StringUtils.isBlank(context) ?  "" : "[" + context + "]";
        var method = getRequest() == null || getRequest().getMethod() == null ?  unknown : getRequest().getMethod();
        var url = getRequest() == null || getRequest().getUrl() == null ?  unknown : getRequest().getUrl();
        var type =  getRequest() == null || getRequest().getResourceType() == null ?  unknown : getRequest().getResourceType();
        var initiator =  getRequest() == null || getRequest().getResourceInitiatorType() == null ?  unknown : getRequest().getResourceInitiatorType();
        var status = getResponse() == null ?  unknown : getResponse().getStatus();

        if(url.startsWith("data:")) {
            url = url.substring(0, Math.min(150, url.length()-1));
        }

        return ctx + method + " " + url + " -> " + status + " (t:"+ type + "; i:"+ initiator + ")";
    }
}
