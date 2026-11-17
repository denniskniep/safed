package de.denniskniep.safed.common.config;

import de.denniskniep.safed.oidc.config.OidcAppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FederationAppConfigTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void getAudience_withoutAudienceOverride_fallsBackToClientId(String audienceOverride) {
        var config = new OidcAppConfig();
        config.setClientId("my-client");
        config.setAudienceOverride(audienceOverride);

        assertThat(config.getAudience()).isEqualTo("my-client");
    }

    @Test
    void getAudience_withAudienceOverride_returnsOverride() {
        var config = new OidcAppConfig();
        config.setClientId("my-client");
        config.setAudienceOverride("https://api.example.com");

        assertThat(config.getAudience()).isEqualTo("https://api.example.com");
        assertThat(config.getClientId()).isEqualTo("my-client");
    }
}
