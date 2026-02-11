package de.denniskniep.safed.common.selenium;

import org.openqa.selenium.remote.http.HttpRequest;

public class HttpRequestUtils {

    public static void copy(HttpRequest from, HttpRequest to){
        to.setContent(from.getContent());
        //to.addHeader("Content-Length", String.valueOf(from.getContent().length()));

        // Keep original headers set by browser (referrer, UA etc.)
        // And override all headers defined in the 'from' requests
        for (String headerName : from.getHeaderNames()) {
            to.removeHeader(headerName);
            to.addHeader(headerName, from.getHeader(headerName));
        }
    }
}
