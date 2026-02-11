package de.denniskniep.safed.common.scans;

import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class ScanResult {
    private final Instant createdAt;
    private final AuthResult authResult;
    private final List<String> evidences;
    private final ScanResultStatus status;

    public ScanResult(AuthResult authResult, ScanResultStatus status, List<String> evidences) {
        this.createdAt = Instant.now();
        this.authResult = authResult;
        this.evidences = evidences;
        this.status = status;
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    public <T extends AuthResult> T getAuthResult(Class<T> clazz) {
        if(!clazz.isInstance(authResult)){
            throw new RuntimeException("AuthResult is not of type " + clazz.getName());
        }
        return (T)authResult;
    }

    public List<String> getEvidences() {
        return evidences;
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
}
