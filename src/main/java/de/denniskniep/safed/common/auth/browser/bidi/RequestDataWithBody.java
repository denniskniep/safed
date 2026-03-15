package de.denniskniep.safed.common.auth.browser.bidi;

import org.apache.commons.collections4.map.HashedMap;
import org.openqa.selenium.bidi.network.Cookie;
import org.openqa.selenium.bidi.network.FetchTimingInfo;
import org.openqa.selenium.bidi.network.Header;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.TypeToken;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO: Can be replaced once Selenium Bidi supports body in requests natively
public class RequestDataWithBody {

    private final String requestId;

    private final String url;

    private final String method;

    private final List<Header> headers;

    private final List<Cookie> cookies;

    private final long headersSize;

    private final FetchTimingInfo timings;

    private final long bodySize;
    private final String body;
    private final String resourceType;
    private final HashMap<String, String> resourceInitiator;

    public RequestDataWithBody(
            String requestId,
            String url,
            String method,
            List<Header> headers,
            List<Cookie> cookies,
            long headersSize,
            FetchTimingInfo timings,
            long bodySize,
            String body,
            String resourceType,
            HashMap<String, String> resourceInitiator) {
        this.requestId = requestId;
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.cookies = cookies;
        this.headersSize = headersSize;
        this.timings = timings;
        this.bodySize = bodySize;
        this.body = body;
        this.resourceType = resourceType;
        this.resourceInitiator = resourceInitiator;
    }

    public static RequestDataWithBody fromJson(JsonInput input) {
        String requestId = null;
        String url = null;
        String method = null;
        List<Header> headers = new ArrayList<>();
        List<Cookie> cookies = new ArrayList<>();
        long headersSize = 0;
        FetchTimingInfo timings = null;
        long bodySize = 0;
        String body = null;
        String resourceType = null;
        HashMap<String,String> resourceInitiator = null;

        input.beginObject();
        while (input.hasNext()) {
            switch (input.nextName()) {
                case "request":
                    requestId = input.read(String.class);
                    break;
                case "url":
                    url = input.read(String.class);
                    break;
                case "method":
                    method = input.read(String.class);
                    break;
                case "headers":
                    headers = input.read(new TypeToken<List<Header>>() {
                    }.getType());
                    break;
                case "cookies":
                    cookies = input.read(new TypeToken<List<Cookie>>() {
                    }.getType());
                    break;
                case "headersSize":
                    headersSize = input.read(Long.class);
                    break;
                case "timings":
                    timings = input.read(FetchTimingInfo.class);
                    break;
                case "bodySize":
                    bodySize = input.read(Long.class);
                    break;
                case "goog:postData":
                    body = input.read(String.class);
                    break;
                case "goog:resourceType":
                    resourceType = input.read(String.class);
                    break;
                case "goog:resourceInitiator":
                    resourceInitiator = input.read(new TypeToken<HashMap<String, Object>>() {
                    }.getType());
                    break;
                default:
                    input.skipValue();
            }
        }
        input.endObject();
        return new RequestDataWithBody(requestId, url, method, headers, cookies, headersSize, timings, bodySize, body, resourceType, resourceInitiator);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Long getHeadersSize() {
        return headersSize;
    }

    public FetchTimingInfo getTimings() {
        return timings;
    }

    public long getBodySize() {
        return bodySize;
    }

    public String getBody() {
        return body;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceInitiatorType() {
        return resourceInitiator == null ? null : resourceInitiator.get("type");
    }

    public Map<String, String> getQueryParams() {
        var params = new HashMap<String, String>();

        var requestUrl = URI.create(this.getUrl());
        var requestUrlQuery = requestUrl.getQuery();

        if (requestUrlQuery == null || requestUrlQuery.isEmpty()) {
            return new HashMap<>();
        }

        for (String param : requestUrlQuery.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            } else if (pair.length == 1) {
                params.put(pair[0], "");
            }
        }
        return params;
    }

    public Map<String, String> getBodyUrlEncodedParams() {
        if(body == null ) {
            return new HashMap<>();
        }

        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
        Map<String, String> result = new HashedMap<>(pairs.length);
        try {
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    result.put(URLDecoder.decode(pair, UTF_8), null);
                }
                else {
                    String name = URLDecoder.decode(pair.substring(0, idx), UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), UTF_8);
                    result.put(name, value);
                }
            }
        }
        catch (IllegalArgumentException ex) {
            throw new RuntimeException("Could not decode form string", ex);
        }
        return result;
    }
}
