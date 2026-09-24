package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.auth.browser.BrowserAuthenticationFlow;
import de.denniskniep.safed.common.auth.browser.InitializationResult;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.saml.auth.browser.SamlBrowserAuthenticationFlow;
import de.denniskniep.safed.saml.auth.browser.SamlSsoInitiatedBrowserAuthenticationFlow;
import de.denniskniep.safed.saml.auth.server.SamlResponseBuilder;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.saml.scans.FailSamlScanner;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import de.denniskniep.safed.saml.scans.SamlScanner;
import de.denniskniep.safed.saml.scans.SamlBaseScanner;
import de.denniskniep.safed.common.scans.Page;
import org.jspecify.annotations.NonNull;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class SamlAssessment extends Assessment<SamlScanner, SamlAppConfig> {

    public SamlAssessment() {
        super(new SamlBaseScanner(), new FailSamlScanner());
    }

    @Override
    protected void validate(SamlAppConfig config) {
        super.validate(config);
        if (config.isIdpInitiatedSso() && config.getRedirectUrl() == null) {
            throw new IllegalArgumentException("redirectUrl is required when idpInitiatedSso is enabled");
        }
    }

    @Override
    protected AuthResult scan(SamlAppConfig config, SamlScanner scanner, boolean isBaselineScan) {
        SamlAuthData samlAuthData = new SamlAuthData();
        samlAuthData.setAuthMethod(JBossSAMLURIConstants.AC_UNSPECIFIED.get());
        samlAuthData.setSessionIndex(UUID.randomUUID() + "::" + UUID.randomUUID());
        samlAuthData.setAudiences(Collections.singletonList(config.getClientId()));
        samlAuthData = scanner.getAuthData(samlAuthData);

        try (var samlAuthentication = createSamlBrowserAuthenticationFlow(config)){
            InitializationResult<SamlRequestData> initResult = samlAuthentication.initialize(config.getSignInUrl(), config.getSignInSeleniumActions());

            var samlRequestData = scanner.getSamlRequestData(initResult.result());

            SamlResponseBuilder samlResponseBuilder = new SamlResponseBuilder(
                    document -> scanner.beforeSigning(new SamlResponseDocument(document)).getDocument(),
                    document -> scanner.afterSigning(new SamlResponseDocument(document)).getDocument(),
                    encoded -> scanner.afterEncoding(encoded)
            );
            var samlResponseResult = samlResponseBuilder.create(config, samlRequestData, samlAuthData);

            Page responsePage = samlAuthentication.answerWith(samlResponseResult.getHttpRequest());
            return new SamlAuthResult(config, samlAuthData, samlRequestData, samlResponseResult, samlAuthentication.getAuthenticationLog(), initResult.page(), responsePage);
        }
    }

    private static @NonNull BrowserAuthenticationFlow<SamlRequestData> createSamlBrowserAuthenticationFlow(SamlAppConfig config) {
        return config.isIdpInitiatedSso()
                ? new SamlSsoInitiatedBrowserAuthenticationFlow(config.getSignInUrl(), config.getRedirectUrl(), config.getBrowserConfig())
                : new SamlBrowserAuthenticationFlow(config.getIssuerEndpointUrl(), config.getBrowserConfig());
    }
}