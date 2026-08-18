package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.ScanResultStatus;

public enum EvidenceStatus {
    OK,
    VULNERABLE,
    INFO,
    ;

    public static EvidenceStatus from(ScanResultStatus status){
        switch (status) {
            case OK -> {
                return EvidenceStatus.OK;
            }
            case VULNERABLE -> {
                return EvidenceStatus.VULNERABLE;
            }
            default -> {
                throw new RuntimeException("Evidences can´t be defined for failed Scans!");
            }
        }
    }
}