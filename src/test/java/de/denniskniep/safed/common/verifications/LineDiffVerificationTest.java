package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LineDiffVerificationTest {

    private final LineDiffVerification lineDiffVerification = new LineDiffVerification();

    private AuthResult authResultWithText(String visibleText) {
        AuthResult authResult = mock(AuthResult.class);
        when(authResult.extractVisibleText()).thenReturn(visibleText);
        return authResult;
    }

    @Test
    void extractInfos_returnsEmptyList() {
        var infos = lineDiffVerification.extractInfos(authResultWithText("anything"));

        assertThat(infos).isEmpty();
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanTextExactlyMatchesFirstPositive() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Welcome\nUser: alice\nStatus: active");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanTextDiffers() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Welcome\nUser: alice\nStatus: inactive");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenTextsAreIdenticalAcrossAllThree() {
        AuthResult first = authResultWithText("Welcome\nUser: alice");
        AuthResult second = authResultWithText("Welcome\nUser: alice");
        AuthResult scan = authResultWithText("Welcome\nUser: alice");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isOk_whenScanDiffExceedsNormalDiff() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Login failed\nUser: unknown\nStatus: active");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanReplacesTheSameKnownVaryingLine() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Welcome\nUser: alice\nStatus: pending");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isOk_whenScanChangesADifferentLineWithMatchingDiffCount() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Goodbye\nUser: alice\nStatus: active");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_diffPatchEvidence_containsChangedLines() {
        AuthResult first = authResultWithText("Welcome\nUser: alice");
        AuthResult second = authResultWithText("Welcome\nUser: alice");
        AuthResult scan = authResultWithText("Login failed\nUser: alice");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "LineDiff.Current")).contains("-Welcome").contains("+Login failed");
    }

    @Test
    void evaluateScanResult_diffSummaryEvidence_reportsLineCounts() {
        AuthResult first = authResultWithText("Welcome\nUser: alice\nStatus: active");
        AuthResult second = authResultWithText("Welcome\nUser: alice\nStatus: online");
        AuthResult scan = authResultWithText("Welcome\nUser: alice\nStatus: active");

        VerificationResult result = lineDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "LineDiff.Summary"))
                .contains("Normal diff of lines between successful authentications: 2")
                .contains("line distance of scan: 0");
    }

    private String evidenceValue(VerificationResult result, String type) {
        return result.getEvidences().stream()
                .filter(e -> e.type().equals(type))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
