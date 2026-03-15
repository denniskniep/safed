package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.List;

public class VerificationResult {
    private final List<String> evidences;
    private final ScanResultStatus status;

    public VerificationResult(ScanResultStatus status, List<String> evidences) {
        this.evidences = evidences;
        this.status = status;
    }

    public List<String> getEvidences() {
        return evidences;
    }

    public ScanResultStatus getStatus() {
        return status;
    }
}
