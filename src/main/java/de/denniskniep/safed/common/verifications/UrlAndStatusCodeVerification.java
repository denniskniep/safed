package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrlAndStatusCodeVerification implements ScanResultVerificationStrategy {

    @Override
    public List<String> extractInfos(AuthResult scanAuthResult) {
        return List.of(
                "[INFO] " + extractUrlAndStatusCode(scanAuthResult)
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        ScanResultStatus status;
        String comparator;

        var firstPositiveAuthResultExtract = extractUrlAndStatusCode(firstPositiveAuthResult);
        var scanAuthResultExtract = extractUrlAndStatusCode(scanAuthResult);

        if(StringUtils.equalsIgnoreCase(firstPositiveAuthResultExtract, scanAuthResultExtract)){
            status = ScanResultStatus.VULNERABLE;
            comparator = " == ";
        }else{
            status = ScanResultStatus.OK;
            comparator = " != ";
        }

        return new VerificationResult(status, List.of("[" + status +"] " + firstPositiveAuthResultExtract + comparator + scanAuthResultExtract));
    }

    private String extractUrlAndStatusCode(AuthResult authResult) {
        if(authResult.getResponsePage() == null){
            return "";
        }

        if(authResult.getResponsePage().capturedHttpResponse() == null){
            return authResult.getResponsePage().url() + " -> <null>";
        }

        return authResult.getResponsePage().url() + " -> "+ authResult.getResponsePage().capturedHttpResponse().getStatus();
    }
}
