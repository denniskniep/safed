package de.denniskniep.safed;

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

@ConditionalOnBooleanProperty(value = "safed.run-cli", matchIfMissing = true)
@Component
public class SafedCli implements CommandLineRunner {
    private final SamlConfig samlConfig;
    private final SamlAssessment samlAssessment;
    private final OidcConfig oidcConfig;
    private final OidcAssessment oidcAssessment;
    private final MtlsAssessment mtlsAssessment;
    private final ApplicationContext applicationContext;

    private static final Logger LOG = LoggerFactory.getLogger(SafedCli.class);
    private final MtlsConfig mtlsConfig;

    @Autowired
    public SafedCli(ApplicationContext applicationContext, SamlConfig samlConfig, SamlAssessment samlAssessment, OidcConfig oidcConfig, OidcAssessment oidcAssessment, MtlsAssessment mtlsAssessment, MtlsConfig mtlsConfig) {
        this.applicationContext = applicationContext;
        this.samlConfig = samlConfig;
        this.samlAssessment = samlAssessment;
        this.oidcConfig = oidcConfig;
        this.oidcAssessment = oidcAssessment;
        this.mtlsAssessment = mtlsAssessment;
        this.mtlsConfig = mtlsConfig;
    }

    @Override
    public void run(String... args) throws MalformedURLException {
        List<String> clientIds = Arrays.stream(args).toList();
        if(clientIds.isEmpty()) {
            throw new RuntimeException("No arguments provided, must be a configured clientId");
        }

        var reports = new ArrayList<Report>();
        for(String clientId : clientIds) {
            SamlAppConfig samlAppConfig = samlConfig.getClient(clientId);
            if(samlAppConfig != null) {
                Report report = samlAssessment.run(clientId, samlAppConfig);
                reports.add(report);
            }

            OidcAppConfig oidcAppConfig = oidcConfig.getClient(clientId);
            if(oidcAppConfig != null) {
                Report report = oidcAssessment.run(clientId, oidcAppConfig);
                reports.add(report);
            }

            MtlsAppConfig mtlsAppConfig = mtlsConfig.getClient(clientId);
            if(mtlsAppConfig != null) {
                Report report = mtlsAssessment.run(clientId, mtlsAppConfig);
                reports.add(report);
            }

            if(samlAppConfig == null && oidcAppConfig == null && mtlsAppConfig == null){
                var report = new Report();
                report.setStatus(ScanResultStatus.FAILED);
                report.setErrors(List.of("Provided ClientId  " + clientId + " not found!"));
            }

            LOG.info(Serialization.AsPrettyJson(reports));
        }

        SpringApplication.exit(applicationContext, () -> 0);
    }
}
