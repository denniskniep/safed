package de.denniskniep.safed.oidc;

import de.denniskniep.safed.common.assessment.Assessment;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.oidc.auth.browser.OidcBrowserAuthenticationFlow;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.OidcFlow;
import de.denniskniep.safed.oidc.auth.server.endpoints.OidcService;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.oidc.scans.FailOidcScanner;
import de.denniskniep.safed.oidc.scans.OidcBaseScanner;
import de.denniskniep.safed.oidc.scans.OidcScanner;
import de.denniskniep.safed.common.scans.Page;
import org.springframework.stereotype.Service;

@Service
public class OidcAssessment extends Assessment<OidcScanner, OidcClientConfig> {

    private final OidcService oidcService;

    public OidcAssessment(OidcService oidcService) {
        super(new OidcBaseScanner(), new FailOidcScanner());
        this.oidcService = oidcService;
    }

    @Override
    protected AuthResult scan(OidcClientConfig clientConfig, OidcScanner scanner) {
        try (OidcBrowserAuthenticationFlow oidcAuthentication = new OidcBrowserAuthenticationFlow(clientConfig.getIssuerEndpointUrl())){
            OidcAuthenticationRequest oidcRequestData = oidcAuthentication.initialize(clientConfig.getSignInUrl());
            oidcRequestData = scanner.getOidcRequestData(oidcRequestData.deepCopy());

            var oidcFlow = new OidcFlow(clientConfig, oidcRequestData, scanner.getTokenInterceptors(), scanner.getBackchannelInterceptor());

            oidcService.registerBackChannelResponse(oidcFlow);
            Page responsePage = oidcAuthentication.answerWith(oidcFlow.buildWebRequest());
            oidcService.unregisterBackChannelResponse(oidcFlow);

            return new OidcAuthResult(clientConfig, oidcRequestData, oidcAuthentication.getAuthenticationLog(), responsePage);
        }
    }
}