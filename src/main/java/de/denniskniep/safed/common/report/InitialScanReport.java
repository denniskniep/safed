package de.denniskniep.safed.common.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({ "createdAt", "evidences", "trafficLog"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitialScanReport {
    private String createdAt;
    private List<String> evidences;
    private List<String> trafficLog;


    public List<String> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<String> evidences) {
        this.evidences = evidences;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTrafficLog() {
        return trafficLog;
    }

    public void setTrafficLog(List<String> trafficLog) {
        this.trafficLog = trafficLog;
    }
}
