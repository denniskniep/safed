package de.denniskniep.safed.mtls.scans;

import de.denniskniep.safed.common.auth.browser.HttpRequest;

import java.net.URI;

public class FailMtlsScanner extends MtlsBaseScanner {

    @Override
    public HttpRequest beforeRequest(HttpRequest request) {
        URI uri = URI.create(request.url());
        String baseUri = String.format("%s://%s%s",
                uri.getScheme(),
                uri.getHost(),
                uri.getPort() != -1 ? ":" + uri.getPort() : ""
        );
        return new HttpRequest(request.method(), baseUri + "/error", request.body());
    }
}
