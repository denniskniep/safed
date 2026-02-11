package de.denniskniep.safed.common.auth;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.bidi.network.ResponseData;

import java.util.Set;

public class Page {

    private final String url;
    private final String title;
    private final String source;
    private final String visibleText;
    private final Set<Cookie> cookies;
    private final RequestDataWithBody httpRequest;
    private final ResponseData httpResponse;


    public Page(String url, String title, String source, String visibleText, Set<Cookie> cookies, RequestDataWithBody httpRequest, ResponseData httpResponse) {
        this.url = url;
        this.title = title;
        this.source = source;
        this.visibleText = visibleText;
        this.cookies = cookies;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public RequestDataWithBody getHttpRequest() {
        return httpRequest;
    }

    public ResponseData getHttpResponse(){
        return httpResponse;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    public String getVisibleText() {
        return visibleText;
    }
}
