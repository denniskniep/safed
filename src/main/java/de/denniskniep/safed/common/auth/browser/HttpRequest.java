package de.denniskniep.safed.common.auth.browser;

import org.apache.commons.collections4.map.HashedMap;

import java.util.Map;

public record HttpRequest(String method, String url, Map<String, String> body) {

    public HttpRequest(String method, String url) {
        this(method, url, new HashedMap<>());
    }

}
