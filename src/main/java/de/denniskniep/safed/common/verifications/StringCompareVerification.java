package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class StringCompareVerification implements ScanResultVerificationStrategy {

    private final String type;

    public StringCompareVerification(String type) {
        this.type = type;
    }

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
            new Evidence(EvidenceStatus.INFO, type, extract(scanAuthResult))
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        ScanResultStatus status;
        String comparator;

        var firstExtract = extract(firstPositiveAuthResult);
        var scanExtract = extract(scanAuthResult);

        if(StringUtils.equalsIgnoreCase(firstExtract, scanExtract)){
            status = ScanResultStatus.VULNERABLE;
            comparator = " == ";
        }else{
            status = ScanResultStatus.OK;
            comparator = " != ";
        }

        return new VerificationResult(status, List.of(
            new Evidence(EvidenceStatus.from(status), type, firstExtract + comparator + scanExtract)
        ));
    }

    protected abstract String extract(AuthResult authResult);
}
