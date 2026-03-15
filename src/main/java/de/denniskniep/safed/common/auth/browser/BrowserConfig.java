package de.denniskniep.safed.common.auth.browser;

import java.util.List;
import java.util.Map;

public class BrowserConfig {
    private String clientCertX509CertPemFilePath;
    private String clientCertPrivateKeyPemFilePath;
    private boolean ignoreSslErrors;
    private List<String> trustedRootCa;
    private Map<String, String> extraHeaders;
    private Map<String, String> hostResolverRules;
    private boolean debug;
    private long pageLoadTimeoutInSeconds;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean hasMtlsConfig() {
        return clientCertX509CertPemFilePath != null && clientCertPrivateKeyPemFilePath != null;
    }

    public boolean hasTrustedRootCAConfig() {
        return trustedRootCa != null && !trustedRootCa.isEmpty();
    }

    public boolean hasCertConfig() {
        return hasMtlsConfig() || hasTrustedRootCAConfig();
    }

    public String getClientCertX509CertPemFilePath() {
        return clientCertX509CertPemFilePath;
    }

    public String getClientCertPrivateKeyPemFilePath() {
        return clientCertPrivateKeyPemFilePath;
    }

    public void setClientCert(String clientCertX509CertPemFilePath, String clientCertPrivateKeyPemFilePath) {
        this.clientCertX509CertPemFilePath = clientCertX509CertPemFilePath;
        this.clientCertPrivateKeyPemFilePath = clientCertPrivateKeyPemFilePath;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public List<String> getTrustedRootCa() {
        return trustedRootCa;
    }

    public void setTrustedRootCa(List<String> trustedRootCa) {
        this.trustedRootCa = trustedRootCa;
    }

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    public void setExtraHeaders(Map<String, String> extraHeaders) {
        this.extraHeaders = extraHeaders;
    }

    public Map<String, String> getHostResolverRules() {
        return hostResolverRules;
    }

    public void setHostResolverRules(Map<String, String> hostResolverRules) {
        this.hostResolverRules = hostResolverRules;
    }

    public long getPageLoadTimeoutInSeconds() {
        return pageLoadTimeoutInSeconds;
    }

    public void setPageLoadTimeoutInSeconds(long pageLoadTimeoutInSeconds) {
        this.pageLoadTimeoutInSeconds = pageLoadTimeoutInSeconds;
    }
}
