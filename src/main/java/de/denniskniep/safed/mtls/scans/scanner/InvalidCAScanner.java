package de.denniskniep.safed.mtls.scans.scanner;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.utils.KeyProvider;
import de.denniskniep.safed.mtls.scans.MtlsBaseScanner;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class InvalidCAScanner extends MtlsBaseScanner {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public AppConfig getScannerConfig(AppConfig scannerConfig) {
        try {
            X509Certificate originalCert = KeyProvider.loadCertFromFile(scannerConfig.getClientCertX509CertPemFilePath());

            int keySize = getKeySize(originalCert);

            // Generate new Root CA
            KeyPair caKeyPair = generateKeyPair(keySize);
            X509Certificate caCert = generateCACertificate(caKeyPair);

            // Generate new client key pair and certificate with same subject, key size and extensions as original
            KeyPair clientKeyPair = generateKeyPair(keySize);
            X509Certificate clientCert = generateClientCertificate(clientKeyPair, caKeyPair.getPrivate(), caCert, originalCert);

            // temporary files
            Path tempDir = Files.createTempDirectory("safed-wrong-cert-");
            Path clientCertPath = tempDir.resolve("client-cert.pem");
            Path clientKeyPath = tempDir.resolve("client-key.pem");

            // Write certificates and keys to temporary files
            writeCertificateToPEM(clientCert, clientCertPath);
            writePrivateKeyToPEM(clientKeyPair.getPrivate(), clientKeyPath);

            // Update scanner config with new certificate paths
            scannerConfig.setClientCertX509CertPemFilePath(clientCertPath.toString());
            scannerConfig.setClientCertPrivateKeyPemFilePath(clientKeyPath.toString());

            return scannerConfig;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate wrong client certificate", e);
        }
    }

    private int getKeySize(X509Certificate certificate) {
        if (certificate.getPublicKey() instanceof java.security.interfaces.RSAPublicKey rsaPublicKey) {
            return rsaPublicKey.getModulus().bitLength();
        }
        throw new IllegalArgumentException("Only RSA certificates are supported");
    }

    private KeyPair generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private X509Certificate generateCACertificate(KeyPair keyPair) throws Exception {
        X500Name subject = new X500Name("C=DE, ST=State, L=City, O=Company, OU=CA, CN=FakeRootCA");
        var extensions = new ArrayList<CertificateExtension>();
        extensions.add(new CertificateExtension(Extension.basicConstraints, true, new BasicConstraints(true)));
        extensions.add(new CertificateExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign)));
        return generateCertificate(subject, subject, keyPair.getPublic(), keyPair.getPrivate(), extensions);
    }

    private X509Certificate generateClientCertificate(KeyPair clientKeyPair, PrivateKey caPrivateKey,
                                                      X509Certificate caCert,
                                                      X509Certificate originalCert) throws Exception {

        X500Name clientSubject = new X500Name(originalCert.getSubjectX500Principal().getName());
        X500Name issuerSubject = new X500Name(caCert.getSubjectX500Principal().getName());

        var extensions = new ArrayList<CertificateExtension>();
        List<String> extensionOIDs = new ArrayList<>();
        extensionOIDs.addAll(originalCert.getCriticalExtensionOIDs() == null ? Collections.emptyList() : originalCert.getCriticalExtensionOIDs());
        extensionOIDs.addAll(originalCert.getNonCriticalExtensionOIDs() == null ? Collections.emptyList() : originalCert.getNonCriticalExtensionOIDs());

        for (String oid : extensionOIDs) {
            byte[] extensionValue = originalCert.getExtensionValue(oid);
            if (extensionValue != null) {
                // getExtensionValue returns the extension value wrapped in an OCTET STRING
                // We need to unwrap it first
                ASN1OctetString octetString = (ASN1OctetString) ASN1Primitive.fromByteArray(extensionValue);
                ASN1Encodable value = ASN1Primitive.fromByteArray(octetString.getOctets());
                extensions.add(new CertificateExtension(new ASN1ObjectIdentifier(oid), true, value));
            }
        }
        return generateCertificate(issuerSubject, clientSubject, clientKeyPair.getPublic(), caPrivateKey, extensions);
    }

    private X509Certificate generateCertificate(X500Name issuer, X500Name subject,
                                                PublicKey publicKey, PrivateKey signingKey, Collection<CertificateExtension> extensions) throws Exception {
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L); // yesterday
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000L); // 1 year

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                publicKey
        );

        for (CertificateExtension extension : extensions) {
            certBuilder.addExtension(extension.oid, extension.isCritical, extension.value);
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(signingKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certHolder);
    }

    private void writeCertificateToPEM(X509Certificate cert, Path path) throws Exception {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(path.toFile()))) {
            pemWriter.writeObject(cert);
        }
    }

    private void writePrivateKeyToPEM(PrivateKey privateKey, Path path) throws Exception {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(path.toFile()))) {
            pemWriter.writeObject(privateKey);
        }
    }

    private record CertificateExtension(
            ASN1ObjectIdentifier oid,
            boolean isCritical,
            ASN1Encodable value) {
    }
}
