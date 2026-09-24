package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CapturedRequestUrlInfo implements ScanResultVerificationStrategy {

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
            new Evidence(EvidenceStatus.INFO, "IdpInit.CapturedRequestUrl", scanAuthResult.getRequestPage().isPresent() ? scanAuthResult.getRequestPage().get().capturedHttpRequest().getUrl() : ""),
            new Evidence(EvidenceStatus.INFO, "IdpResponse.CapturedRequestUrl", scanAuthResult.getResponsePage().capturedHttpRequest().getUrl())
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, new ArrayList<>());
    }
}
