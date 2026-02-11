package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DiffVerification implements ScanResultVerificationStrategy  {
    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {

            var normalPatch = DiffUtils.diff(
                    Arrays.asList(extractVisibleText(firstPositiveAuthResult).split("\n")),
                    Arrays.asList(extractVisibleText(secondPositiveAuthResult).split("\n"))
            );

            List<String> normalDiff = UnifiedDiffUtils.generateUnifiedDiff(
                    null,
                    null,
                    Arrays.stream(extractVisibleText(firstPositiveAuthResult).split("\n")).toList(),
                    normalPatch,
                    0).stream().skip(3).toList();

            var scanPatch = DiffUtils.diff(
                    Arrays.asList(extractVisibleText(firstPositiveAuthResult).split("\n")),
                    Arrays.asList(extractVisibleText(scanAuthResult).split("\n"))
            );

            List<String> scanDiff = UnifiedDiffUtils.generateUnifiedDiff(
                    null,
                    null,
                    Arrays.stream(extractVisibleText(firstPositiveAuthResult).split("\n")).toList(),
                    scanPatch,
                    0).stream().skip(3).toList();;


            ScanResultStatus status = ScanResultStatus.OK;
            if(scanDiff.size() <= normalDiff.size()){
                status = ScanResultStatus.VULNERABLE;
            }
            var evidences = List.of(
                    "[INFO] Diff: \n" + String.join("\n", scanDiff),
                    "["+status + "] Normal diff lines between successful authentications: "+ normalDiff.size() + " and line distance of scan: " + scanDiff.size()
            );

            return new ScanResult(scanAuthResult, status, evidences);

    }


    private static int countChars(List<String> strings) {
        int count = 0;
        for (String str : strings) {
            if (str != null) {
                count += str.length();
            }
        }
        return count;
    }

    private String extractVisibleText(AuthResult authResult){
        return authResult.getResponsePage().getVisibleText();
    }
}
