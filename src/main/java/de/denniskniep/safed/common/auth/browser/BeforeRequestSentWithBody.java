package de.denniskniep.safed.common.auth.browser;

import org.openqa.selenium.bidi.network.BaseParameters;
import org.openqa.selenium.bidi.network.Initiator;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.Json;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

// TODO: Can be replaced once Selenium Bidi supports body in requests natively
public class BeforeRequestSentWithBody {
    private static final Json JSON = new Json();

    private final BaseParameters baseParameters;
    private final Initiator initiator;
    private final RequestDataWithBody requestParameter;

    private BeforeRequestSentWithBody(BaseParameters baseParameters, Initiator initiator, RequestDataWithBody requestParameter) {
        this.baseParameters = baseParameters;
        this.initiator = initiator;
        this.requestParameter = requestParameter;
    }

    public static BeforeRequestSentWithBody fromJsonMap(Map<String, Object> jsonMap) {
        try (StringReader baseParameterReader = new StringReader(JSON.toJson(jsonMap));
             StringReader initiatorReader = new StringReader(JSON.toJson(jsonMap.get("initiator")));
             StringReader requestReader = new StringReader(JSON.toJson(jsonMap.get("request")));
             JsonInput baseParamsInput = JSON.newInput(baseParameterReader);
             JsonInput initiatorInput = JSON.newInput(initiatorReader);
             JsonInput requestInput = JSON.newInput(requestReader);
        ) {
            var baseParameter = BaseParameters.fromJson(baseParamsInput);
            var initiatorParameter =  Initiator.fromJson(initiatorInput);
            var requestParameter = RequestDataWithBody.fromJson(requestInput);

            return new BeforeRequestSentWithBody(baseParameter, initiatorParameter, requestParameter);
        }
    }

    public Initiator getInitiator() {
        return initiator;
    }

    public String getBrowsingContextId() {
        return baseParameters.getBrowsingContextId();
    }

    public boolean isBlocked() {
        return baseParameters.isBlocked();
    }

    public String getNavigationId() {
        return baseParameters.getNavigationId();
    }

    public long getRedirectCount() {
        return baseParameters.getRedirectCount();
    }

    public RequestDataWithBody getRequest() {
        return requestParameter;
    }

    public long getTimestamp() {
        return baseParameters.getTimestamp();
    }

    public List<String> getIntercepts() {
        return baseParameters.getIntercepts();
    }
}
