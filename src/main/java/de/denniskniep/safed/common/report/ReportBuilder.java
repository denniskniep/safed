package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportBuilder {

    private final String clientId;
    private final long durationInMs;
    private final ScanResult firstScan;
    private final ScanResult secondScan;
    private final ScanResult isVulnerableScan;
    private final ScanResult isOkScan;
    private final Map<String, ScanResult> scanResults;
    private final List<ReportError> errors;

    public ReportBuilder(String clientId, long durationInMs, ScanResult firstScan, ScanResult secondScan, ScanResult isVulnerableScan, ScanResult isOkScan, Map<String, ScanResult> scanResults, List<ReportError> errors) {
        this.clientId = clientId;
        this.durationInMs = durationInMs;
        this.firstScan = firstScan;
        this.secondScan = secondScan;
        this.isVulnerableScan = isVulnerableScan;
        this.isOkScan = isOkScan;
        this.scanResults = scanResults;
        this.errors = errors;
    }

    public Report Build(){
        var report = new Report();
        report.setClientId(clientId);
        report.setDurationInMs(durationInMs);

        report.setFirstScan(asInitialScanReport(firstScan));
        report.setSecondScan(asInitialScanReport(secondScan));

        ScanResultReport isVulnerableScanReport = asScanResultReport(isVulnerableScan);
        report.setIsVulnerableTestScan(isVulnerableScanReport);

        ScanResultReport isOkScanReport = asScanResultReport(isOkScan);
        report.setIsOkTestScan(isOkScanReport);

        ScanResultStatus status = ScanResultStatus.OK;
        for(var scanResult : scanResults.entrySet()){
            if(scanResult.getValue().getStatus() == ScanResultStatus.OK || scanResult.getValue().getStatus() == ScanResultStatus.FAILED){
                report.addNoFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
            }else{
                status = ScanResultStatus.VULNERABLE;
                report.addFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
            }
            // metadata of error will be available in scan
            if(!scanResult.getValue().getErrors().isEmpty()){
                errors.addAll(scanResult.getValue().getErrors().stream().map(r ->  new ReportError( "[Scanner:"+scanResult.getKey()+"] " + r.getMessage(), new HashMap<>())).toList());
            }
        }
        if(!errors.isEmpty()){
            status = ScanResultStatus.FAILED;
            report.setErrors(errors);
        }
        report.setStatus(status);
        return report;
    }

    private ScanResultReport asScanResultReport(ScanResult scanResult){
        if(scanResult == null){
            return null;
        }
        ScanResultReport scanResultReport = new ScanResultReport();
        scanResultReport.setStatus(scanResult.getStatus());
        scanResultReport.setEvidences(scanResult.getEvidences());

        List<String> trafficLog = new ArrayList<>();
        for(var t : scanResult.getAuthResult().getAuthenticationLog().getTraffic()){
            trafficLog.add(t.asShortLog());
        }
        scanResultReport.setTrafficLog(trafficLog);
        scanResultReport.setCreatedAt(scanResult.getCreatedAtFormatted());
        scanResultReport.setErrors(scanResult.getErrors());
        return scanResultReport;
    }

    private InitialScanReport asInitialScanReport(ScanResult scanResult){
        if(scanResult == null){
            return null;
        }

        InitialScanReport initialScanReport = new InitialScanReport();

        List<String> trafficLog = new ArrayList<>();
        for(var t : scanResult.getAuthResult().getAuthenticationLog().getTraffic()){
            trafficLog.add(t.asShortLog());
        }
        initialScanReport.setTrafficLog(trafficLog);
        initialScanReport.setEvidences(scanResult.getEvidences());
        initialScanReport.setCreatedAt(scanResult.getCreatedAtFormatted());
        return initialScanReport;
    }
}
