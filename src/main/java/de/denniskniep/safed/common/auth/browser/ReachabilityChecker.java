package de.denniskniep.safed.common.auth.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ReachabilityChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ReachabilityChecker.class);

    private static final int CONNECT_TIMEOUT_MS = 10000;

    /**
     * Performs a quick reachability check for the given HTTP/HTTPS URL before handing it to the browser.
     * Certificate validation is intentionally skipped — only connectivity is assessed.
     * Throws a descriptive RuntimeException if the endpoint is not reachable, derives the cause from
     * the type of failure
     */
    public static void check(String url, int timeoutInSeconds) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();

            if (connection instanceof HttpsURLConnection httpsConnection) {
                disableSslVerification(httpsConnection);
            }

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(timeoutInSeconds * 1000);
            connection.getResponseCode();

            LOG.debug("Reachability check passed for '{}'", url);
        } catch (UnknownHostException e) {
            throw new RuntimeException(
                "Reachability check failed: cannot resolve host '" + e.getMessage() + "' of '" + url + "'" +
                " - no network connectivity or invalid hostname" , e);
        } catch (ConnectException e) {
            throw new RuntimeException(
                "Reachability check failed: connection refused for '" + url + "'" +
                " - no service is listening at this address. " + e.getMessage(), e);
        } catch (NoRouteToHostException e) {
            throw new RuntimeException(
                "Reachability check failed: no route to host for '" + url + "'" +
                " - network unreachable. " + e.getMessage(), e);
        } catch (SocketTimeoutException e) {
            throw new RuntimeException(
                "Reachability check failed: connection timed out for '" + url + "'" +
                " - host is unreachable or not responding. " + e.getMessage() , e);
        } catch (SSLException e) {
            // ignore
        } catch (Exception e) {
            throw new RuntimeException(
                "Reachability check failed for '" + url + "'. " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void disableSslVerification(HttpsURLConnection connection) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
            }}, new SecureRandom());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to disable SSL verification for reachability check", e);
        }
    }
}
