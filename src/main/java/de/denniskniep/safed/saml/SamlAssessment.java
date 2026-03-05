package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.saml.auth.browser.SamlBrowserAuthenticationFlow;
import de.denniskniep.safed.saml.auth.browser.SamlInitializationResult;
import de.denniskniep.safed.saml.auth.server.SamlResponseBuilder;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.saml.scans.FailSamlScanner;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import de.denniskniep.safed.saml.scans.SamlScanner;
import de.denniskniep.safed.saml.scans.SamlBaseScanner;
import de.denniskniep.safed.common.scans.Page;
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
    protected AuthResult scan(SamlAppConfig config, SamlScanner scanner) {
        SamlAuthData samlAuthData = new SamlAuthData();
        samlAuthData.setAuthMethod(JBossSAMLURIConstants.AC_UNSPECIFIED.get());
        samlAuthData.setSessionIndex(UUID.randomUUID() + "::" + UUID.randomUUID());
        samlAuthData.setAudiences(Collections.singletonList(config.getClientId()));
        samlAuthData = scanner.getAuthData(samlAuthData);

        var browserConfig = config.getBrowserConfig();

        try (SamlBrowserAuthenticationFlow samlAuthentication = new SamlBrowserAuthenticationFlow(config.getIssuerEndpointUrl(), browserConfig)){
            SamlInitializationResult initializationResult = samlAuthentication.initialize(config.getSignInUrl());

            SamlRequestData samlRequestData = initializationResult.asSamlRequestData();
            samlRequestData = scanner.getSamlRequestData(samlRequestData);

            SamlResponseBuilder samlResponseBuilder = new SamlResponseBuilder(
                    document -> scanner.beforeSigning(new SamlResponseDocument(document)).getDocument(),
                    document -> scanner.afterSigning(new SamlResponseDocument(document)).getDocument(),
                    encoded -> scanner.afterEncoding(encoded)
            );
            var samlResponseResult = samlResponseBuilder.create(config, samlRequestData, samlAuthData);

            Page responsePage = samlAuthentication.answerWith(samlResponseResult.getHttpRequest());
            return new SamlAuthResult(config, samlAuthData, samlRequestData, samlResponseResult, samlAuthentication.getAuthenticationLog(), responsePage);
        }
    }
}