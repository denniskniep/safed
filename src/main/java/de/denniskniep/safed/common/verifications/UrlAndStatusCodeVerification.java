package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UrlAndStatusCodeVerification implements ScanResultVerificationStrategy {

    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        ScanResultStatus status;
        List<String> evidences = new ArrayList<>();

        if(StringUtils.equalsIgnoreCase(firstPositiveAuthResult.getResponsePage().getUrl(), scanAuthResult.getResponsePage().getUrl())
                && firstPositiveAuthResult.getResponsePage().getHttpResponse().getStatus() == scanAuthResult.getResponsePage().getHttpResponse().getStatus()){

            status = ScanResultStatus.VULNERABLE;
            evidences.add("[" + status +"] " + extractUrlAndStatusCode(firstPositiveAuthResult) + " == " + extractUrlAndStatusCode(scanAuthResult));

        }else{
            status = ScanResultStatus.OK;
            evidences.add("[" +status+"] " +extractUrlAndStatusCode(firstPositiveAuthResult) + " != " + extractUrlAndStatusCode(scanAuthResult));

        }

        return new ScanResult(scanAuthResult, status, evidences);
    }

    private String extractUrlAndStatusCode(AuthResult authResult) {
        return authResult.getResponsePage().getUrl() + " -> "+ authResult.getResponsePage().getHttpResponse().getStatus();
    }
}
