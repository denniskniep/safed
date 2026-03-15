package de.denniskniep.safed;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.common.utils.Serialization;
import de.denniskniep.safed.mtls.MtlsAssessment;
import de.denniskniep.safed.mtls.config.MtlsAppConfig;
import de.denniskniep.safed.mtls.config.MtlsConfig;
import de.denniskniep.safed.oidc.OidcAssessment;
import de.denniskniep.safed.oidc.config.OidcAppConfig;
import de.denniskniep.safed.oidc.config.OidcConfig;
import de.denniskniep.safed.saml.SamlAssessment;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlConfig;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.webhook.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ConditionalOnBooleanProperty(value = "safed.run-cli", matchIfMissing = true)
@Component
public class SafedCli implements CommandLineRunner {
    private final SamlConfig samlConfig;
    private final SamlAssessment samlAssessment;
    private final OidcConfig oidcConfig;
    private final OidcAssessment oidcAssessment;
    private final MtlsAssessment mtlsAssessment;
    private final ApplicationContext applicationContext;
    private final WebhookService webhookService;

    private static final Logger LOG = LoggerFactory.getLogger(SafedCli.class);
    private final MtlsConfig mtlsConfig;

    @Autowired
    public SafedCli(ApplicationContext applicationContext, SamlConfig samlConfig, SamlAssessment samlAssessment, OidcConfig oidcConfig, OidcAssessment oidcAssessment, MtlsAssessment mtlsAssessment, MtlsConfig mtlsConfig, WebhookService webhookService) {
        this.applicationContext = applicationContext;
        this.samlConfig = samlConfig;
        this.samlAssessment = samlAssessment;
        this.oidcConfig = oidcConfig;
        this.oidcAssessment = oidcAssessment;
        this.mtlsAssessment = mtlsAssessment;
        this.mtlsConfig = mtlsConfig;
        this.webhookService = webhookService;
    }

    @Override
    public void run(String... args) throws MalformedURLException {
        var clientIds = extractListArg(Arrays.asList(args), 0);
        if(clientIds.isEmpty()) {
            throw new RuntimeException("No arguments provided, must be a configured clientId");
        }

        var triggeredScanners = extractListArg(Arrays.asList(args), 1);

        var reports = new ArrayList<Report>();
        for(String clientId : clientIds) {
            SamlAppConfig samlAppConfig = samlConfig.getClient(clientId);
            if(samlAppConfig != null) {
                samlAppConfig = replaceTriggeredScanners(samlAppConfig,  triggeredScanners);
                Report report = samlAssessment.run(clientId, samlAppConfig);
                reports.add(report);
            }

            OidcAppConfig oidcAppConfig = oidcConfig.getClient(clientId);
            if(oidcAppConfig != null) {
                oidcAppConfig = replaceTriggeredScanners(oidcAppConfig,  triggeredScanners);
                Report report = oidcAssessment.run(clientId, oidcAppConfig);
                reports.add(report);
            }

            MtlsAppConfig mtlsAppConfig = mtlsConfig.getClient(clientId);
            if(mtlsAppConfig != null) {
                mtlsAppConfig = replaceTriggeredScanners(mtlsAppConfig,  triggeredScanners);
                Report report = mtlsAssessment.run(clientId, mtlsAppConfig);
                reports.add(report);
            }

            if(samlAppConfig == null && oidcAppConfig == null && mtlsAppConfig == null){
                var report = new Report();
                report.setStatus(ScanResultStatus.FAILED);
                report.setErrors(List.of("Provided ClientId  '" + clientId + "' not found!"));
                reports.add(report);
            }
        }

        LOG.debug(Serialization.AsPrettyJson(reports));
        if(!LOG.isDebugEnabled()) {
            LOG.info(Serialization.AsJsonString(reports));
        }

        webhookService.sendReports(reports);
        SpringApplication.exit(applicationContext, () -> 0);
    }

    private <T extends AppConfig> T replaceTriggeredScanners(T appConfig, List<String> triggeredScanners) {
        var result = appConfig;
        if(triggeredScanners != null && !triggeredScanners.isEmpty()) {
            // Overwrite configured value with cmd arg
            result = (T)appConfig.deepCopy();
            result.setScanners(triggeredScanners);
        }
        return result;
    }

    private List<String> extractListArg(List<String> args, int index){
        if (args.size() < index + 1){
            return new ArrayList<>();
        }

        var arg = args.get(index);
        return List.of(arg.split(","));
    }
}
