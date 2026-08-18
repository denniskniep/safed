package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.List;

public class VerificationResult {
    private final List<Evidence> evidences;
    private final ScanResultStatus status;

    public VerificationResult(ScanResultStatus status, List<Evidence> evidences) {
        this.evidences = evidences;
        this.status = status;
    }

    public List<Evidence> getEvidences() {
        return evidences;
    }

    public ScanResultStatus getStatus() {
        return status;
    }
}
