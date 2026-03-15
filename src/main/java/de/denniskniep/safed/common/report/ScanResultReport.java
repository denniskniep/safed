package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.scans.ScanResultStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({ "createdAt", "status", "errors", "evidences", "trafficLog"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanResultReport {

    private String createdAt;
    private List<String> trafficLog;
    private List<String> evidences;
    private List<String> errors;
    private ScanResultStatus status;

    public List<String> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<String> evidences) {
        this.evidences = evidences;
    }

    public ScanResultStatus getStatus() {
        return status;
    }

    public void setStatus(ScanResultStatus status) {
        this.status = status;
    }

    public List<String> getTrafficLog() {
        return trafficLog;
    }

    public void setTrafficLog(List<String> trafficLog) {
        this.trafficLog = trafficLog;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
