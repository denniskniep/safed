package de.denniskniep.safed.common.assessment;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.report.ReportBuilder;
import de.denniskniep.safed.common.report.ReportError;
import de.denniskniep.safed.common.scans.*;
import de.denniskniep.safed.common.scans.Scanner;
import de.denniskniep.safed.common.verifications.ScanResultVerificationStrategy;
import de.denniskniep.safed.common.verifications.VerificationResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Assessment<T extends Scanner, C extends AppConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(Assessment.class);

    private final T successScanner;
    private final T failureScanner;

    private Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies;
    private List<T> scanners;

    private ScanResult firstScanSuccess;
    private ScanResult secondScanSuccess;
    private ScanResult isVulnerableScan;
    private ScanResult isOkScan;

    public Assessment(T successScanner, T failureScanner) {
        this.successScanner = successScanner;
        this.failureScanner = failureScanner;
    }

    @Autowired
    public final void setScanner(List<T> scanners) {
        this.scanners = scanners;
    }

    @Autowired
    public final void setScanResultVerificationStrategies(Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies) {
        this.scanResultVerificationStrategies = scanResultVerificationStrategies;
    }

    protected void validate(C config){
        if (config == null) {
            throw new RuntimeException("scannerConfig is null");
        }
    }

    public Report run(String clientId, C scannerConfig) {
        var start = Instant.now();
        validate(scannerConfig);

        List<String> scannersToUse = new ArrayList<>();
        for (var scanner : scanners) {
            if (scannerConfig.getScanners() == null || scannerConfig.getScanners().contains(scanner.getClass().getSimpleName())) {
                scannersToUse.add(scanner.getClass().getSimpleName());
            }
        }

        try {
            return runInternal(clientId, scannersToUse, scannerConfig);
        }catch (Exception e){
            ReportError error = ReportError.from(e);
            LOG.error("ClientId: {}; Status: {}; Finished Assessment ", clientId, ScanResultStatus.FAILED);
            var duration = Duration.between(start, Instant.now());
            var scanResult = scannersToUse.stream().collect(Collectors.toMap(
                    s -> s ,
                    s -> ScanResult.failed(List.of(error))
            ));
            ReportBuilder reportBuilder = new ReportBuilder(clientId, duration.toMillis() , firstScanSuccess, secondScanSuccess, isVulnerableScan, isOkScan, scanResult, new ArrayList<>());
            return reportBuilder.Build();
        }
    }

    private Report runInternal(String clientId, List<String> scannersToUse, C scannerConfig) {
        var start = Instant.now();

        List<ReportError> errors = new ArrayList<>();
        LOG.info("Start first baseline scan");
        // First scan with successful login
        firstScanSuccess = runScan(scannerConfig, successScanner, true);

        LOG.info("Start second baseline scan");
        // Second scan with successful login, but maybe changes in the page content from login to login
        secondScanSuccess = runScan(scannerConfig, successScanner, true);

        LOG.info("Start test scan - proof ok");
        // scan with failure - means OK (not vulnerable)
        isOkScan = runScan(scannerConfig, failureScanner, false);

        // Dynamically adopt Verification Strategy
        List<String> okVerifications = isOkScan.getVerificationStrategies(ScanResultStatus.OK);
        List<String> vulnVerifications = isOkScan.getVerificationStrategies(ScanResultStatus.VULNERABLE);
        if(!vulnVerifications.isEmpty() && !okVerifications.isEmpty()){
            // a significant drift in the response is expected, therefore verifications should report OK
            // if not we remove all verifications that are not reporting OK!
            scannerConfig = (C)scannerConfig.deepCopy();
            scannerConfig.setVerificationStrategies(okVerifications);
            LOG.info("Restart test scan - proof ok (Adopted Verification Strategy)");
            isOkScan = runScan(scannerConfig, failureScanner, false);
        }

        LOG.info("Start test scan - proof vulnerable");
        // Scan with success - means VULNERABLE
        isVulnerableScan = runScan(scannerConfig, successScanner, false);

        // For a malicious scan, we expect a significant drift in the response
        // The successScanner does not have that drift on purpose!
        // As no error occur and the user is normally logged in, we expect that to be classified as VULNERABLE
        if (isVulnerableScan.getStatus() == ScanResultStatus.OK) {
            var msg = "Fourth scan must always be classified as VULNERABLE!";
            LOG.warn(msg);
            errors.add(new ReportError(msg));
        }

        // For a malicious scan, we expect a significant drift in the response
        // The failureScanner should have a significant drift in the response
        // Because an error occur and the user is not logged in, we expect that to be classified as OK
        if (isOkScan.getStatus() == ScanResultStatus.VULNERABLE) {
            var msg = "Third scan must always be classified as OK!";
            LOG.warn(msg);
            errors.add(new ReportError(msg));
        }

        LOG.info("Finished baseline and test scans");

        var scanResults = new HashMap<String, ScanResult>();
        for (var scanner : scanners) {
            if (!scannersToUse.contains(scanner.getClass().getSimpleName())) {
                LOG.info("Skip scanning with {}", scanner.getClass().getSimpleName());
                continue;
            }

            LOG.debug("Start scanning with {}", scanner.getClass().getSimpleName());
            try{
                var scanResult = runScan(scannerConfig, scanner, false);
                scanResults.put(scanner.getClass().getSimpleName(), scanResult);
                LOG.info("ClientId: {}; Status: {}; Scanner: {};", clientId, scanResult.getStatus(), scanner.getClass().getSimpleName());
            }catch(Exception e){
                ReportError error = ReportError.from(e);
                var scanResult = ScanResult.failed(List.of(error));
                LOG.error("ClientId: {}; Status: {}; Scanner: {};", clientId, scanResult.getStatus(), scanner.getClass().getSimpleName());
                scanResults.put(scanner.getClass().getSimpleName(), scanResult);
            }
        }
        var duration = Duration.between(start, Instant.now());
        ReportBuilder reportBuilder = new ReportBuilder(clientId, duration.toMillis() , firstScanSuccess, secondScanSuccess, isVulnerableScan, isOkScan, scanResults, errors);
        var report = reportBuilder.Build();
        if(report.getStatus() == ScanResultStatus.FAILED){
            LOG.error("ClientId: {}; Status: {}; Finished Assessment ", report.getClientId(), report.getStatus());
        }else{
            LOG.info("ClientId: {}; Status: {}; Finished Assessment ", report.getClientId(), report.getStatus());
        }
        return report;
    }

    private ScanResult runScan(C inputScannerConfig, T scanner, boolean isBaselineScan) {
        scanner.init(firstScanSuccess, secondScanSuccess, isVulnerableScan, isOkScan);

        C scannerConfig = (C)scanner.getScannerConfig(inputScannerConfig.deepCopy());

        AuthResult authResult = scan(scannerConfig, scanner, isBaselineScan);

        // All VerificationStrategies are used to gather infos
        var allVerificationStrategies = createVerificationStrategy(scanResultVerificationStrategies.keySet());
        var infos = extractInfos(allVerificationStrategies, authResult);

        if (firstScanSuccess == null || secondScanSuccess == null) {
            return ScanResult.ok(authResult, infos);
        }

        var selectedVerificationStrategies = createVerificationStrategy(scannerConfig.getVerificationStrategies());
        var verifications = evaluate(selectedVerificationStrategies, firstScanSuccess.getAuthResult(), secondScanSuccess.getAuthResult(), authResult);
        return new ScanResult(authResult, verifications, infos);
    }

    private Map<String, List<String>> extractInfos(List<ScanResultVerificationStrategy> verifications, AuthResult scanAuthResult) {
        var infos = new HashMap<String, List<String>>();
        for(var verificationStrategy : verifications){
            infos.put(verificationStrategy.getClass().getSimpleName(), verificationStrategy.extractInfos(scanAuthResult));
        }
        return infos;
    }

    public  Map<String, VerificationResult> evaluate(List<ScanResultVerificationStrategy> verifications, AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult authResult) {
        var results = new HashMap<String, VerificationResult>();
        for(var verificationStrategy : verifications){
            var result = verificationStrategy.evaluateScanResult(firstPositiveAuthResult, secondPositiveAuthResult, authResult);
            results.put(verificationStrategy.getClass().getSimpleName(), result);
        }
        return results;
    }

    protected abstract AuthResult scan(C scannerConfig, T scanner, boolean isBaselineScan);

    private List<ScanResultVerificationStrategy> createVerificationStrategy(Collection<String> verificationStrategyNames) {
        if (verificationStrategyNames == null || verificationStrategyNames.isEmpty()) {
            verificationStrategyNames = scanResultVerificationStrategies.keySet();
        }

       List<ScanResultVerificationStrategy> verificationStrategies = new ArrayList<>();
       for (var name : verificationStrategyNames) {
           verificationStrategies.add(findVerificationStrategyByName(name));
       }

       return verificationStrategies;
    }

    private ScanResultVerificationStrategy findVerificationStrategyByName(String name) {
        String normalizedName = "";
        for (var n : scanResultVerificationStrategies.keySet()) {
            if (StringUtils.equalsIgnoreCase(name, n)) {
                normalizedName = n;
            }
        }

        var verificationStrategy = scanResultVerificationStrategies.get(normalizedName);
        if (verificationStrategy == null) {
            throw new RuntimeException("No verification strategy found for " + name);
        }
        return verificationStrategy;
    }
}