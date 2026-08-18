package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UrlVerification implements ScanResultVerificationStrategy {

    public static final String UNSTABLE_VALUE = "<unstable value>";

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
            new Evidence(EvidenceStatus.INFO, "Url.Parts", asString(getUrlParts(scanAuthResult)))
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        // Which parts of the Url are the same on both successful authentications.
        // Expectation is that a stable part (e.g. path) is also present and unchanged on a scan.
        // Parts that legitimately vary between the two positive auths (e.g. a one-time query token) are ignored.
        var firstParts = getUrlParts(firstPositiveAuthResult);
        var secondParts = getUrlParts(secondPositiveAuthResult);
        var scanParts = getUrlParts(scanAuthResult);

        var expectedPartsOnBothSides = intersectedParts(firstParts, secondParts);
        var actualPartsOnBothSides = intersectedParts(firstParts, scanParts);

        var partsDiff = diff(expectedPartsOnBothSides, actualPartsOnBothSides);
        ScanResultStatus status = ScanResultStatus.OK;
        if(!partsDiff.isEmpty()){
            status = ScanResultStatus.VULNERABLE;
        }

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, "Url.Expected", asString(expectedPartsOnBothSides)),
            new Evidence(EvidenceStatus.INFO, "Url.Current", asString(actualPartsOnBothSides)),
            new Evidence(EvidenceStatus.from(status), "Url.Diff", partsDiff.size() + " diff between url parts was detected. "+ String.join(",", partsDiff))
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

    private String asString(Map<String, String> parts){
        var url = new StringBuilder();

        if(parts.containsKey("Scheme")){
            url.append(parts.get("Scheme")).append("://");
        }
        if(parts.containsKey("Host")){
            url.append(parts.get("Host"));
        }
        if(parts.containsKey("Port")){
            url.append(":").append(parts.get("Port"));
        }

        url.append(pathAsString(parts));

        var query = queryAsString(parts);
        if(!query.isEmpty()){
            url.append("?").append(query);
        }

        if(parts.containsKey("Fragment")){
            url.append("#").append(parts.get("Fragment"));
        }

        return url.toString();
    }

    private String pathAsString(Map<String, String> parts){
        var segments = parts.keySet().stream()
                .filter(k -> k.startsWith("Path."))
                .sorted(Comparator.comparingInt(k -> Integer.parseInt(k.substring("Path.".length()))))
                .map(parts::get)
                .toList();

        if(segments.isEmpty()){
            return "";
        }
        return "/" + String.join("/", segments);
    }

    private String queryAsString(Map<String, String> parts){
        return parts.keySet().stream()
                .filter(k -> k.startsWith("Query."))
                .sorted()
                .map(k -> k.substring("Query.".length()) + "=" + parts.get(k))
                .collect(Collectors.joining("&"));
    }

    private Map<String, String> intersectedParts(Map<String, String> partsA, Map<String, String> partsB){
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

    private Map<String, String> getUrlParts(AuthResult authResult){
        var parts = new HashMap<String, String>();
        if(authResult.getResponsePage() == null){
            return parts;
        }

        var url = authResult.getResponsePage().url();
        if(StringUtils.isBlank(url)){
            return parts;
        }

        UriComponents components;
        try {
            components = UriComponentsBuilder.fromUriString(url).build();
        } catch (IllegalArgumentException e) {
            return parts;
        }

        putIfPresent(parts, "Scheme", components.getScheme());
        putIfPresent(parts, "Host", components.getHost());
        if(components.getPort() != -1){
            parts.put("Port", String.valueOf(components.getPort()));
        }
        putPathSegments(parts, components.getPath());
        putIfPresent(parts, "Fragment", components.getFragment());
        putQueryParams(parts, components);

        return parts;
    }

    private void putIfPresent(Map<String, String> parts, String name, String value){
        if(value != null){
            parts.put(name, value);
        }
    }

    private void putPathSegments(Map<String, String> parts, String path){
        if(StringUtils.isBlank(path)){
            return;
        }

        int index = 0;
        for (String segment : path.split("/")){
            if(segment.isEmpty()){
                continue;
            }
            parts.put("Path." + index, segment);
            index++;
        }
    }

    private void putQueryParams(Map<String, String> parts, UriComponents components){
        components.getQueryParams().forEach((name, values) ->
            parts.put("Query." + name, String.join(",", values))
        );
    }
}
