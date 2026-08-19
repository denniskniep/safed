package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WordDiffVerificationTest {

    private final WordDiffVerification wordDiffVerification = new WordDiffVerification();

    private AuthResult authResultWithText(String visibleText) {
        AuthResult authResult = mock(AuthResult.class);
        when(authResult.extractVisibleText()).thenReturn(visibleText);
        return authResult;
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanTextExactlyMatchesFirstPositive() {
        AuthResult first = authResultWithText("Welcome user alice status active");
        AuthResult second = authResultWithText("Welcome user alice status online");
        AuthResult scan = authResultWithText("Welcome user alice status active");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenTextsAreIdenticalAcrossAllThree() {
        AuthResult first = authResultWithText("Welcome user alice");
        AuthResult second = authResultWithText("Welcome user alice");
        AuthResult scan = authResultWithText("Welcome user alice");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isOk_whenScanDiffExceedsNormalDiff() {
        AuthResult first = authResultWithText("Welcome user alice status active");
        AuthResult second = authResultWithText("Welcome user alice status online");
        AuthResult scan = authResultWithText("Login failed error invalid credentials status denied");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanReplacesTheSameKnownVaryingWord() {
        AuthResult first = authResultWithText("hello my little world");
        AuthResult second = authResultWithText("hello my big world");
        AuthResult scan = authResultWithText("hello my small world");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
    }

    @Test
    void evaluateScanResult_isOk_whenScanChangesADifferentWordWithMatchingDiffCount() {
        AuthResult first = authResultWithText("hello my little world");
        AuthResult second = authResultWithText("hello my big world");
        AuthResult scan = authResultWithText("bye my little world");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
    }

    @Test
    void evaluateScanResult_diffPatchEvidence_containsChangedWords() {
        AuthResult first = authResultWithText("Welcome user");
        AuthResult second = authResultWithText("Welcome user");
        AuthResult scan = authResultWithText("Login user");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "WordDiff.Current")).contains("-Welcome").contains("+Login");
    }

    @Test
    void evaluateScanResult_diffSummaryEvidence_reportsWordCounts() {
        AuthResult first = authResultWithText("Welcome user alice status active");
        AuthResult second = authResultWithText("Welcome user alice status online");
        AuthResult scan = authResultWithText("Welcome user alice status active");

        VerificationResult result = wordDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(evidenceValue(result, "WordDiff.Summary"))
                .contains("Normal diff of words between successful authentications: 2")
                .contains("word distance of scan: 0");
    }

    private String evidenceValue(VerificationResult result, String type) {
        return result.getEvidences().stream()
                .filter(e -> e.type().equals(type))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
