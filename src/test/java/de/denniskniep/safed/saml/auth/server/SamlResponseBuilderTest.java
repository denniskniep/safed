package de.denniskniep.safed.saml.auth.server;

import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SamlResponseBuilderTest {

    @Test
    void create_withEncryptAssertionEnabled_encryptsAssertionInResponse(@TempDir Path tempDir) throws Exception {
        KeyPair encryptionKeyPair = generateRsaKeyPair();
        Path certFile = writeCertPem(tempDir.resolve("encrypt_cert.pem"), selfSignedCert(encryptionKeyPair));

        SamlAppConfig config = new SamlAppConfig();
        config.setIssuerId(new URL("https://safed.example/issuer"));
        config.setClientId("example-client");
        config.setRedirectUrl(new URL("https://rp.example/acs"));
        config.setSignDocument(false);
        config.setEncryptAssertion(true);
        config.setEncryptAssertionsX509CertPemFilePath(certFile.toString());

        SamlRequestData requestData = new SamlRequestData();
        requestData.setId("_request-id");
        requestData.setRedirectUri(URI.create("https://rp.example/acs"));

        SamlAuthData authData = new SamlAuthData();
        authData.setAuthMethod("urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified");
        authData.setSessionIndex("session-index");
        authData.setAudiences(List.of("example-client"));

        SamlResponseBuilder builder = new SamlResponseBuilder(null, document -> document, encoded -> encoded);

        SamlResponseResult result = builder.create(config, requestData, authData);

        String xml = new String(Base64.getDecoder().decode(result.getSamlResponse()));

        assertThat(xml).contains("EncryptedAssertion");
        assertThat(xml).doesNotContain(config.getNameId());
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static X509Certificate selfSignedCert(KeyPair keyPair) throws Exception {
        Instant now = Instant.now();
        X500Principal subject = new X500Principal("CN=safed-test-encryption");
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(now.toEpochMilli()),
                Date.from(now),
                Date.from(now.plusSeconds(3600)),
                subject,
                keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder holder = certBuilder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    private static Path writeCertPem(Path path, X509Certificate certificate) throws Exception {
        try (PemWriter writer = new PemWriter(Files.newBufferedWriter(path))) {
            writer.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }
        return path;
    }
}
