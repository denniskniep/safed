package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VisibleTextInfo implements ScanResultVerificationStrategy {

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
                new Evidence(EvidenceStatus.INFO, "VisibleText", scanAuthResult.extractVisibleText())
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, new ArrayList<>());
    }
}
