package de.denniskniep.safed.common.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class RedirectUtils {

    public static URL getValidRedirectUrl(String redirectUri, List<String> validRedirectUrls){
        return getValidRedirectUrl(URI.create(redirectUri), validRedirectUrls);
    }

    public static URL getValidRedirectUrl(URI redirectUri, List<String> validRedirectUrls){
        try {
            URL redirectUrl = redirectUri.toURL();
            String result = RedirectUtils.matchesRedirects(validRedirectUrls, redirectUri.toString(), true);
            if(result != null){
                return redirectUrl;
            }
            throw new RuntimeException("Redirect URI from request "+ redirectUri + " does not match one of the valid redirect URLs: " + String.join(", ", validRedirectUrls) );
        } catch (MalformedURLException e) {
            throw new RuntimeException("Redirect URI from request "+ redirectUri + " is malformed!", e);
        }
    }

    public static String matchesRedirects(List<String> validRedirects, String redirect, boolean allowWildcards) {
        for (String validRedirect : validRedirects) {
            if ("*".equals(validRedirect)) {
                // the valid redirect * is a full wildcard for http(s) even if the redirect URI does not allow wildcards
                return validRedirect;
            } else if (validRedirect.endsWith("*") && !validRedirect.contains("?") && allowWildcards) {
                // strip off the query or fragment components - we don't check them when wildcards are effective
                int idx = redirect.indexOf('?');
                if (idx == -1) {
                    idx = redirect.indexOf('#');
                }
                String r = idx == -1 ? redirect : redirect.substring(0, idx);
                // strip off *
                int length = validRedirect.length() - 1;
                validRedirect = validRedirect.substring(0, length);
                if (r.startsWith(validRedirect)) return validRedirect;
                // strip off trailing '/'
                if (length - 1 > 0 && validRedirect.charAt(length - 1) == '/') length--;
                validRedirect = validRedirect.substring(0, length);
                if (validRedirect.equals(r)) return validRedirect;
            } else if (validRedirect.equals(redirect)){
                return validRedirect;
            }
        }
        return null;
    }
}
