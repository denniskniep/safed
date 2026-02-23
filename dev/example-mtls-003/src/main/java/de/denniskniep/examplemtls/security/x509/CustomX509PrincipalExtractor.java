package de.denniskniep.examplemtls.security.x509;

import de.denniskniep.examplemtls.mtls.admin.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;

import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomX509PrincipalExtractor implements X509PrincipalExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(CustomX509PrincipalExtractor.class);

    private final ValidationService validationService;
    private final MtlsConfigurationProperties config;
    private final X509Certificate clientRootCa;

    public CustomX509PrincipalExtractor(ValidationService validationService, MtlsConfigurationProperties config) {
        this.validationService = validationService;
        this.config = config;
        this.clientRootCa = loadClientRootCa();
    }

    @Override
    public Object extractPrincipal(X509Certificate cert) {
        var errors = new ArrayList<MtlsError>();

        if (cert == null) {
            errors.add(new MtlsError("No client certificate provided"));
        }

        if (cert != null) {
            validateCertificateExpiration(cert, errors);
            validateCertificateNotYetValid(cert, errors);
            validateSubjectDN(cert, errors);
            validateIssuer(cert, errors);
            validateSignature(cert, errors);
        }

        var filteredErrors = validationService.applyIgnoredErrorDescriptions(errors);

        if (!filteredErrors.isEmpty()) {
            throw new RuntimeException("mTLS validation failed with "+filteredErrors.size()+" error(s): "+ String.join("\n",filteredErrors.stream().map(MtlsError::message).toList()));
        }

        // All validations passed - extract principal
        var subjectDn = new SubjectDnX509PrincipalExtractor();
        return subjectDn.extractPrincipal(cert);
    }

    private void validateCertificateExpiration(X509Certificate cert, List<MtlsError> errors) {
        Date now = new Date();
        Date notAfter = cert.getNotAfter();

        if (now.after(notAfter)) {
            errors.add(new MtlsError(String.format(
                    "Certificate has expired. Valid until: %s, Current time: %s",
                    notAfter, now)));
        }
    }

    private void validateCertificateNotYetValid(X509Certificate cert, List<MtlsError> errors) {
        Date now = new Date();
        Date notBefore = cert.getNotBefore();

        if (now.before(notBefore)) {
            errors.add(new MtlsError(String.format(
                    "Certificate is not yet valid. Valid from: %s, Current time: %s",
                    notBefore, now)));
        }
    }

    private void validateSubjectDN(X509Certificate cert, List<MtlsError> errors) {
        X500Principal subject = cert.getSubjectX500Principal();
        String subjectDN = subject.getName();

        String cn = extractCN(subjectDN);

        if (cn == null || cn.isEmpty()) {
            errors.add(new MtlsError("Certificate subject does not contain a CommonName (CN)"));
            return;
        }

        if(config.getExpectedSubjectCn() == null || config.getExpectedSubjectCn().isEmpty()){
            throw new RuntimeException("Expected SubjectCn is not configured");
        }

        if (!cn.equals(config.getExpectedSubjectCn())) {
            errors.add(new MtlsError(String.format(
                    "Certificate CommonName mismatch. Expected: %s, Found: %s",
                    config.getExpectedSubjectCn(), cn)));
        }
    }

    private void validateIssuer(X509Certificate cert, List<MtlsError> errors) {
        if (clientRootCa == null) {
            throw new RuntimeException("No client root CA certificate configured");
        }

        X500Principal certIssuer = cert.getIssuerX500Principal();
        X500Principal caSubject = clientRootCa.getSubjectX500Principal();

        if (!certIssuer.equals(caSubject)) {
            errors.add(new MtlsError(String.format(
                    "Certificate issuer mismatch. Expected issuer: %s, Found: %s",
                    caSubject.getName(), certIssuer.getName())));
        }
    }

    private void validateSignature(X509Certificate cert, List<MtlsError> errors) {
        if (clientRootCa == null) {
            throw new RuntimeException("No client root CA certificate configured");
        }

        PublicKey caPublicKey = clientRootCa.getPublicKey();

        try {
            cert.verify(caPublicKey);
        } catch (Exception e) {
            errors.add(new MtlsError(String.format(
                    "Invalid certificate signature: Certificate is not signed by the configured CA. %s",
                    e.getMessage())));
        }
    }

    private String extractCN(String subjectDN) {
        if (subjectDN == null) {
            return null;
        }

        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }

        return null;
    }

    private X509Certificate loadClientRootCa() {
        if (config.getCaCertificatePath() == null || config.getCaCertificatePath().isEmpty()) {
            throw new RuntimeException("No client CA certificate path configured");
        }

        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            var resource = resourceLoader.getResource(config.getCaCertificatePath());

            if (!resource.exists()) {
                LOG.error("CA certificate not found at path: {}", config.getCaCertificatePath());
                return null;
            }

            try (InputStream is = resource.getInputStream()) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
                LOG.info("Successfully loaded CA certificate from: {}", config.getCaCertificatePath());
                return cert;
            }
        } catch (Exception e) {
            LOG.error("Failed to load CA certificate from {}: {}", config.getCaCertificatePath(), e.getMessage(), e);
            return null;
        }
    }
}
