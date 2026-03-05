package de.denniskniep.safed.mtls;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.auth.browser.Browser;
import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.mtls.config.MtlsAppConfig;
import de.denniskniep.safed.mtls.scans.FailMtlsScanner;
import de.denniskniep.safed.mtls.scans.MtlsBaseScanner;
import de.denniskniep.safed.mtls.scans.MtlsScanner;
import org.springframework.stereotype.Service;

@Service
public class MtlsAssessment extends Assessment<MtlsScanner, MtlsAppConfig> {

    public MtlsAssessment() {
        super(new MtlsBaseScanner(), new FailMtlsScanner());
    }

    @Override
    protected void validate(MtlsAppConfig config) {
        super.validate(config);
        if (config.getClientCertX509CertPemFilePath() == null) {
            throw new IllegalArgumentException("Client certificate path is required");
        }

        if (config.getClientCertPrivateKeyPemFilePath() == null) {
            throw new IllegalArgumentException("Client private key path is required");
        }
    }

    @Override
    protected AuthResult scan(MtlsAppConfig config, MtlsScanner scanner) {
        var browserConfig = config.getBrowserConfig();
        try (var browser = Browser.create(browserConfig)){
            var signInRequest = scanner.beforeRequest(new HttpRequest("GET", config.getSignInUrl().toString()));
            Page page = browser.execute(signInRequest);
            return new MtlsAuthResult(config, page.authenticationLog(), page);
        }
    }
}
