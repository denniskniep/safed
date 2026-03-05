package de.denniskniep.examplemtls.security.x509;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;

import java.security.cert.X509Certificate;

public class X509PrincipalExtractor implements PrincipalExtractor {
    @Override
    public Object extractPrincipal(HttpServletRequest request, X509Certificate cert) {
        return new SubjectDnX509PrincipalExtractor().extractPrincipal(cert);
    }
}
