package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentVerification implements ScanResultVerificationStrategy {

    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {

            Integer normalDistance = new LevenshteinDistance(1000).apply(extractVisibleText(firstPositiveAuthResult), extractVisibleText(secondPositiveAuthResult));
            Integer scanDistance = new LevenshteinDistance(1000).apply(extractVisibleText(firstPositiveAuthResult), extractVisibleText(scanAuthResult));

            var deviation = scanDistance - normalDistance;
            var threshold = 5;

            ScanResultStatus status = ScanResultStatus.OK;
            if(deviation <= threshold){
               status = ScanResultStatus.VULNERABLE;
            }
            var evidences = List.of(
                    "[INFO] Content:\n" + extractVisibleText(scanAuthResult),
                    "[" + status + "] Normal distance between successful authentications: "+ normalDistance + " and distance of scan: " + scanDistance + " -> deviation of "+deviation+" (<="+ threshold+" -> vulnerable)"
            );
            return new ScanResult(scanAuthResult, status, evidences);
    }

    private String extractVisibleText(AuthResult authResult){
       return authResult.getResponsePage().visibleText();
    }
}