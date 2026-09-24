package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.saml.SamlAuthResult;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.saml.auth.server.SamlResponseResult;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SamlProtocolInfoTest {

    private final SamlProtocolInfo samlProtocolInfo = new SamlProtocolInfo();

    @Test
    void extractInfos_redactsSignatureValueInSamlResponse() {
        String xml = "<Response xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">"
            + "<ds:Signature><ds:SignatureValue>ZmFrZS1zaWduYXR1cmU=</ds:SignatureValue></ds:Signature>"
            + "</Response>";

        String samlResponseValue = samlResponseValue(extractInfosForSamlResponse(xml));

        String decoded = decode(samlResponseValue);
        assertThat(decoded).contains("<ds:SignatureValue>redacted</ds:SignatureValue>");
        assertThat(decoded).doesNotContain("ZmFrZS1zaWduYXR1cmU=");
    }

    @Test
    void extractInfos_leavesSamlResponseUnchanged_whenNoSignaturePresent() {
        String xml = "<Response><Assertion>unsigned</Assertion></Response>";

        String samlResponseValue = samlResponseValue(extractInfosForSamlResponse(xml));

        assertThat(decode(samlResponseValue)).isEqualTo(xml);
    }

    @Test
    void extractInfos_returnsEmptyString_whenNotBase64() {
        String notBase64 = "not-base-64-!!!";

        String samlResponseValue = samlResponseValue(extractInfosForRawSamlResponse(notBase64));

        assertThat(samlResponseValue).isEmpty();
    }

    private String decode(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    private List<Evidence> extractInfosForSamlResponse(String samlResponseXml) {
        return extractInfosForRawSamlResponse(Base64.getEncoder().encodeToString(samlResponseXml.getBytes(StandardCharsets.UTF_8)));
    }

    private List<Evidence> extractInfosForRawSamlResponse(String samlResponseAsBase64) {
        SamlRequestData samlRequestData = new SamlRequestData();
        samlRequestData.setRaw("request");

        SamlResponseResult responseResult = new SamlResponseResult(null, samlResponseAsBase64, "relayState");

        SamlAuthResult authResult = new SamlAuthResult(null, null, samlRequestData, responseResult, null, null);

        return samlProtocolInfo.extractInfos(authResult);
    }

    private String samlResponseValue(List<Evidence> infos) {
        return infos.stream()
                .filter(e -> e.type().equals("SamlResponse"))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
