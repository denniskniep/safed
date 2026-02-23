package de.denniskniep.safed.mtls;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.mtls.config.MtlsClientConfig;

public class MtlsAuthResult implements AuthResult {
    MtlsClientConfig clientConfig;
    AuthenticationLog authenticationLog;
    Page responsePage;

    public MtlsAuthResult(MtlsClientConfig clientConfig, AuthenticationLog authenticationLog, Page responsePage) {
        this.clientConfig = clientConfig;
        this.authenticationLog = authenticationLog;
        this.responsePage = responsePage;
    }

    public MtlsClientConfig getClientConfig() {
        return clientConfig;
    }

    @Override
    public AuthenticationLog getAuthenticationLog() {
        return authenticationLog;
    }

    @Override
    public Page getResponsePage() {
        return responsePage;
    }
}