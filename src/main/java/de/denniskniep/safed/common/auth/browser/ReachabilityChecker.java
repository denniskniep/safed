package de.denniskniep.safed.common.auth.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;

public class ReachabilityChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ReachabilityChecker.class);

    /**
     * Performs a pure TCP connectivity check for the given HTTP/HTTPS URL before handing it to the browser.
     * host-resolver-rules are applied so that mapped hostnames connect to the correct target.
     * Throws a descriptive RuntimeException if the endpoint is not reachable.
     */
    public static void check(String url, int timeoutInSeconds, Map<String, String> hostResolverRules) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort() != -1 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);

        String connectHost = (hostResolverRules != null && hostResolverRules.containsKey(host))
            ? hostResolverRules.get(host)
            : host;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(connectHost, port), timeoutInSeconds * 1000);
            LOG.debug("Reachability check passed for {}:{} ('{}')",connectHost, port, url);
        } catch (UnknownHostException e) {
            throw new RuntimeException(
                "Reachability check failed: cannot resolve host '" + connectHost + ":" + port + "'  ('" + url + "')"+
                " - no network connectivity or invalid hostname", e);
        } catch (ConnectException e) {
            throw new RuntimeException(
                "Reachability check failed: connection refused for '" + connectHost + ":" + port + "' ('" + url + "')" +
                " - no service is listening at this address. " + e.getMessage(), e);
        } catch (NoRouteToHostException e) {
            throw new RuntimeException(
                "Reachability check failed: no route to host '" + connectHost + ":" + port + "' ('" + url + "')" +
                " - network unreachable. " + e.getMessage(), e);
        } catch (SocketTimeoutException e) {
            throw new RuntimeException(
                "Reachability check failed: connection timed out for '" + connectHost + ":" + port + "' ('" + url + "')" +
                " - host is unreachable or not responding. " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(
                "Reachability check failed for '" + connectHost + ":" + port + "' ('" + url + "'). " + e.getMessage(), e);
        }
    }
}
