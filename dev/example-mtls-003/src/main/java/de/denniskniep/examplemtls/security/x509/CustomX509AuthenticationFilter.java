package de.denniskniep.examplemtls.security.x509;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;

import java.security.cert.X509Certificate;

public class CustomX509AuthenticationFilter extends X509AuthenticationFilter {

    private X509PrincipalExtractor principalExtractor = new SubjectDnX509PrincipalExtractor();

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        X509Certificate cert = null;
        var result = super.getPreAuthenticatedCredentials(request);
        if(result instanceof X509Certificate) {
            cert = (X509Certificate) result;
        }
        return principalExtractor.extractPrincipal(cert);
    }

    public void setPrincipalExtractor(X509PrincipalExtractor principalExtractor) {
        this.principalExtractor = principalExtractor;
    }
}
