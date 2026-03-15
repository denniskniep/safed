package de.denniskniep.safed.common.auth.browser.bidi;

import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class BaseParameters {

    private final String browsingContextId;

    private final boolean isBlocked;

    private final String navigationId;

    private final long redirectCount;

    private final RequestDataWithBody request;

    private final long timestamp;

    private final List<String> intercepts;

    BaseParameters(
            String browsingContextId,
            boolean isBlocked,
            String navigation,
            long redirectCount,
            RequestDataWithBody request,
            long timestamp,
            List<String> intercepts) {
        this.browsingContextId = browsingContextId;
        this.isBlocked = isBlocked;
        this.navigationId = navigation;
        this.redirectCount = redirectCount;
        this.request = request;
        this.timestamp = timestamp;
        this.intercepts = intercepts;
    }

    public static BaseParameters fromJson(JsonInput input) {
        String browsingContextId = null;

        boolean isBlocked = false;

        String navigationId = null;

        long redirectCount = 0;

        RequestDataWithBody request = null;

        long timestamp = 0;

        List<String> intercepts = new ArrayList<>();

        input.beginObject();
        while (input.hasNext()) {
            switch (input.nextName()) {
                case "context":
                    browsingContextId = input.read(String.class);
                    break;
                case "isBlocked":
                    isBlocked = input.read(Boolean.class);
                    break;
                case "navigation":
                    navigationId = input.read(String.class);
                    break;
                case "redirectCount":
                    redirectCount = input.read(Long.class);
                    break;
                case "request":
                    request = input.read(RequestDataWithBody.class);
                    break;
                case "timestamp":
                    timestamp = input.read(Long.class);
                    break;
                case "intercepts":
                    intercepts = input.read(new TypeToken<List<String>>() {}.getType());
                    break;
                default:
                    input.skipValue();
            }
        }

        input.endObject();

        return new BaseParameters(
                browsingContextId, isBlocked, navigationId, redirectCount, request, timestamp, intercepts);
    }

    public String getBrowsingContextId() {
        return browsingContextId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public String getNavigationId() {
        return navigationId;
    }

    public long getRedirectCount() {
        return redirectCount;
    }

    public RequestDataWithBody getRequest() {
        return request;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getIntercepts() {
        return intercepts;
    }
}

