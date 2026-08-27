package de.denniskniep.safed.common.verifications;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

@Service
public class TemplateDiffVerification implements ScanResultVerificationStrategy {

    public static final String UNSTABLE_VALUE = "<unstable value>";

    private static final Pattern UNIT = Pattern.compile("\\w+|\n|[^\\w\\s]");

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return new ArrayList<>();
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        var firstUnits = split(firstPositiveAuthResult);
        var secondUnits = split(secondPositiveAuthResult);
        var patch = DiffUtils.diff(firstUnits, secondUnits);
        var template = buildTemplate(firstUnits, patch);
        var widths = patch.getDeltas().stream()
            .map(delta -> Math.max(delta.getSource().size(), delta.getTarget().size()))
            .toList();
        var scanUnits = split(scanAuthResult);

        boolean matches = matchesTemplate(template, widths, scanUnits);
        var mismatches = matches ? List.<String>of() : diffAgainstTemplate(template, scanUnits);

        ScanResultStatus status = mismatches.isEmpty() ? ScanResultStatus.VULNERABLE : ScanResultStatus.OK;

        var summary = mismatches.isEmpty()
            ? "Scan fully matches the expected template. No unexplained differences."
            : mismatches.size() + " unexplained change(s) between template and scan:\n" + String.join("\n", mismatches);

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, "TemplateDiff.Template", join(template)),
            new Evidence(EvidenceStatus.INFO, "TemplateDiff.Current", join(scanUnits)),
            new Evidence(EvidenceStatus.from(status), "TemplateDiff.Diff", summary)
        );

        return new VerificationResult(status, evidences);
    }

    // Words, punctuation and newlines are kept as separate units, so a word changing doesn't drag
    // its trailing punctuation into the unstable region, and the template preserves line structure.
    private List<String> split(AuthResult authResult) {
        var matcher = UNIT.matcher(authResult.extractVisibleText());
        var units = new ArrayList<String>();
        while (matcher.find()) {
            units.add(matcher.group());
        }
        return units;
    }

    private static final Pattern PUNCTUATION = Pattern.compile("[^\\w\\s]");

    private String join(List<String> units) {
        var text = new StringBuilder();
        for (int i = 0; i < units.size(); i++) {
            text.append(units.get(i));
            boolean lastUnit = i == units.size() - 1;
            if (lastUnit) {
                continue;
            }
            var current = units.get(i);
            var next = units.get(i + 1);
            boolean noSpace = current.equals("\n") || next.equals("\n") || PUNCTUATION.matcher(next).matches();
            if (!noSpace) {
                text.append(" ");
            }
        }
        return text.toString();
    }

    // Collapses every unit that varies between the two baselines into UNSTABLE_VALUE,
    // leaving a reference sequence where only genuinely stable content remains literal.
    private List<String> buildTemplate(List<String> firstUnits, Patch<String> patch) {
        var template = new ArrayList<>(firstUnits);

        var deltas = new ArrayList<>(patch.getDeltas());
        deltas.sort(Comparator.comparingInt((AbstractDelta<String> delta) -> delta.getSource().getPosition()).reversed());

        // Applied back-to-front so collapsing one delta doesn't shift the positions of the others.
        for (var delta : deltas) {
            int start = delta.getSource().getPosition();
            int end = start + delta.getSource().size();
            template.subList(start, end).clear();
            template.add(start, UNSTABLE_VALUE);
        }
        return template;
    }

    // Splits the template into the literal runs that sit between/around its UNSTABLE_VALUE
    // placeholders, e.g. ["Dear"] UNSTABLE [","," welcome"] for "Dear <unstable value>, welcome".
    private List<List<String>> segmentByPlaceholder(List<String> template) {
        var segments = new ArrayList<List<String>>();
        var current = new ArrayList<String>();
        for (var unit : template) {
            if (unit.equals(UNSTABLE_VALUE)) {
                segments.add(current);
                current = new ArrayList<>();
            } else {
                current.add(unit);
            }
        }
        segments.add(current);
        return segments;
    }

    private boolean startsWith(List<String> list, int pos, List<String> sub) {
        return pos >= 0 && pos + sub.size() <= list.size() && list.subList(pos, pos + sub.size()).equals(sub);
    }

    // Checks whether the scan matches the template, letting each UNSTABLE_VALUE placeholder
    // absorb as many scan tokens as the widest difference ever seen between the two baselines
    // at that position (widths, one per placeholder, in left-to-right order) - not just one.
    // Tracks every reachable scan position per stage (not just the leftmost) so a literal
    // anchor word that happens to also appear inside a placeholder's own content doesn't
    // cause a valid alignment further right to be missed.
    private boolean matchesTemplate(List<String> template, List<Integer> widths, List<String> scanUnits) {
        var segments = segmentByPlaceholder(template);
        if (segments.size() == 1) {
            return segments.get(0).equals(scanUnits);
        }

        var first = segments.get(0);
        if (!startsWith(scanUnits, 0, first)) {
            return false;
        }
        var reachable = new HashSet<Integer>();
        reachable.add(first.size());

        for (int i = 1; i < segments.size() - 1; i++) {
            var segment = segments.get(i);
            int maxWidth = widths.get(i - 1);
            var next = new HashSet<Integer>();
            for (int pos : reachable) {
                for (int offset = 0; offset <= maxWidth; offset++) {
                    if (startsWith(scanUnits, pos + offset, segment)) {
                        next.add(pos + offset + segment.size());
                    }
                }
            }
            if (next.isEmpty()) {
                return false;
            }
            reachable = next;
        }

        var last = segments.get(segments.size() - 1);
        int maxWidth = widths.get(widths.size() - 1);
        int lastStart = scanUnits.size() - last.size();
        if (!startsWith(scanUnits, lastStart, last)) {
            return false;
        }
        return reachable.stream().anyMatch(pos -> lastStart - pos >= 0 && lastStart - pos <= maxWidth);
    }

    // Diffs the scan against the template using an equalizer where UNSTABLE_VALUE matches
    // anything, so only genuine, unexplained differences survive into the result.
    private List<String> diffAgainstTemplate(List<String> template, List<String> scanUnits) {
        BiPredicate<String, String> equalizer = (templateUnit, scanUnit) ->
            StringUtils.equals(templateUnit, scanUnit) || StringUtils.equals(templateUnit, UNSTABLE_VALUE);

        var patch = DiffUtils.diff(template, scanUnits, equalizer);

        var mismatches = new ArrayList<String>();
        for (var delta : patch.getDeltas()) {
            delta.getSource().getLines().forEach(unit -> mismatches.add("-" + unit));
            delta.getTarget().getLines().forEach(unit -> mismatches.add("+" + unit));
        }
        return mismatches;
    }
}
