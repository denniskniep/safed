package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.common.verifications.ScanResultVerificationStrategy;
import de.denniskniep.safed.saml.auth.SamlAuthenticationFlow;
import de.denniskniep.safed.saml.auth.SamlInitializationResult;
import de.denniskniep.safed.saml.auth.SamlResponseBuilder;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.data.SamlAuthData;
import de.denniskniep.safed.saml.data.SamlRequestData;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.report.ReportBuilder;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import de.denniskniep.safed.saml.scans.SamlScanner;
import de.denniskniep.safed.saml.scans.scanner.SamlBaseScanner;
import org.apache.commons.lang3.StringUtils;
import de.denniskniep.safed.common.scans.Page;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SamlAssessment {

    private static final Logger LOG = LoggerFactory.getLogger(SamlAssessment.class);

    private final Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies;
    private final List<SamlScanner> scanners;

    private ScanResult firstPositiveScan;
    private ScanResult secondPositiveScan;
    private ScanResult thirdPositiveScan;

    @Autowired
    public SamlAssessment(Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies, List<SamlScanner> scanners) {
        this.scanResultVerificationStrategies = scanResultVerificationStrategies;
        this.scanners = scanners;
    }

    public Report run(IssuerConfig issuerConfig, SamlClientConfig samlClientConfig) {
        LOG.info("Start initial tests");

        LOG.debug("First scan");
        // First scan with positive login
        firstPositiveScan = scan(issuerConfig, samlClientConfig, new SamlBaseScanner());

        LOG.debug("Second scan");
        // Second scan with positive login, but maybe anyhow changes in the page content
        secondPositiveScan = scan(issuerConfig, samlClientConfig, new SamlBaseScanner());

        LOG.debug("Third scan");
        // Third scan with positive login
        thirdPositiveScan = scan(issuerConfig, samlClientConfig, new SamlBaseScanner());

        // ToDo: Add another initial scan that forces an error as an empty SAMLResponse is sent
        //forceFailureScan = scan(clientId, new BaseScanner());
        LOG.info("End initial tests");

        if(thirdPositiveScan.getStatus() != ScanResultStatus.VULNERABLE) {
            // For a manipulated SAMLResponse we expect a significant drift in the response
            // The third scan with positive login does not have that on purpose!
            throw new RuntimeException("Third positive scan must always be classified as VULNERABLE!");
        }

        var scanResults = new HashMap<String, ScanResult>();
        for (var scanner : scanners){
            if(samlClientConfig.getScanners() != null && !samlClientConfig.getScanners().contains(scanner.getClass().getSimpleName())) {
                LOG.info("Skip scanning with {}", scanner.getClass().getSimpleName());
                continue;
            }

            LOG.info("Start scanning with {}", scanner.getClass().getSimpleName());
            var scanResult = scan(issuerConfig, samlClientConfig, scanner);
            scanResults.put(scanner.getClass().getSimpleName(), scanResult);
            LOG.trace("Scanner {} finished with status: {}.\nFollowing evidences collected:\n{}", scanner.getClass().getSimpleName(), scanResult.getStatus(),String.join("\n", scanResult.getEvidences()));
        }

        ReportBuilder reportBuilder = new ReportBuilder(firstPositiveScan, secondPositiveScan, thirdPositiveScan, scanResults);
        return reportBuilder.Build();
    }

    private ScanResult scan(IssuerConfig samlissuerConfig, SamlClientConfig samlClientConfig, SamlScanner scanner){
        SamlClientConfig clientConfig = samlClientConfig.deepCopy();
        IssuerConfig issuerConfig = samlissuerConfig.deepCopy();
        clientConfig = scanner.getSamlClientConfig(clientConfig);
        issuerConfig = scanner.getSamlIssuerConfig(issuerConfig);

        SamlAuthData samlAuthData = new SamlAuthData();
        samlAuthData.setAuthMethod(JBossSAMLURIConstants.AC_UNSPECIFIED.get());
        samlAuthData.setSessionIndex(UUID.randomUUID() + "::" + UUID.randomUUID());
        samlAuthData.setAudiences(Collections.singletonList(clientConfig.getClientId()));
        samlAuthData = scanner.getAuthData(samlAuthData);

        try (SamlAuthenticationFlow samlAuthentication = new SamlAuthenticationFlow(issuerConfig.getEndpointUrl())){
            SamlInitializationResult initializationResult = samlAuthentication.initialize(clientConfig.getSignInUrl());

            scanner.init(firstPositiveScan, secondPositiveScan, thirdPositiveScan);

            SamlRequestData samlRequestData = initializationResult.asSamlRequestData();
            samlRequestData = scanner.getSamlRequestData(samlRequestData);

            SamlResponseBuilder samlResponseBuilder = new SamlResponseBuilder(
                    document -> scanner.beforeSigning(new SamlResponseDocument(document)).getDocument(),
                    document -> scanner.afterSigning(new SamlResponseDocument(document)).getDocument(),
                    encoded -> scanner.afterEncoding(encoded)
            );
            var samlResponseResult = samlResponseBuilder.create(issuerConfig, clientConfig, samlRequestData, samlAuthData);

            Page responsePage = samlAuthentication.answerWith(samlResponseResult.getHttpRequest());
            var samlAuthResult = new SamlAuthResult(issuerConfig,clientConfig, samlAuthData, samlRequestData, samlResponseResult, samlAuthentication.getAuthenticationLog(), responsePage);

            if(firstPositiveScan == null || secondPositiveScan == null){
                return new ScanResult(samlAuthResult, ScanResultStatus.OK, List.of());
            }

            var verificationStrategy = findVerificationStrategyBy(clientConfig.getScanResultVerificationStrategy());

            if(thirdPositiveScan == null){
                return verificationStrategy.evaluateScanResult(firstPositiveScan.getAuthResult(), secondPositiveScan.getAuthResult(), samlAuthResult);
            }

            return verificationStrategy.evaluateScanResult(firstPositiveScan.getAuthResult(), secondPositiveScan.getAuthResult(), samlAuthResult);
        }
    }

    private ScanResultVerificationStrategy findVerificationStrategyBy(String name){
        String normalizedName = "";
        for(var n : scanResultVerificationStrategies.keySet()){
           if(StringUtils.equalsIgnoreCase(name, n)){
               normalizedName = n;
           }
       }

        var verificationStrategy = scanResultVerificationStrategies.get(normalizedName);
        if(verificationStrategy == null){
            throw new RuntimeException("No verification strategy found for " + name);
        }
        return verificationStrategy;
    }
}
