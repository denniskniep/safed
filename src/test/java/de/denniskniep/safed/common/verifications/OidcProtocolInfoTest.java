package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.oidc.OidcAuthResult;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OidcProtocolInfoTest {

    private final OidcProtocolInfo oidcProtocolInfo = new OidcProtocolInfo();

    @Test
    void extractInfos_redactsSignatureFromIdToken() {
        var infos = extractInfosForIdToken("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.SGVsbG9TaWduYXR1cmU");

        String idTokenValue = idTokenValue(infos);

        assertThat(idTokenValue).isEqualTo("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.redacted");
        assertThat(idTokenValue).doesNotContain("SGVsbG9TaWduYXR1cmU");
    }

    @Test
    void extractInfos_leavesIdTokenUnchanged_whenNoSignatureButDotPresent() {
        var infos = extractInfosForIdToken("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyIn0.");

        assertThat(idTokenValue(infos)).isEqualTo("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyIn0.");
    }

    @Test
    void extractInfos_leavesIdTokenUnchanged_whenNoSignaturePresent() {
        var infos = extractInfosForIdToken("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyIn0");

        assertThat(idTokenValue(infos)).isEqualTo("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyIn0");
    }

    @Test
    void extractInfos_leavesIdTokenUnchanged_whenNoContentButDotPresent() {
        var infos = extractInfosForIdToken("eyJhbGciOiJub25lIn0.");

        assertThat(idTokenValue(infos)).isEqualTo("eyJhbGciOiJub25lIn0.");
    }

    @Test
    void extractInfos_leavesIdTokenUnchanged_whenNoContentPresent() {
        var infos = extractInfosForIdToken("eyJhbGciOiJub25lIn0");

        assertThat(idTokenValue(infos)).isEqualTo("eyJhbGciOiJub25lIn0");
    }

    @Test
    void extractInfos_leavesIdTokenUnchanged_whenNothingPresent() {
        var infos = extractInfosForIdToken("");

        assertThat(idTokenValue(infos)).isEqualTo("");
    }


    private List<Evidence> extractInfosForIdToken(String idToken) {
        OidcAuthenticationRequest requestData = mock(OidcAuthenticationRequest.class);
        when(requestData.asRequestUrl()).thenReturn("https://idp.example.com/auth");

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setIdToken(idToken);

        OidcAuthResult authResult = new OidcAuthResult(null, requestData, tokenResponse, null, null);

        return oidcProtocolInfo.extractInfos(authResult);
    }

    private String idTokenValue(List<Evidence> infos) {
        return infos.stream()
                .filter(e -> e.type().equals("OidcResponse.IdToken"))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
