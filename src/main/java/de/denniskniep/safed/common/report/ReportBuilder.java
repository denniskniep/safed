package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportBuilder {

    private final String clientId;
    private final long durationInMs;
    private final ScanResult firstScan;
    private final ScanResult secondScan;
    private final ScanResult isVulnerableScan;
    private final ScanResult isOkScan;
    private final HashMap<String, ScanResult> scanResults;
    private final List<String> errors;

    public ReportBuilder(String clientId, long durationInMs, ScanResult firstScan, ScanResult secondScan, ScanResult isVulnerableScan, ScanResult isOkScan, HashMap<String, ScanResult> scanResults, List<String> errors) {
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
            if(scanResult.getValue().getStatus() == ScanResultStatus.OK){
                report.addNoFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
            }else{
                status = ScanResultStatus.VULNERABLE;
                report.addFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
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
        ScanResultReport scanResultReport = new ScanResultReport();
        scanResultReport.setStatus(scanResult.getStatus());
        scanResultReport.setEvidences(scanResult.getEvidences());

        List<String> trafficLog = new ArrayList<>();
        for(var t : scanResult.getAuthResult().getAuthenticationLog().getTraffic()){
            trafficLog.add(t.asShortLog());
        }
        scanResultReport.setTrafficLog(trafficLog);
        scanResultReport.setCreatedAt(scanResult.getCreatedAtFormatted());
        return scanResultReport;
    }

    private InitialScanReport asInitialScanReport(ScanResult scanResult){
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
