package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PartsVerification implements ScanResultVerificationStrategy {

    public static final String UNSTABLE_VALUE = "<unstable value>";

    protected abstract String getEvidenceType();

    protected abstract Map<String, String> extractParts(AuthResult authResult);

    protected abstract String asString(Map<String, String> parts);

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
            new Evidence(EvidenceStatus.INFO, getEvidenceType(), asString(extractParts(scanAuthResult)))
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        // Which parts are the same on both successful authentications.
        // Expectation is that a stable part is also present and unchanged on a scan.
        // Parts that legitimately vary between the two positive auths are ignored.
        var firstParts = extractParts(firstPositiveAuthResult);
        var secondParts = extractParts(secondPositiveAuthResult);
        var scanParts = extractParts(scanAuthResult);

        var expectedPartsOnBothSides = intersect(firstParts, secondParts);
        var actualPartsOnBothSides = intersect(firstParts, scanParts);

        var partsDiff = diff(expectedPartsOnBothSides, actualPartsOnBothSides);
        ScanResultStatus status = ScanResultStatus.OK;
        if(!partsDiff.isEmpty()){
            status = ScanResultStatus.VULNERABLE;
        }

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Expected", asString(expectedPartsOnBothSides)),
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Current", asString(actualPartsOnBothSides)),
            new Evidence(EvidenceStatus.from(status), getEvidenceType() + ".Diff", partsDiff.size() + " diff between " + getEvidenceType().toLowerCase() + " was detected. " + String.join(",", partsDiff))
        );
        return new VerificationResult(status, evidences);
    }

    private List<String> diff(Map<String, String> partsA, Map<String, String> partsB) {
        var partsDiff = new ArrayList<String>();

        for (var partName : partsA.keySet()){
            String partAValue = partsA.get(partName);
            String partBValue = partsB.get(partName);

            if(StringUtils.equals(partAValue, partBValue)){
                // Both parts exist and value match!
                continue;
            }

            if(partAValue != null && partBValue != null && StringUtils.equals(partAValue, UNSTABLE_VALUE)){
                // Both parts exist, but it must not match!
                continue;
            }

            if(partBValue == null){
                partsDiff.add("'"+partName + "' does not exist");
                continue;
            }

            if(!StringUtils.equals(partAValue, partBValue)){
                partsDiff.add("The values for '" + partName + "' are not equal");
                continue;
            }

            throw new RuntimeException("Case not handled!");
        }

        for (var partName : partsB.keySet()){
            String partAValue = partsA.get(partName);

            if(partAValue == null){
                partsDiff.add("'"+partName + "' does not exist");
            }
        }

        return partsDiff;
    }

    private Map<String, String> intersect(Map<String, String> partsA, Map<String, String> partsB){
        var parts = new HashMap<String, String>();
        var sortedPartsA = partsA.keySet().stream().sorted().toList();

        for (String partName : sortedPartsA) {
            String partAValue = partsA.get(partName);
            String partBValue = partsB.get(partName);

            if(StringUtils.equals(partAValue, partBValue)){
                // pin the value if equal!
                parts.put(partName, partAValue);
            } else if(partAValue != null && partBValue != null){
                // only expect the name if not equal!
                parts.put(partName, UNSTABLE_VALUE);
            }
        }
        return parts;
    }
}
