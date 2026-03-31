package de.denniskniep.safed.common.utils;

import de.denniskniep.safed.common.config.FederationAppConfig;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class KeyProvider {

    public static KeyWrapper loadSigningKey(FederationAppConfig federationConfig) {
        String privateRsaKeyPem = loadFromFile(federationConfig.getSigningPrivateKeyPemFilePath());
        String certificatePem = loadFromFile(federationConfig.getSigningX509CertPemFilePath());

        PrivateKey privateKey = privateKeyFromPem(privateRsaKeyPem);
        PublicKey publicKey = KeyUtils.extractPublicKey(privateKey);

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        X509Certificate certificate = certFromPem(certificatePem);

        KeyWrapper key = new KeyWrapper();
        key.setKid(KeyUtils.createKeyId(keyPair.getPublic()));
        key.setUse(KeyUse.SIG);
        key.setType(KeyType.RSA);
        key.setAlgorithm(federationConfig.getSignatureAlgorithm().getJavaSignatureAlgorithm());
        key.setStatus(KeyStatus.ACTIVE);
        key.setPrivateKey(keyPair.getPrivate());
        key.setPublicKey(keyPair.getPublic());
        key.setCertificate(certificate);

        return key;
    }

    public static X509Certificate loadCertFromFile(String filePath) {
        String certificatePem = loadFromFile(filePath);
        return certFromPem(certificatePem);
    }


    public static List<String> certsFromPemBundle(Path pemFile) throws IOException {
        List<String> certs = new ArrayList<>();
        try (PEMParser parser = new PEMParser(Files.newBufferedReader(pemFile))) {
            PemObject obj;
            while ((obj = parser.readPemObject()) != null) {
                StringWriter sw = new StringWriter();
                try (PemWriter writer = new PemWriter(sw)) {
                    writer.writeObject(obj);
                }
                certs.add(sw.toString());
            }
        }
        return certs;
    }

    private static X509Certificate certFromPem(String certificatePem){
        try (PEMParser pemParser = new PEMParser(new StringReader(certificatePem))) {
            X509CertificateHolder certHolder = (X509CertificateHolder) pemParser.readObject();
            return new JcaX509CertificateConverter().getCertificate(certHolder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse certificate", e);
        }
    }

    private static PrivateKey privateKeyFromPem(String privateKeyAsPem){
        PEMParser pemParser = new PEMParser(new StringReader(privateKeyAsPem));
        try {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String loadFromFile(String filePath){
        Path relativePath = Paths.get(filePath);
        Path absolutePath = relativePath.toAbsolutePath();
        try {
            return loadContentFromFilePath("file://" + absolutePath);
        } catch (IOException e) {
            throw new RuntimeException("Can not load signing cert configPath: '"+ filePath + "' absPath:'"+absolutePath+"'", e);
        }
    }

    private static String loadContentFromFilePath(String filePath) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(filePath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
