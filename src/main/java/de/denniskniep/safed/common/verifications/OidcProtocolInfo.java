package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.oidc.OidcAuthResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OidcProtocolInfo implements ScanResultVerificationStrategy {

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        if(scanAuthResult instanceof OidcAuthResult oidcAuthResult){
            return List.of(
                new Evidence(EvidenceStatus.INFO, "OidcRequest", oidcAuthResult.getOidcRequestData().asRequestUrl()),
                new Evidence(EvidenceStatus.INFO, "OidcResponse.IdToken", redactSignature(oidcAuthResult.getTokenResponse().getIdToken()))
            );
        }
        return List.of();
    }

    private static String redactSignature(String idToken) {
        if(idToken == null){
            return null;
        }
        String[] parts = idToken.split("\\.", -1);
        if(parts.length != 3 || parts[2].isEmpty()){
            return idToken;
        }
        return parts[0] + "." + parts[1] + ".redacted";
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, new ArrayList<>());
    }
}