package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TitleInfo implements ScanResultVerificationStrategy {

    @Override
    public List<String> extractInfos(AuthResult scanAuthResult) {
        if(scanAuthResult.getResponsePage().base64Screenshot() == null){
            return new ArrayList<>();
        }

        return List.of(
                "[INFO] Title: " + scanAuthResult.getResponsePage().title()
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, List.of());
    }
}
