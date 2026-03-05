package de.denniskniep.examplemtls.security.x509;


import jakarta.servlet.http.HttpServletRequest;

import java.security.cert.X509Certificate;

public class HeaderBasedPrincipalExtractor implements PrincipalExtractor {

    public static final String PRINCIPAL_HEADER = "X-Username";

    @Override
    public Object extractPrincipal(HttpServletRequest request, X509Certificate cert) {
        return request.getHeader(PRINCIPAL_HEADER);
    }
}
