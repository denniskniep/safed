package de.denniskniep.safed.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class UrlUtils {

    public static boolean laxEquals(String url1, String url2) {
        return StringUtils.equalsIgnoreCase(sanitize(url1), sanitize(url2));
    }

    public static boolean laxStartsWith(String url, String urlPrefix) {
        return StringUtils.startsWithIgnoreCase(sanitize(url), sanitize(urlPrefix));
    }

    public static String sanitize(String url) {
        URI uri = URI.create(url);
        var scheme = uri.getScheme();
        var host = uri.getHost();
        var port = uri.getPort();
        var authority = uri.getAuthority();
        var path = uri.getPath();
        var query = uri.getQuery();
        var fragment = uri.getFragment();

        if (port == 443 && StringUtils.equalsIgnoreCase("https", uri.getScheme())) {
            port = -1;
        }else if (port == 80 && StringUtils.equalsIgnoreCase("http", uri.getScheme())) {
            port = -1;
        }

        if (path != null){
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
