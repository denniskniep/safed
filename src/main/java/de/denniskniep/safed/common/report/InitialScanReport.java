package de.denniskniep.safed.common.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({ "createdAt", "trafficLog"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitialScanReport {
    private String createdAt;
    private List<String> trafficLog;
    private String visibleText;

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

    public String getVisibleText() {
        return visibleText;
    }

    public void setVisibleText(String visibleText) {
        this.visibleText = visibleText;
    }
}
