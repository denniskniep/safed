package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;

import java.util.List;

public interface ScanResultVerificationStrategy {
    public List<String> extractInfos(AuthResult scanAuthResult);
    ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult,AuthResult scanAuthResult);
}
