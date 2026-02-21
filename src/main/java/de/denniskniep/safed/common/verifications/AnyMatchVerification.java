package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.ArrayList;
import java.util.List;

public class AnyMatchVerification implements ScanResultVerificationStrategy {

    private final List<ScanResultVerificationStrategy> verifications;

    public AnyMatchVerification(List<ScanResultVerificationStrategy> verifications){
        this.verifications = verifications;
    }

    public List<String> extractInfos(AuthResult scanAuthResult) {
        List<String> infos = new ArrayList<>();
        for(var verificationStrategy : verifications){
            for(var info : verificationStrategy.extractInfos(scanAuthResult)){
                if(!infos.contains(info)){
                    infos.add(info);
                }
            }
        }
        return infos;
    }

    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        List<String> evidences = new ArrayList<>();
        var scanResultStatus =  ScanResultStatus.OK;
        for(var verificationStrategy : verifications){
            ScanResult scanResult = verificationStrategy.evaluateScanResult(firstPositiveAuthResult, secondPositiveAuthResult, scanAuthResult);
            if(scanResult.getStatus() == ScanResultStatus.VULNERABLE){
                scanResultStatus = ScanResultStatus.VULNERABLE;
            }

            for(var newEvidence : scanResult.getEvidences()){
                if(!evidences.contains(newEvidence)){
                    evidences.add(newEvidence);
                }
            }

        }
        return new ScanResult(scanAuthResult, scanResultStatus, evidences);
    }
}
