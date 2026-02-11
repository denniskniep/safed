package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.utils.Serialization;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"status", "firstScan", "secondScan", "positiveScan", "findings", "noFindings" })
public class Report {

    private InitialScanReport firstScan;
    private InitialScanReport secondScan;
    private ScanResultReport positiveScan;
    private ScanResultStatus status;
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

    public ScanResultReport getPositiveScan() {
        return positiveScan;
    }

    public void setPositiveScan(ScanResultReport positiveScan) {
        this.positiveScan = positiveScan;
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
}
