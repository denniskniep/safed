package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.common.utils.Serialization;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"clientId", "durationInMs", "status", "errors", "firstScan", "secondScan", "isVulnerableTestScan", "isOkTestScan", "findings", "noFindings" })
public class Report {

    private String clientId;
    private long durationInMs;
    private InitialScanReport firstScan;
    private InitialScanReport secondScan;
    private ScanResultReport isVulnerableTestScan;
    private ScanResultReport isOkTestScan;
    private ScanResultStatus status;
    private List<ReportError> errors = new ArrayList<>();
    private final Map<String, ScanResultReport> noFindings = new HashMap<>();
    private final Map<String, ScanResultReport> findings = new HashMap<>();


    public InitialScanReport getFirstScan() {
        return firstScan;
    }

    public void setFirstScan(InitialScanReport firstScan) {
        this.firstScan = firstScan;
    }

    public InitialScanReport getSecondScan() {
        return secondScan;
    }

    public void setSecondScan(InitialScanReport secondScan) {
        this.secondScan = secondScan;
    }

    public ScanResultReport getIsVulnerableTestScan() {
        return isVulnerableTestScan;
    }

    public void setIsVulnerableTestScan(ScanResultReport isVulnerableTestScan) {
        this.isVulnerableTestScan = isVulnerableTestScan;
    }

    public ScanResultReport getIsOkTestScan() {
        return isOkTestScan;
    }

    public void setIsOkTestScan(ScanResultReport isOkTestScan) {
        this.isOkTestScan = isOkTestScan;
    }

    public ScanResultStatus getStatus() {
        return status;
    }

    public void setStatus(ScanResultStatus status) {
        this.status = status;
    }

    public Map<String, ScanResultReport> getNoFindings() {
        return noFindings;
    }

    public void addNoFinding(String key, ScanResultReport noFinding) {
        this.noFindings.put(key, noFinding);
    }

    public Map<String, ScanResultReport> getFindings() {
        return findings;
    }

    public void addFinding(String key, ScanResultReport finding) {
        this.findings.put(key, finding);
    }

    public String asJson() {
        return Serialization.AsPrettyJson(this);
    }

    public void setErrors(List<ReportError> errors) {
        this.errors = errors;
    }
    public List<ReportError> getErrors() {
        return errors;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getDurationInMs() {
        return durationInMs;
    }

    public void setDurationInMs(long durationInMs) {
        this.durationInMs = durationInMs;
    }
}
