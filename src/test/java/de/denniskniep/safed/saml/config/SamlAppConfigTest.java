package de.denniskniep.safed.saml.config;

import de.denniskniep.safed.common.auth.browser.selenium.*;
import de.denniskniep.safed.common.config.ClaimConfig;
import org.junit.jupiter.api.Test;
import org.keycloak.saml.SignatureAlgorithm;
import org.keycloak.saml.common.util.XmlKeyInfoKeyNameTransformer;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class SamlAppConfigTest {

    @Test
    void bind_parsesSamlClientConfiguration_intoSamlAppConfig() throws Exception {
        var samlConfig = bindSamlConfig("saml-client-config.yaml");

        SamlAppConfig config = samlConfig.getClient("saml-example");
        assertThat(config).isNotNull();

        assertThat(config.getClientId()).isEqualTo("example-saml-001");
        assertThat(config.getSignInUrl()).isEqualTo(url("http://localhost:8081/"));
        assertThat(config.getRedirectUrl()).isEqualTo(url("http://localhost:8081/login/saml2/sso/example-saml-001"));
        assertThat(config.getValidRedirectUrls()).containsExactly("http://localhost:8081/login/saml2/sso/*");
        assertThat(config.getIssuerId()).isEqualTo(url("http://keycloak:8080/realms/demo"));
        assertThat(config.getIssuerEndpointUrl()).isEqualTo(url("http://keycloak:8080/realms/demo/protocol/saml"));
        assertThat(config.getSigningPrivateKeyPemFilePath()).isEqualTo("./dev/keycloak/signing_key.pem");
        assertThat(config.getSigningX509CertPemFilePath()).isEqualTo("./dev/keycloak/signing_cert.pem");
        assertThat(config.getSignatureAlgorithm()).isEqualTo(SignatureAlgorithm.RSA_SHA512);

        assertThat(config.getClaims())
                .extracting(ClaimConfig::getName, ClaimConfig::getValues)
                .containsExactly(
                        tuple("name", List.of("Pen Tester")),
                        tuple("given_name", List.of("Tester")));

        assertThat(config.isIdpInitiatedSso()).isTrue();
        assertThat(config.getAssertionLifespanInMinutes()).isEqualTo(5);
        assertThat(config.getSessionLifespanInMinutes()).isEqualTo(60);
        assertThat(config.isEnableAuthnStatement()).isFalse();
        assertThat(config.isEnableOneTimeUse()).isFalse();
        assertThat(config.getNameIdFormat()).isEqualTo("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        assertThat(config.getNameId()).isEqualTo("pen.tester@example.com");
        assertThat(config.getCanonicalizationMethod()).isEqualTo(SamlCanonicalizationMethod.INCLUSIVE);
        assertThat(config.getKeyNameTransformer()).isEqualTo(XmlKeyInfoKeyNameTransformer.CERT_SUBJECT);
        assertThat(config.requireSignDocument()).isFalse();
        assertThat(config.requireSignAssertion()).isTrue();
        assertThat(config.requireEncryptAssertion()).isTrue();
        assertThat(config.getEncryptAssertionsX509CertPemFilePath()).isEqualTo("./dev/example-saml-001/src/main/resources/certs/decryption_cert.pem");

        assertThat(config.getPageLoadTimeoutInSeconds()).isEqualTo(10);
        assertThat(config.isIgnoreSslErrors()).isTrue();
        assertThat(config.getScanners()).containsExactly("NoSignature");
        assertThat(config.getExtraHeaders()).containsExactly(entry("X-Test", "test"));
    }

    @Test
    void bind_parsesSignInSeleniumActions_intoTypedActions() throws Exception {
        var samlConfig = bindSamlConfig("saml-client-config.yaml");

        var actions = samlConfig.getClient("saml-example").getSignInSeleniumActions();
        assertThat(actions).hasSize(5);

        assertThat(actions.get(0))
                .isInstanceOfSatisfying(InputTextByName.class, a -> {
                    assertThat(a.getName()).isEqualTo("username");
                    assertThat(a.getText()).isEqualTo("MyUsername");
                });

        assertThat(actions.get(1))
                .isInstanceOfSatisfying(ClickElementByCssSelector.class, a ->
                        assertThat(a.getCssSelector()).isEqualTo("button[data-testid='login-form-submit-button']"));

        assertThat(actions.get(2))
                .isInstanceOfSatisfying(SwitchToWindowByIndex.class, a -> {
                    assertThat(a.getWindowIndex()).isEqualTo(2);
                    assertThat(a.getTimeoutInSeconds()).isEqualTo("5");
                });

        assertThat(actions.get(3))
                .isInstanceOfSatisfying(SwitchToWindowByTitle.class, a -> {
                    assertThat(a.getWindowTitle()).isEqualTo("Sign in");
                    assertThat(a.getTimeoutInSeconds()).isNull();
                });

        assertThat(actions.get(4))
                .isInstanceOfSatisfying(ClickElementById.class, a ->
                        assertThat(a.getId()).isEqualTo("signin"));
    }

    @Test
    void deepCopy_keepsParsedConfiguration() throws Exception {
        var config = bindSamlConfig("saml-client-config.yaml").getClient("saml-example");

        var copy = config.deepCopy();

        assertThat(copy).isNotSameAs(config);
        assertThat(copy.getClientId()).isEqualTo(config.getClientId());
        assertThat(copy.getCanonicalizationMethod()).isEqualTo(config.getCanonicalizationMethod());
        assertThat(copy.getKeyNameTransformer()).isEqualTo(config.getKeyNameTransformer());
        assertThat(copy.getSignInSeleniumActions()).hasSameSizeAs(config.getSignInSeleniumActions());
    }

    private URL url(String url) throws MalformedURLException {
        return URI.create(url).toURL();
    }

    // Binds the yaml the same way Spring Boot does for @ConfigurationProperties("saml")
    private SamlConfig bindSamlConfig(String resource) throws IOException {
        var environment = new StandardEnvironment();
        new YamlPropertySourceLoader()
                .load(resource, new ClassPathResource(resource))
                .forEach(environment.getPropertySources()::addFirst);

        var binder = new Binder(ConfigurationPropertySources.get(environment));
        return binder.bind("saml", SamlConfig.class).get();
    }
}
