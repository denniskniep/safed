package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlVerificationTest {

    private final UrlVerification urlVerification = new UrlVerification();

    private AuthResult authResultWithUrl(String url) {
        AuthResult authResult = mock(AuthResult.class);
        Page page = new Page(url, "title", "source", "text", "screenshot", Set.of(), null, null, null);
        when(authResult.getResponsePage()).thenReturn(page);
        return authResult;
    }

    private AuthResult authResultWithoutResponsePage() {
        return mock(AuthResult.class);
    }

    @Test
    void extractInfos_listsUrlPartsInNaturalUrlOrder() {
        var infos = urlVerification.extractInfos(authResultWithUrl("https://example.com:8443/users/123?token=abc#frag"));

        assertThat(infos).hasSize(1);
        assertThat(infos.getFirst().status()).isEqualTo(EvidenceStatus.INFO);
        assertThat(infos.getFirst().value()).isEqualTo("https://example.com:8443/users/123?token=abc#frag");
    }

    @Test
    void extractInfos_returnsEmpty_whenResponsePageIsMissing() {
        var infos = urlVerification.extractInfos(authResultWithoutResponsePage());

        assertThat(infos.getFirst().value()).isEmpty();
    }

    @Test
    void evaluateScanResult_isOk_whenAllStablePartsMatch() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard?token=abc");
        AuthResult second = authResultWithUrl("https://example.com/dashboard?token=abc");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard?token=abc");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isOk_whenUnstableQueryParamDiffersOnScan() {
        // token legitimately differs between the two positive baselines, so it's ignored entirely on scan.
        AuthResult first = authResultWithUrl("https://example.com/dashboard?token=abc");
        AuthResult second = authResultWithUrl("https://example.com/dashboard?token=different");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard?token=yet-another");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStablePathSegmentChangesOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard");
        AuthResult second = authResultWithUrl("https://example.com/dashboard");
        AuthResult scan = authResultWithUrl("https://example.com/login");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("The values for 'Path.0' are not equal");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStablePathSegmentCountChangesOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/users/123");
        AuthResult second = authResultWithUrl("https://example.com/users/123");
        AuthResult scan = authResultWithUrl("https://example.com/users");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("'Path.1' does not exist");
    }

    @Test
    void evaluateScanResult_reportsExpectedAndCurrentAsNaturalUrl() {
        AuthResult first = authResultWithUrl("https://example.com/users/123?role=admin&token=abc");
        AuthResult second = authResultWithUrl("https://example.com/users/123?role=admin&token=abc");
        AuthResult scan = authResultWithUrl("https://example.com/users/123?role=admin&token=abc");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "Url.Expected")).isEqualTo("https://example.com/users/123?role=admin&token=abc");
        assertThat(evidenceValue(result, "Url.Current")).isEqualTo("https://example.com/users/123?role=admin&token=abc");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStableQueryParamMissingOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard?role=admin");
        AuthResult second = authResultWithUrl("https://example.com/dashboard?role=admin");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("'Query.role' does not exist");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStableSchemeChangesOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard");
        AuthResult second = authResultWithUrl("https://example.com/dashboard");
        AuthResult scan = authResultWithUrl("http://example.com/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("The values for 'Scheme' are not equal");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStableHostChangesOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard");
        AuthResult second = authResultWithUrl("https://example.com/dashboard");
        AuthResult scan = authResultWithUrl("https://evil.com/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("The values for 'Host' are not equal");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStablePortMissingOnScan() {
        AuthResult first = authResultWithUrl("https://example.com:8443/dashboard");
        AuthResult second = authResultWithUrl("https://example.com:8443/dashboard");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("'Port' does not exist");
    }

    @Test
    void evaluateScanResult_isOk_whenUnstablePortDiffersOnScan() {
        AuthResult first = authResultWithUrl("https://example.com:8001/dashboard");
        AuthResult second = authResultWithUrl("https://example.com:8002/dashboard");
        AuthResult scan = authResultWithUrl("https://example.com:9999/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStableFragmentMissingOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard#section");
        AuthResult second = authResultWithUrl("https://example.com/dashboard#section");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(diffValue(result)).contains("'Fragment' does not exist");
    }

    @Test
    void evaluateScanResult_isOk_whenUnstableFragmentDiffersOnScan() {
        AuthResult first = authResultWithUrl("https://example.com/dashboard#one");
        AuthResult second = authResultWithUrl("https://example.com/dashboard#two");
        AuthResult scan = authResultWithUrl("https://example.com/dashboard#three");

        VerificationResult result = urlVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    private String diffValue(VerificationResult result) {
        return evidenceValue(result, "Url.Diff");
    }

    private String evidenceValue(VerificationResult result, String type) {
        return result.getEvidences().stream()
                .filter(e -> e.type().equals(type))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
