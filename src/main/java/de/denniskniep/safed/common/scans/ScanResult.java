package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.report.ReportError;
import de.denniskniep.safed.common.verifications.VerificationResult;
import org.apache.commons.collections4.map.HashedMap;

import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScanResult {
    private final Instant createdAt;
    private final AuthResult authResult;
    private final Map<String, List<String>> infos;
    private final Map<String, VerificationResult> verifications;
    private final ScanResultStatus status;
    private final List<ReportError> errors;

    public ScanResult(AuthResult authResult, Map<String, VerificationResult> verifications, Map<String, List<String>> infos) {
        this(authResult, overallResultOf(verifications), verifications, infos, new ArrayList<>());
    }

    private static ScanResultStatus overallResultOf(Map<String, VerificationResult> verifications) {
        return verifications.entrySet().stream().anyMatch(v -> v.getValue().getStatus() == ScanResultStatus.VULNERABLE) ? ScanResultStatus.VULNERABLE : ScanResultStatus.OK;
    }

    public ScanResult(AuthResult authResult, ScanResultStatus status, Map<String, VerificationResult> verifications, Map<String, List<String>> infos, List<ReportError> errors) {
        if(authResult == null && status != ScanResultStatus.FAILED) {
            throw new RuntimeException("authResult is null, but status is " + status);
        }
        this.createdAt = Instant.now();
        this.authResult = authResult;
        this.verifications = verifications;
        this.infos = infos;
        this.status = status;
        this.errors = errors;
    }

    public static ScanResult ok(AuthResult authResult, Map<String, List<String>> infos) {
        return new ScanResult(authResult, ScanResultStatus.OK, new HashedMap<>(), infos, new ArrayList<>());
    }

    public static ScanResult failed(ScanResult scanResult, List<ReportError> errors) {
        return new ScanResult(scanResult.authResult, ScanResultStatus.FAILED, scanResult.verifications, scanResult.infos, errors);
    }

    public static ScanResult failed(List<ReportError> errors) {
        var authResult = new EmptyAuthResult();
        return new ScanResult(authResult, ScanResultStatus.FAILED, new HashedMap<>(), new HashedMap<>(), errors);
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    public <T extends AuthResult> T getAuthResult(Class<T> clazz) {
        if(authResult == null){
            return null;
        }

        if(!clazz.isInstance(authResult)){
            throw new RuntimeException("AuthResult is not of type " + clazz.getName());
        }
        return (T)authResult;
    }

    public List<String> getEvidences() {
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        for(List<String> info : infos.values()){
            deduplicated.addAll(info);
        }
        for(VerificationResult verificationResult : verifications.values()){
            deduplicated.addAll(verificationResult.getEvidences());
        }
        return new ArrayList<>(deduplicated);
    }

    public ScanResultStatus getStatus() {
        return status;
    }

    public String getCreatedAtFormatted() {
        return new DateTimeFormatterBuilder()
                .appendInstant(3)
                .toFormatter()
                .format(createdAt);
    }

    public List<ReportError> getErrors() {
        return errors;
    }

    public List<String> getVerificationStrategies(ScanResultStatus filter) {
        return verifications
                .entrySet()
                .stream()
                .filter(v -> v.getValue().getStatus() == filter)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
