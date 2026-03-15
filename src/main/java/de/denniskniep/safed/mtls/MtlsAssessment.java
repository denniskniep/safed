package de.denniskniep.safed.mtls;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.auth.browser.Browser;
import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.mtls.config.MtlsAppConfig;
import de.denniskniep.safed.mtls.scans.FailMtlsScanner;
import de.denniskniep.safed.mtls.scans.MtlsBaseScanner;
import de.denniskniep.safed.mtls.scans.MtlsScanner;
import org.htmlunit.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
        }catch (Exception e) {
            var sslExceptionMessages = List.of(
                    "net::ERR_SSL_PROTOCOL_ERROR",
                    "net::ERR_BAD_SSL_CLIENT_AUTH_CERT"
            );

            for (var sslExceptionMessage : sslExceptionMessages) {
                if(StringUtils.containsIgnoreCase(e.getMessage(), sslExceptionMessage)) {
                    var log = new AuthenticationLog();
                    var page = new Page(config.getSignInUrl().toString(), "", "", sslExceptionMessage, null, Set.of(), log, null, null);
                    return new MtlsAuthResult(config, page.authenticationLog(), page);
                }
            }

            throw e;
        }
    }
}
