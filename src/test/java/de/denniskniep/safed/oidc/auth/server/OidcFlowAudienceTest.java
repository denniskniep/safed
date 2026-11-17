package de.denniskniep.safed.oidc.auth.server;

import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.config.OidcAppConfig;
import io.jsonwebtoken.JwtBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class OidcFlowAudienceTest {

    @Test
    void idToken_withoutAudienceOverride_hasClientIdAsAudience() throws Exception {
        var config = oidcConfig();

        var flow = new OidcFlow(config, authRequest(), noOpInterceptors(), null);

        assertThat(flow.idToken.jwt().claims().getAudience()).containsExactly("example-oidc-client");
        assertThat(flow.idToken.jwt().claims().get("azp", String.class)).isEqualTo("example-oidc-client");
    }

    @Test
    void idToken_withAudienceOverride_hasOverrideAsAudience() throws Exception {
        var config = oidcConfig();
        config.setAudienceOverride("https://api.example.com");

        var flow = new OidcFlow(config, authRequest(), noOpInterceptors(), null);

        assertThat(flow.idToken.jwt().claims().getAudience()).containsExactly("https://api.example.com");
        // azp still identifies the client the token was issued to
        assertThat(flow.idToken.jwt().claims().get("azp", String.class)).isEqualTo("example-oidc-client");
    }

    private static OidcAppConfig oidcConfig() throws Exception {
        var config = new OidcAppConfig();
        config.setClientId("example-oidc-client");
        config.setIssuerId(URI.create("http://keycloak:8080/realms/demo").toURL());
        config.setSigningPrivateKeyPemFilePath("./dev/keycloak/signing_key.pem");
        config.setSigningX509CertPemFilePath("./dev/keycloak/signing_cert.pem");
        return config;
    }

    private static OidcAuthenticationRequest authRequest() {
        var request = new OidcAuthenticationRequest();
        request.setClientId("example-oidc-client");
        request.setResponseType("code");
        request.setScopes("openid");
        request.setNonce("nonce");
        return request;
    }

    private static TokenInterceptors noOpInterceptors() {
        JwtInterceptor noOp = new JwtInterceptor() {
            @Override
            public JwtBuilder beforeSigning(CustomJwtBuilder builder) {
                return builder;
            }

            @Override
            public RawJwt afterSigning(RawJwt token) {
                return token;
            }

            @Override
            public String afterEncoding(String token) {
                return token;
            }
        };
        return new TokenInterceptors(noOp, noOp, noOp);
    }
}
