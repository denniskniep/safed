package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DiffVerification implements ScanResultVerificationStrategy  {

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return new ArrayList<>();
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        var normalDiff = diff(firstPositiveAuthResult, secondPositiveAuthResult);
        var scanDiff = diff(firstPositiveAuthResult, scanAuthResult);

        ScanResultStatus status = ScanResultStatus.OK;
        if(scanDiff.size() <= normalDiff.size()){
            status = ScanResultStatus.VULNERABLE;
        }

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, "Diff.Patch", String.join("\n", scanDiff)),
            new Evidence(EvidenceStatus.from(status), "Diff.Summary","Normal diff lines between successful authentications: "+ normalDiff.size() + " and line distance of scan: " + scanDiff.size())
        );

        return new VerificationResult(status, evidences);
    }

    private List<String> diff(AuthResult authResultA, AuthResult authResultB){
        var patch = DiffUtils.diff(
                Arrays.asList(authResultA.extractVisibleText().split("\n")),
                Arrays.asList(authResultB.extractVisibleText().split("\n"))
        );

        return UnifiedDiffUtils.generateUnifiedDiff(
                null,
                null,
                Arrays.stream(authResultA.extractVisibleText().split("\n")).toList(),
                patch,
                0).stream().skip(3).toList();
    }
}
