package de.denniskniep.safed.common.verifications;

import com.github.difflib.DiffUtils;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DiffVerification implements ScanResultVerificationStrategy {

    protected abstract String getUnitName();

    protected abstract List<String> split(AuthResult authResult);

    private String getEvidenceType() {
        return StringUtils.capitalize(getUnitName()) + "Diff";
    }

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return new ArrayList<>();
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        var normalDiff = diff(firstPositiveAuthResult, secondPositiveAuthResult);
        var scanDiff = diff(firstPositiveAuthResult, scanAuthResult);

        // Same diff size alone isn't proof: it could remove totally different content
        // by coincidence. Only trust it if every removed unit is one we already saw
        // vary between the two known-good authentications.
        var unknownRemoved = new HashSet<>(removedTokens(scanDiff));
        unknownRemoved.removeAll(removedTokens(normalDiff));
        boolean onlyKnownRemoved = unknownRemoved.isEmpty();

        ScanResultStatus status = ScanResultStatus.OK;
        if(scanDiff.size() <= normalDiff.size() && onlyKnownRemoved){
            status = ScanResultStatus.VULNERABLE;
        }

        var summary = "Normal diff of " + getUnitName() + "s between successful authentications: " + normalDiff.size()
                + " and " + getUnitName() + " distance of scan: " + scanDiff.size()
                + (onlyKnownRemoved
                    ? "; only known " + getUnitName() + "s removed"
                    : "; unexpected " + getUnitName() + "s removed: " + String.join("\n", unknownRemoved));

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Expected", String.join("\n", normalDiff)),
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Current", String.join("\n", scanDiff)),
            new Evidence(EvidenceStatus.from(status), getEvidenceType() + ".Summary", summary)
        );

        return new VerificationResult(status, evidences);
    }

    private List<String> diff(AuthResult authResultA, AuthResult authResultB){
        var patch = DiffUtils.diff(split(authResultA), split(authResultB));

        var changed = new ArrayList<String>();
        for (var delta : patch.getDeltas()) {
            delta.getSource().getLines().forEach(unit -> changed.add("--" + unit));
            delta.getTarget().getLines().forEach(unit -> changed.add("++" + unit));
        }
        return changed;
    }

    private Set<String> removedTokens(List<String> changed){
        return changed.stream()
                .filter(unit -> unit.startsWith("--"))
                .map(unit -> unit.substring(2))
                .collect(Collectors.toSet());
    }
}
