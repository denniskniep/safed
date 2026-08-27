package de.denniskniep.safed.common.verifications;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
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

    protected String formatDiff(List<String> unitsA, Patch<String> patch) {
        return String.join("\n", changedUnits(patch));
    }

    private String getEvidenceType() {
        return StringUtils.capitalize(getUnitName()) + "Diff";
    }

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return new ArrayList<>();
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        var firstUnits = split(firstPositiveAuthResult);
        var normalPatch = DiffUtils.diff(firstUnits, split(secondPositiveAuthResult));
        var scanPatch = DiffUtils.diff(firstUnits, split(scanAuthResult));

        var normalChanged = changedUnits(normalPatch);
        var scanChanged = changedUnits(scanPatch);

        // Same diff size alone isn't proof: it could remove totally different content
        // by coincidence. Only trust it if every removed unit is one we already saw
        // vary between the two known-good authentications.
        var unknownRemoved = new HashSet<>(removedTokens(scanChanged));
        unknownRemoved.removeAll(removedTokens(normalChanged));
        boolean onlyKnownRemoved = unknownRemoved.isEmpty();

        ScanResultStatus status = ScanResultStatus.OK;
        if(scanChanged.size() <= normalChanged.size() && onlyKnownRemoved){
            status = ScanResultStatus.VULNERABLE;
        }

        var summary = "Normal diff of " + getUnitName() + "s between successful authentications: " + normalChanged.size()
                + " and " + getUnitName() + " distance of scan: " + scanChanged.size()
                + (onlyKnownRemoved
                    ? "; only known " + getUnitName() + "s removed"
                    : "; unexpected removed " + getUnitName() + "s: " + String.join("\n", unknownRemoved));

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Expected", formatDiff(firstUnits, normalPatch)),
            new Evidence(EvidenceStatus.INFO, getEvidenceType() + ".Current", formatDiff(firstUnits, scanPatch)),
            new Evidence(EvidenceStatus.from(status), getEvidenceType() + ".Summary", summary)
        );

        return new VerificationResult(status, evidences);
    }

    private List<String> changedUnits(Patch<String> patch){
        var changed = new ArrayList<String>();
        for (var delta : patch.getDeltas()) {
            delta.getSource().getLines().forEach(unit -> changed.add("-" + unit));
            delta.getTarget().getLines().forEach(unit -> changed.add("+" + unit));
        }
        return changed;
    }

    private Set<String> removedTokens(List<String> changed){
        return changed.stream()
                .filter(unit -> unit.startsWith("-"))
                .map(unit -> unit.substring(1))
                .collect(Collectors.toSet());
    }
}
