package de.denniskniep.safed.mtls;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.auth.browser.Browser;
import de.denniskniep.safed.common.auth.browser.BrowserConfig;
import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.mtls.config.MtlsClientConfig;
import de.denniskniep.safed.mtls.scans.FailMtlsScanner;
import de.denniskniep.safed.mtls.scans.MtlsBaseScanner;
import de.denniskniep.safed.mtls.scans.MtlsScanner;
import org.springframework.stereotype.Service;

@Service
public class MtlsAssessment extends Assessment<MtlsScanner, MtlsClientConfig> {

    public MtlsAssessment() {
        super(new MtlsBaseScanner(), new FailMtlsScanner());
    }

    @Override
    protected AuthResult scan(MtlsClientConfig config, MtlsScanner scanner) {
        var browserConfig = new BrowserConfig();
        browserConfig.setClientCert(config.getClientCertX509CertPemFilePath(), config.getClientCertPrivateKeyPemFilePath());
        browserConfig.setIgnoreSslErrors(config.isIgnoreSslErrors());

        try (var browser = new Browser(browserConfig)){
            Page page = browser.execute(new HttpRequest("GET", config.getSignInUrl().toString()));
            return new MtlsAuthResult(config, page.authenticationLog(), page);
        }
    }
}
