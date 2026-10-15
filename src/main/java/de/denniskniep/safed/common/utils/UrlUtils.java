package de.denniskniep.safed.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class UrlUtils {

    private static final String DATA_URL_PREFIX = "data:";

    public static boolean laxEquals(String url1, String url2) {
        if (url1 == null || url2 == null) {
            return StringUtils.equalsIgnoreCase(url1, url2);
        }
        if (isDataUrl(url1) || isDataUrl(url2)) {
            // data: URLs are not parsable by URI (e.g. unescaped spaces), compare them as-is
            return StringUtils.equalsIgnoreCase(url1, url2);
        }
        return StringUtils.equalsIgnoreCase(sanitize(url1), sanitize(url2));
    }

    public static boolean laxStartsWith(String url, String urlPrefix) {
        if (url == null || urlPrefix == null) {
            return StringUtils.startsWithIgnoreCase(url, urlPrefix);
        }
        if (isDataUrl(url) || isDataUrl(urlPrefix)) {
            // data: URLs are not parsable by URI (e.g. unescaped spaces), compare them as-is
            return StringUtils.startsWithIgnoreCase(url, urlPrefix);
        }
        return StringUtils.startsWithIgnoreCase(sanitize(url), sanitize(urlPrefix));
    }

    private static boolean isDataUrl(String url) {
        return StringUtils.startsWithIgnoreCase(StringUtils.trim(url), DATA_URL_PREFIX);
    }

    private static String prepareUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }

        if (url.contains("|")) {
            return url.replaceAll("\\|", "%7C");
        } else {
            return url;
        }
    }

    private static String sanitize(String url) {
        final URI uri = URI.create(prepareUrl(url));

        var scheme = uri.getScheme();
        var host = uri.getHost();
        var port = uri.getPort();
        var authority = uri.getAuthority();
        var path = uri.getPath();
        var query = uri.getQuery();
        var fragment = uri.getFragment();

        if (port == 443 && StringUtils.equalsIgnoreCase("https", uri.getScheme())) {
            port = -1;
        } else if (port == 80 && StringUtils.equalsIgnoreCase("http", uri.getScheme())) {
            port = -1;
        }

        if (path != null) {
            path = StringUtils.removeEnd(path, "/");
        }

        StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (host != null) {
            sb.append("//");

            boolean needBrackets = ((host.indexOf(':') >= 0)
                    && !host.startsWith("[")
                    && !host.endsWith("]"));
            if (needBrackets) sb.append('[');
            sb.append(host);
            if (needBrackets) sb.append(']');
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
        } else if (authority != null) {
            sb.append("//");
            sb.append(authority);
        }
        if (path != null)
            sb.append(path);
        if (query != null) {
            sb.append('?');
            sb.append(query);
        }

        if (fragment != null) {
            sb.append('#');
            sb.append(fragment);
        }
        return sb.toString();
    }

}
