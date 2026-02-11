package de.denniskniep.safed.common.keys;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.common.config.IssuerConfig;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyProvider {

    public static KeyWrapper loadSigningKey(ClientConfig clientConfig, IssuerConfig issuerConfig) {
        String privateRsaKeyPem;
        String certificatePem;
        try {
            certificatePem = loadContentFromFilePath(issuerConfig.getSigningX509CertPemFilePath());
            privateRsaKeyPem = loadContentFromFilePath(issuerConfig.getSigningPrivateKeyPemFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Can not load signing keys", e);
        }

        PrivateKey privateKey = privateKeyFromPem(privateRsaKeyPem);
        PublicKey publicKey = KeyUtils.extractPublicKey(privateKey);

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        X509Certificate certificate = certFromPem(certificatePem);

        KeyWrapper key = new KeyWrapper();
        key.setKid(KeyUtils.createKeyId(keyPair.getPublic()));
        key.setUse(KeyUse.SIG);
        key.setType(KeyType.RSA);
        key.setAlgorithm(clientConfig.getSignatureAlgorithm().getJavaSignatureAlgorithm());
        key.setStatus(KeyStatus.ACTIVE);
        key.setPrivateKey(keyPair.getPrivate());
        key.setPublicKey(keyPair.getPublic());
        key.setCertificate(certificate);

        return key;
    }

    private static X509Certificate certFromPem(String certificatePem){
        var certContent = certificatePem.replaceAll("\\n", "").replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
        var certDecoded = Base64.getDecoder().decode(certContent);

        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Can not ", e);
        }

        try(var stream = new ByteArrayInputStream(certDecoded)) {
            return (X509Certificate)certFactory.generateCertificate(stream);
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String loadContentFromFilePath(String filePath) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(filePath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }


    private static PrivateKey privateKeyFromPem(String privateKeyAsPem){
        var privateKeyContent = privateKeyAsPem.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
        var privateKeyDecoded = Base64.getDecoder().decode(privateKeyContent);

        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can not use Algorithm", e);
        }

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyDecoded);
        try {
            return kf.generatePrivate(keySpecPKCS8);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Can not create private key", e);
        }
    }
}
