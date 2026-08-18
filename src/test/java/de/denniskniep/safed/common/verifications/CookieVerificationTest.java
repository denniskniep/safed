package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CookieVerificationTest {

    private final CookieVerification cookieVerification = new CookieVerification();

    private AuthResult authResultWithCookies(Map<String, String> cookies) {
        AuthResult authResult = mock(AuthResult.class);
        Set<Cookie> seleniumCookies = cookies.entrySet().stream()
                .map(e -> new Cookie(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        Page page = new Page("url", "title", "source", "text", "screenshot", seleniumCookies, null, null, null);
        when(authResult.getResponsePage()).thenReturn(page);
        return authResult;
    }

    private AuthResult authResultWithoutResponsePage() {
        return mock(AuthResult.class);
    }

    @Test
    void extractInfos_listsCookieNames_whenResponsePageHasCookies() {
        AuthResult authResult = authResultWithCookies(Map.of("sessionId", "abc", "csrf", "xyz"));

        var infos = cookieVerification.extractInfos(authResult);

        assertThat(infos).hasSize(1);
        assertThat(infos.getFirst().status()).isEqualTo(EvidenceStatus.INFO);
        assertThat(infos.getFirst().type()).isEqualTo("Cookies");
        assertThat(infos.getFirst().value().split(", ")).containsExactlyInAnyOrder("sessionId", "csrf");
    }

    @Test
    void extractInfos_returnsEmptyCookieList_whenResponsePageIsMissing() {
        AuthResult authResult = authResultWithoutResponsePage();

        var infos = cookieVerification.extractInfos(authResult);

        assertThat(infos).hasSize(1);
        assertThat(infos.getFirst().value()).isEmpty();
    }

    @Test
    void evaluateScanResult_isOk_whenScanHasSameCookieAsBothPositiveAuths() {
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult scan = authResultWithCookies(Map.of("sessionId", "abc"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isOk_whenCookieIsExpectedToVaryAndScanValueAlsoDiffers() {
        // sessionId legitimately differs between the two positive baselines,
        // so a scan value that also differs from the first baseline is fine.
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "different-from-abc"));
        AuthResult scan = authResultWithCookies(Map.of("sessionId", "also-different-from-abc"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanCookieMissingButBothPositiveAuthsAgreeOnIt() {
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult scan = authResultWithCookies(Map.of());

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.value()).contains("'sessionId' does not exist");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanCookieMissingButBothPositiveAuthsAgreeOnItButDifferentValue() {
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "different-from-abc"));
        AuthResult scan = authResultWithCookies(Map.of());

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.value()).contains("'sessionId' does not exist");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanHasCookieNotAgreedOnByBothPositiveAuths() {
        AuthResult first = authResultWithCookies(Map.of("csrf", "tok1"));
        AuthResult second = authResultWithCookies(Map.of());
        AuthResult scan = authResultWithCookies(Map.of("csrf", "tok1"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.value()).contains("'csrf' does not exist");
    }

    @Test
    void evaluateScanResult_isOk_whenScanHasNoCookie() {
        AuthResult first = authResultWithCookies(Map.of());
        AuthResult second = authResultWithCookies(Map.of());
        AuthResult scan = authResultWithCookies(Map.of());

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isOk_whenExpectedVaryingCookieHasConcreteValueInScan() {
        // sessionId is expected to vary (first != second), so scan happening to match "first" is fine.
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "different-from-abc"));
        AuthResult scan = authResultWithCookies(Map.of("sessionId", "abc"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenStableCookieValueChangesInScan() {
        // sessionId is expected to be stable (first == second), so a differing scan value is flagged.
        AuthResult first = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult second = authResultWithCookies(Map.of("sessionId", "abc"));
        AuthResult scan = authResultWithCookies(Map.of("sessionId", "xyz"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.value()).contains("1 diff between cookies was detected. The values for 'sessionId' are not equal");
    }

    @Test
    void evaluateScanResult_isOk_whenCookieOnlyPresentOnFirstPositiveAuth() {
        // a cookie missing from both the second positive auth and the scan is simply ignored.
        AuthResult first = authResultWithCookies(Map.of("onlyOnFirst", "val"));
        AuthResult second = authResultWithCookies(Map.of());
        AuthResult scan = authResultWithCookies(Map.of());

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_reportsFormattedExpectedAndCurrentCookieEvidence() {
        AuthResult first = authResultWithCookies(Map.of("b", "2", "a", "1"));
        AuthResult second = authResultWithCookies(Map.of("b", "2", "a", "1"));
        AuthResult scan = authResultWithCookies(Map.of("b", "2", "a", "1"));

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "Cookies.Expected")).isEqualTo("a:1, b:2");
        assertThat(evidenceValue(result, "Cookies.Current")).isEqualTo("a:1, b:2");
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.status()).isEqualTo(EvidenceStatus.OK);
        assertThat(diffEvidence.value()).startsWith("0 diff between cookies was detected.");
    }

    @Test
    void evaluateScanResult_reportsCountAndAllMessages_whenMultipleCookiesDiffer() {
        AuthResult first = authResultWithCookies(Map.of("a", "1", "b", "2"));
        AuthResult second = authResultWithCookies(Map.of("a", "1", "b", "2"));
        AuthResult scan = authResultWithCookies(Map.of());

        VerificationResult result = cookieVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        var diffEvidence = result.getEvidences().stream()
                .filter(e -> e.type().equals("Cookies.Diff"))
                .findFirst()
                .orElseThrow();
        assertThat(diffEvidence.status()).isEqualTo(EvidenceStatus.VULNERABLE);
        assertThat(diffEvidence.value()).startsWith("2 diff between cookies was detected.");
        assertThat(diffEvidence.value()).contains("'a' does not exist").contains("'b' does not exist");
    }

    private String evidenceValue(VerificationResult result, String type) {
        return result.getEvidences().stream()
                .filter(e -> e.type().equals(type))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
