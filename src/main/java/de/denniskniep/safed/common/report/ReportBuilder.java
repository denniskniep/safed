package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportBuilder {

    private final ScanResult firstScan;
    private final ScanResult secondScan;
    private ScanResult positiveScan;
    private HashMap<String, ScanResult> scanResults;

    public ReportBuilder(ScanResult firstScan, ScanResult secondScan, ScanResult positiveScan, HashMap<String, ScanResult> scanResults) {
        this.firstScan = firstScan;
        this.secondScan = secondScan;
        this.positiveScan = positiveScan;
        this.scanResults = scanResults;
    }

    public Report Build(){
        var report = new Report();
        report.setFirstScan(asInitialScanReport(firstScan));
        report.setSecondScan(asInitialScanReport(secondScan));

        ScanResultReport positiveScanReport = asScanResultReport(positiveScan);
        positiveScanReport.setStatus(null);
        report.setPositiveScan(positiveScanReport);

        ScanResultStatus status = ScanResultStatus.OK;
        for(var scanResult : scanResults.entrySet()){
            if(scanResult.getValue().getStatus() == ScanResultStatus.OK){
                report.addNoFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
            }else{
                status = ScanResultStatus.VULNERABLE;
                report.addFinding(scanResult.getKey(), asScanResultReport(scanResult.getValue()));
            }
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

        initialScanReport.setVisibleText(scanResult.getAuthResult().getResponsePage().visibleText());

        List<String> trafficLog = new ArrayList<>();
        for(var t : scanResult.getAuthResult().getAuthenticationLog().getTraffic()){
            trafficLog.add(t.asShortLog());
        }
        initialScanReport.setTrafficLog(trafficLog);
        initialScanReport.setCreatedAt(scanResult.getCreatedAtFormatted());
        return initialScanReport;
    }
}
