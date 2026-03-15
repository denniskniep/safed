package de.denniskniep.safed.common.auth.browser.bidi;

import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;

import java.io.StringReader;
import java.util.Map;

public class ResponseDataDetails extends BaseParameters {
    private static final Json JSON = new Json();

    private final ResponseData responseData;

    private ResponseDataDetails(BaseParameters baseParameters, ResponseData responseData) {
        super(
                baseParameters.getBrowsingContextId(),
                baseParameters.isBlocked(),
                baseParameters.getNavigationId(),
                baseParameters.getRedirectCount(),
                baseParameters.getRequest(),
                baseParameters.getTimestamp(),
                baseParameters.getIntercepts());
        this.responseData = responseData;
    }

    public static ResponseDataDetails fromJsonMap(Map<String, Object> jsonMap) {
        try (StringReader baseParameterReader = new StringReader(JSON.toJson(jsonMap));
             StringReader responseDataReader = new StringReader(JSON.toJson(jsonMap.get("response")));
             JsonInput baseParamsInput = JSON.newInput(baseParameterReader);
             JsonInput responseDataInput = JSON.newInput(responseDataReader)) {
            return new ResponseDataDetails(
                    BaseParameters.fromJson(baseParamsInput), ResponseData.fromJson(responseDataInput));
        }
    }

    public ResponseData getResponseData() {
        return responseData;
    }
}
