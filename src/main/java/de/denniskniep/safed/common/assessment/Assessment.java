package de.denniskniep.safed.common.assessment;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.report.ReportBuilder;
import de.denniskniep.safed.common.scans.*;
import de.denniskniep.safed.common.scans.Scanner;
import de.denniskniep.safed.common.verifications.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class Assessment<T extends Scanner, C extends ClientConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(Assessment.class);

    private final T successScanner;
    private final T failureScanner;

    private Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies;
    private List<T> scanners;

    private ScanResult firstScanSuccess;
    private ScanResult secondScanSuccess;
    private ScanResult thirdScanSuccess;
    private ScanResult fourthScanFailure;

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

    public Report run(IssuerConfig issuerConfig, C clientConfig) {
        List<String> errors = new ArrayList<>();
        LOG.info("Start initial tests");
        // First scan with successful login
        firstScanSuccess = runScan(issuerConfig, clientConfig, successScanner);
        // Second scan with successful login, but maybe changes in the page content from login to login
        secondScanSuccess = runScan(issuerConfig, clientConfig, successScanner);
        // Third scan with successful login - means VULNERABLE
        thirdScanSuccess = runScan(issuerConfig, clientConfig, successScanner);
        // Fourth scan with failed login - means OK
        fourthScanFailure = runScan(issuerConfig, clientConfig, failureScanner);


        // For a manipulated OIDCResponse we expect a significant drift in the response
        // The third scan with a successful login does not have that drift on purpose!
        // As no error occur and the user is normally logged in, we expect that to be classified as VULNERABLE
        if (thirdScanSuccess.getStatus() == ScanResultStatus.OK) {
            errors.add("Third scan must always be classified as VULNERABLE!");
        }

        // The fourth scan with a failing login should have a significant drift in the response
        // Because an error occur and the user is not logged in, we expect that to be classified as OK
        if (fourthScanFailure.getStatus() == ScanResultStatus.VULNERABLE) {
            // For a manipulated OIDCResponse we expect a significant drift in the response
            // The third scan with positive login does not have that on purpose!
            errors.add("Fourth scan must always be classified as OK!");
        }


        LOG.info("End initial tests");
        var scanResults = new HashMap<String, ScanResult>();
        for (var scanner : scanners) {
            if (clientConfig.getScanners() != null && !clientConfig.getScanners().contains(scanner.getClass().getSimpleName())) {
                LOG.info("Skip scanning with {}", scanner.getClass().getSimpleName());
                continue;
            }

            LOG.info("Start scanning with {}", scanner.getClass().getSimpleName());
            var scanResult = runScan(issuerConfig, clientConfig, scanner);
            scanResults.put(scanner.getClass().getSimpleName(), scanResult);
            LOG.trace("Scanner {} finished with status: {}.\nFollowing evidences collected:\n{}", scanner.getClass().getSimpleName(), scanResult.getStatus(), String.join("\n", scanResult.getEvidences()));
        }

        ReportBuilder reportBuilder = new ReportBuilder(firstScanSuccess, secondScanSuccess, thirdScanSuccess, fourthScanFailure, scanResults, errors);
        return reportBuilder.Build();
    }

    private ScanResult runScan(IssuerConfig inputIssuerConfig, C inputClientConfig, T scanner) {
        scanner.init(firstScanSuccess, secondScanSuccess, thirdScanSuccess, fourthScanFailure);

        IssuerConfig issuerConfig = scanner.getIssuerConfig(inputIssuerConfig.deepCopy());
        C clientConfig = (C)scanner.getClientConfig(inputClientConfig.deepCopy());

        AuthResult authResult = scan(issuerConfig, clientConfig, scanner);

        // All VerificationStrategies are used to gather infos
        var allVerificationStrategies = createVerificationStrategy(scanResultVerificationStrategies.keySet());
        List<String> infos = allVerificationStrategies.extractInfos(authResult);

        if (firstScanSuccess == null || secondScanSuccess == null) {
            return new ScanResult(authResult, ScanResultStatus.OK, infos);
        }

        var selectedVerificationStrategies = createVerificationStrategy(clientConfig.getVerificationStrategies());
        var scanResult = selectedVerificationStrategies.evaluateScanResult(firstScanSuccess.getAuthResult(), secondScanSuccess.getAuthResult(), authResult);
        var infosAndEvidences = new ArrayList<>(infos);
        infosAndEvidences.addAll(scanResult.getEvidences());
        return new ScanResult(scanResult.getAuthResult(), scanResult.getStatus(), infosAndEvidences);
    }

    protected abstract AuthResult scan(IssuerConfig oidcIssuerConfig, C clientConfig, T scanner);

    private ScanResultVerificationStrategy createVerificationStrategy(Collection<String> verificationStrategyNames) {
        if (verificationStrategyNames == null || verificationStrategyNames.isEmpty()) {
            verificationStrategyNames = List.of(
                    DiffVerification.class.getSimpleName(),
                    UrlAndStatusCodeVerification.class.getSimpleName(),
                    CookieVerification.class.getSimpleName()
            );
        }

       List<ScanResultVerificationStrategy> verificationStrategies = new ArrayList<>();
       for (var name : verificationStrategyNames) {
           verificationStrategies.add(findVerificationStrategyByName(name));
       }

       return new AnyMatchVerification(verificationStrategies);

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