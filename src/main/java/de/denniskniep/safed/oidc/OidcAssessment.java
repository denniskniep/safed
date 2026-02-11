package de.denniskniep.safed.oidc;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationFlow;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.flows.OidcFlowResult;
import de.denniskniep.safed.oidc.auth.flows.AuthorizationCodeFlow;
import de.denniskniep.safed.oidc.backend.OidcService;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.oidc.scans.scanner.OidcBaseScanner;
import de.denniskniep.safed.oidc.scans.OidcScanner;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.report.ReportBuilder;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.common.verifications.ScanResultVerificationStrategy;
import org.apache.commons.lang3.StringUtils;
import de.denniskniep.safed.common.auth.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OidcAssessment {

    private static final Logger LOG = LoggerFactory.getLogger(OidcAssessment.class);

    private final Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies;
    private final List<OidcScanner> scanners;

    private final OidcService oidcService;

    private ScanResult firstPositiveScan;
    private ScanResult secondPositiveScan;
    private ScanResult thirdPositiveScan;

    @Autowired
    public OidcAssessment(Map<String, ScanResultVerificationStrategy> scanResultVerificationStrategies, List<OidcScanner> scanners, OidcService oidcService) {
        this.scanResultVerificationStrategies = scanResultVerificationStrategies;
        this.scanners = scanners;
        this.oidcService = oidcService;
    }

    public Report run(IssuerConfig issuerConfig, OidcClientConfig oidcClientConfig) {
        LOG.info("Start initial tests");
        // First scan with positive login
        firstPositiveScan = scan(issuerConfig, oidcClientConfig, new OidcBaseScanner());
        // Second scan with positive login, but maybe anyhow changes in the page content
        secondPositiveScan = scan(issuerConfig, oidcClientConfig, new OidcBaseScanner());
        // Third scan with positive login
        thirdPositiveScan = scan(issuerConfig, oidcClientConfig, new OidcBaseScanner());

        // ToDo: Add another initial scan that forces an error as an empty OIDCResponse is sent
        //forceFailureScan = scan(clientId, new BaseScanner());
        LOG.info("End initial tests");

            if(thirdPositiveScan.getStatus() != ScanResultStatus.VULNERABLE) {
            // For a manipulated SAMLResponse we expect a significant drift in the response
            // The third scan with positive login does not have that on purpose!
            throw new RuntimeException("Third positive scan must always be classified as VULNERABLE!");
        }

        var scanResults = new HashMap<String, ScanResult>();
        for (var scanner : scanners){
            if(oidcClientConfig.getScanners() != null && !oidcClientConfig.getScanners().contains(scanner.getClass().getSimpleName())) {
                LOG.info("Skip scanning with {}", scanner.getClass().getSimpleName());
                continue;
            }

            LOG.info("Start scanning with {}", scanner.getClass().getSimpleName());
            var scanResult = scan(issuerConfig, oidcClientConfig, scanner);
            scanResults.put(scanner.getClass().getSimpleName(), scanResult);
            LOG.trace("Scanner {} finished with status: {}.\nFollowing evidences collected:\n{}", scanner.getClass().getSimpleName(), scanResult.getStatus(),String.join("\n", scanResult.getEvidences()));
        }

        ReportBuilder reportBuilder = new ReportBuilder(firstPositiveScan, secondPositiveScan, thirdPositiveScan, scanResults);
        return reportBuilder.Build();
    }

    private ScanResult scan(IssuerConfig oidcIssuerConfig, OidcClientConfig oidcClientConfig, OidcScanner scanner){
        scanner.init(firstPositiveScan, secondPositiveScan, thirdPositiveScan);

        OidcClientConfig clientConfig = scanner.getOidcClientConfig(oidcClientConfig.deepCopy());
        IssuerConfig issuerConfig = scanner.getIssuerConfig(oidcIssuerConfig.deepCopy());

        try (OidcAuthenticationFlow oidcAuthentication = new OidcAuthenticationFlow(issuerConfig.getEndpointUrl())){
            OidcAuthenticationRequest oidcRequestData = oidcAuthentication.initialize(clientConfig.getSignInUrl());
            oidcRequestData = scanner.getOidcRequestData(oidcRequestData.deepCopy());

            OidcFlowResult result;
            if(StringUtils.equals(oidcRequestData.getResponseType(), "code")) {
                var codeFlow = new AuthorizationCodeFlow(issuerConfig, clientConfig, oidcRequestData, scanner.getTokenInterceptors(), scanner::getTokenResponse);
                result = new OidcFlowResult(codeFlow, codeFlow);
            }else{
                throw new RuntimeException("Unexpected response type: " + oidcRequestData.getResponseType());
            }

            oidcService.registerBackChannelResponse(result.getBackChannelResponse());
            Page responsePage = oidcAuthentication.answerWith(result.getFrontChannelRequest().buildWebRequest());

            // Do we need to wait here or is everything done once the FrontChannel page is loaded?
            oidcService.unregisterBackChannelResponse(result.getBackChannelResponse());

            var oidcAuthResult = new OidcAuthResult(issuerConfig, clientConfig, oidcRequestData, result, oidcAuthentication.getAuthenticationLog(), responsePage);


            if(firstPositiveScan == null || secondPositiveScan == null){
                return new ScanResult(oidcAuthResult, ScanResultStatus.OK, List.of());
            }

            var verificationStrategy = findVerificationStrategyBy(clientConfig.getScanResultVerificationStrategy());

            if(thirdPositiveScan == null){
                return verificationStrategy.evaluateScanResult(firstPositiveScan.getAuthResult(), secondPositiveScan.getAuthResult(), oidcAuthResult);
            }

            return verificationStrategy.evaluateScanResult(firstPositiveScan.getAuthResult(), secondPositiveScan.getAuthResult(), oidcAuthResult);
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