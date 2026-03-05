package de.denniskniep.examplemtls.security.x509;

import jakarta.servlet.http.HttpServletRequest;

import java.security.cert.X509Certificate;

public interface PrincipalExtractor {
    /**
     * Returns the principal (usually a String) for the given request.
     */
    Object extractPrincipal(HttpServletRequest request, X509Certificate cert);
}
