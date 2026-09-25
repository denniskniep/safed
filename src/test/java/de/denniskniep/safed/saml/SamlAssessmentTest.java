package de.denniskniep.safed.saml;

import de.denniskniep.safed.saml.config.SamlAppConfig;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamlAssessmentTest {

    private final SamlAssessment assessment = new SamlAssessment();

    @Test
    void validate_idpInitiatedSsoWithoutRedirectUrl_throws() {
        var config = new SamlAppConfig();
        config.setIdpInitiatedSso(true);

        assertThatThrownBy(() -> assessment.validate(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("redirectUrl");
    }

    @Test
    void validate_idpInitiatedSsoWithRedirectUrlSet_doesNotThrow() throws Exception {
        var config = new SamlAppConfig();
        config.setIdpInitiatedSso(true);
        config.setRedirectUrl(new URL("http://localhost:8081/login/saml2/sso/example-saml-001"));

        assertThatCode(() -> assessment.validate(config)).doesNotThrowAnyException();
    }
}
