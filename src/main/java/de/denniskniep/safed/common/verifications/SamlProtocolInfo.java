package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.saml.SamlAuthResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SamlProtocolInfo implements ScanResultVerificationStrategy {

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        if(scanAuthResult instanceof SamlAuthResult samlAuthResult){
            return List.of(
                new Evidence(EvidenceStatus.INFO, "SamlRequest", samlAuthResult.getSamlInitializationResult().getSamlRequestAsBase64()),
                new Evidence(EvidenceStatus.INFO, "SamlResponse", samlAuthResult.getSamlResponseResult().getSamlResponse())
            );
        }
        return List.of();
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, new ArrayList<>());
    }
}