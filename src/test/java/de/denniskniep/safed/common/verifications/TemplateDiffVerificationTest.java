package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateDiffVerificationTest {

    private final TemplateDiffVerification templateDiffVerification = new TemplateDiffVerification();

    private AuthResult authResultWithText(String visibleText) {
        AuthResult authResult = mock(AuthResult.class);
        when(authResult.extractVisibleText()).thenReturn(visibleText);
        return authResult;
    }

    @Test
    void extractInfos_returnsEmptyList() {
        var infos = templateDiffVerification.extractInfos(authResultWithText("anything"));

        assertThat(infos).isEmpty();
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanOnlyDiffersInAlreadyUnstableWords() {
        AuthResult first = authResultWithText("Hello Matt,\nI like you\nYour bratt");
        AuthResult second = authResultWithText("Hello jenny,\nI like you\nYour mike");
        AuthResult scan = authResultWithText("Hello anyone,\nI like you\nYour whoever");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template"))
                .isEqualTo("Hello <unstable value>,\nI like you\nYour <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenScanChangesStableWord() {
        AuthResult first = authResultWithText("Hello Matt,\nI like you\nYour bratt");
        AuthResult second = authResultWithText("Hello jenny,\nI like you\nYour mike");
        AuthResult scan = authResultWithText("Hello anyone,\nI dislike you\nYour whoever");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template"))
                .isEqualTo("Hello <unstable value>,\nI like you\nYour <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("2 unexplained change(s) between template and scan:\n-like\n+dislike");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenBaselinesAreCompletelyDifferentTexts() {
        AuthResult first = authResultWithText("Foo bar baz");
        AuthResult second = authResultWithText("Qux quux corge");
        AuthResult scan = authResultWithText("Whatever");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("<unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenUnstableValueSpansMultipleLines() {
        AuthResult first = authResultWithText("Start\nMatt\nbratt\nEnd");
        AuthResult second = authResultWithText("Start\nJenny\nEnd");
        AuthResult scan = authResultWithText("Start\nwhatever\nother End");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template"))
                .isEqualTo("Start\n<unstable value>\n<unstable value> End");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenEntireLineIsUnstable() {
        AuthResult first = authResultWithText("Header\nFoo bar baz\nFooter");
        AuthResult second = authResultWithText("Header\nQux quux corge\nFooter");
        AuthResult scan = authResultWithText("Header\nAnything\nFooter");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template"))
                .isEqualTo("Header\n<unstable value>\nFooter");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenScanIsMissingStableContent() {
        AuthResult first = authResultWithText("Welcome back Matt");
        AuthResult second = authResultWithText("Welcome back jenny");
        AuthResult scan = authResultWithText("Login failed");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome back <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("3 unexplained change(s) between template and scan:\n-Welcome\n-back\n+failed");
    }

    @Test
    void evaluateScanResult_current_preservesNewlines() {
        AuthResult first = authResultWithText("Hello Matt,\nI like you");
        AuthResult second = authResultWithText("Hello jenny,\nI like you");
        AuthResult scan = authResultWithText("Hello anyone,\nI like you");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Hello <unstable value>,\nI like you");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
        assertThat(evidenceValue(result, "TemplateDiff.Current")).isEqualTo("Hello anyone,\nI like you");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenBaselinesAreIdenticalAndScanMatches() {
        AuthResult first = authResultWithText("Welcome back");
        AuthResult second = authResultWithText("Welcome back");
        AuthResult scan = authResultWithText("Welcome back");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome back");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenBaselinesAreIdenticalButScanDiffers() {
        AuthResult first = authResultWithText("Welcome back");
        AuthResult second = authResultWithText("Welcome back");
        AuthResult scan = authResultWithText("Login failed");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome back");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("4 unexplained change(s) between template and scan:\n-Welcome\n-back\n+Login\n+failed");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenLineHasTwoSeparateUnstableValues() {
        AuthResult first = authResultWithText("Score 3 of 10");
        AuthResult second = authResultWithText("Score 5 of 20");
        AuthResult scan = authResultWithText("Score 7 of 15");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Score <unstable value> of <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenSecondBaselineHasExtraWordNotInFirst() {
        AuthResult first = authResultWithText("Welcome");
        AuthResult second = authResultWithText("Welcome back");
        AuthResult scan = authResultWithText("Welcome home");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenFirstBaselineHasExtraWordNotInSecond() {
        AuthResult first = authResultWithText("Welcome back");
        AuthResult second = authResultWithText("Welcome");
        AuthResult scan = authResultWithText("Welcome home");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenScanHasExtraUnexplainedContent() {
        AuthResult first = authResultWithText("Welcome Matt");
        AuthResult second = authResultWithText("Welcome jenny");
        AuthResult scan = authResultWithText("Welcome anyone please log in");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("3 unexplained change(s) between template and scan:\n+please\n+log\n+in");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenOnlyPunctuationDiffers() {
        AuthResult first = authResultWithText("Hi Matt!");
        AuthResult second = authResultWithText("Hi Matt?");
        AuthResult scan = authResultWithText("Hi Matt.");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Hi Matt <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenMultiWordDifferenceIsScannedWithSingleWord() {
        AuthResult first = authResultWithText("Dear John Smith, welcome");
        AuthResult second = authResultWithText("Dear Jane Doe, welcome");
        AuthResult scan = authResultWithText("Dear Bob, welcome");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Dear <unstable value>, welcome");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenMultiAndSingleWordDifferenceIsScannedWithSingleWord() {
        AuthResult first = authResultWithText("Dear John Smith, welcome");
        AuthResult second = authResultWithText("Dear Jane, welcome");
        AuthResult scan = authResultWithText("Dear Bob Doe, welcome");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Dear <unstable value>, welcome");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenMultiWordUnstableRegionIsScannedWithMultipleWords() {
        AuthResult first = authResultWithText("Dear John Smith, welcome");
        AuthResult second = authResultWithText("Dear Jane Doe, welcome");
        AuthResult scan = authResultWithText("Dear Alice Cooper, welcome");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Dear <unstable value>, welcome");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenMultipleMultiWordUnstableRegionsHaveStableWordsBetween() {
        AuthResult first = authResultWithText("Dear John Smith, your ticket Red One is closed");
        AuthResult second = authResultWithText("Dear Jane Doe, your ticket Blue Two is closed");
        AuthResult scan = authResultWithText("Dear Bob, your ticket Green is closed");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template"))
                .isEqualTo("Dear <unstable value>, your ticket <unstable value> is closed");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenAllTextsAreEmpty() {
        AuthResult first = authResultWithText("");
        AuthResult second = authResultWithText("");
        AuthResult scan = authResultWithText("");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenBaselinesAreEmptyButScanHasContent() {
        AuthResult first = authResultWithText("");
        AuthResult second = authResultWithText("");
        AuthResult scan = authResultWithText("Unexpected content");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("");
        assertThat(evidenceValue(result, "TemplateDiff.Diff"))
                .isEqualTo("2 unexplained change(s) between template and scan:\n+Unexpected\n+content");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesExactlyMaxObservedWidthFromInsertion() {
        AuthResult first = authResultWithText("Welcome");
        AuthResult second = authResultWithText("Welcome back home safely");
        AuthResult scan = authResultWithText("Welcome now instantly always");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesExactlyMaxObservedWidthFromDeletion() {
        AuthResult first = authResultWithText("Welcome back home safely");
        AuthResult second = authResultWithText("Welcome");
        AuthResult scan = authResultWithText("Welcome now instantly always");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesExactlyMaxObservedWidthFromInsertionWithTrailingStableWord() {
        AuthResult first = authResultWithText("Welcome end");
        AuthResult second = authResultWithText("Welcome back home safely end");
        AuthResult scan = authResultWithText("Welcome now instantly always end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }


    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesExactlyMaxObservedWidthFromDeletionWithTrailingStableWord() {
        AuthResult first = authResultWithText("Welcome back home safely end");
        AuthResult second = authResultWithText("Welcome end");
        AuthResult scan = authResultWithText("Welcome now instantly always end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesMinimumObservedWidthWithTrailingStableWord() {
        AuthResult first = authResultWithText("Welcome back home safely end");
        AuthResult second = authResultWithText("Welcome end");
        AuthResult scan = authResultWithText("Welcome end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }


    @Test
    void evaluateScanResult_isVulnerable_whenScanUsesFewerWordsThanMaxObservedWidthWithTrailingStableWord() {
        AuthResult first = authResultWithText("Welcome back home safely end");
        AuthResult second = authResultWithText("Welcome end");
        AuthResult scan = authResultWithText("Welcome alf end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenScanExceedsMaxObservedWidthByOneWord() {
        AuthResult first = authResultWithText("Welcome");
        AuthResult second = authResultWithText("Welcome back home safely");
        AuthResult scan = authResultWithText("Welcome now instantly always somehow");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Welcome <unstable value>");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("3 unexplained change(s) between template and scan:\n+instantly\n+always\n+somehow");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenScanOmitsMultiWordUnstableValueEntirely() {
        AuthResult first = authResultWithText("Dear John Smith, welcome");
        AuthResult second = authResultWithText("Dear Jane, welcome");
        AuthResult scan = authResultWithText("Dear, welcome");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Dear <unstable value>, welcome");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenMultiplePlaceholdersHaveIndependentWidthBudgets() {
        AuthResult first = authResultWithText("Start A B middle C end");
        AuthResult second = authResultWithText("Start X middle Y Z W end");
        AuthResult scan = authResultWithText("Start Q R middle S T U end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Start <unstable value> middle <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenOnePlaceholderExceedsItsIndependentWidthBudget() {
        AuthResult first = authResultWithText("Start A B middle C end");
        AuthResult second = authResultWithText("Start X middle Y Z W end");
        AuthResult scan = authResultWithText("Start Q R middle S T U V end");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Start <unstable value> middle <unstable value> end");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("4 unexplained change(s) between template and scan:\n+R\n+T\n+U\n+V");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenCoincidentalAnchorWordInsideWildcardRequiresNonGreedyMatch() {
        AuthResult first = authResultWithText("A P Q R S T B N C");
        AuthResult second = authResultWithText("A M B O C");
        AuthResult scan = authResultWithText("A Z B Y B V C");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("A <unstable value> B <unstable value> C");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isVulnerable_whenMultiplePlaceholdersHaveIndependentWidthBudgetsAcrossNewlines() {
        AuthResult first = authResultWithText("Start\nA B\nmiddle\nC\nend");
        AuthResult second = authResultWithText("Start\nX\nmiddle\nY Z W\nend");
        AuthResult scan = authResultWithText("Start\nQ R\nmiddle\nS T U\nend");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.VULNERABLE);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Start\n<unstable value>\nmiddle\n<unstable value>\nend");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("Scan fully matches the expected template. No unexplained differences.");
    }

    @Test
    void evaluateScanResult_isOk_whenOnePlaceholderExceedsItsIndependentWidthBudgetAcrossNewlines() {
        AuthResult first = authResultWithText("Start\nA B\nmiddle\nC\nend");
        AuthResult second = authResultWithText("Start\nX\nmiddle\nY Z W\nend");
        AuthResult scan = authResultWithText("Start\nQ R\nmiddle\nS T U V\nend");

        VerificationResult result = templateDiffVerification.evaluateScanResult(first, second, scan);

        assertThat(result.getStatus()).isEqualTo(ScanResultStatus.OK);
        assertThat(evidenceValue(result, "TemplateDiff.Template")).isEqualTo("Start\n<unstable value>\nmiddle\n<unstable value>\nend");
        assertThat(evidenceValue(result, "TemplateDiff.Diff")).isEqualTo("4 unexplained change(s) between template and scan:\n+R\n+T\n+U\n+V");
    }

    private String evidenceValue(VerificationResult result, String type) {
        return result.getEvidences().stream()
                .filter(e -> e.type().equals(type))
                .findFirst()
                .orElseThrow()
                .value();
    }
}
