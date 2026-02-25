package de.denniskniep.safed;

import de.denniskniep.safed.mtls.MtlsAssessment;
import de.denniskniep.safed.mtls.config.MtlsClientConfig;
import de.denniskniep.safed.mtls.config.MtlsConfig;
import de.denniskniep.safed.oidc.OidcAssessment;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.oidc.config.OidcConfig;
import de.denniskniep.safed.saml.SamlAssessment;
import de.denniskniep.safed.saml.config.SamlClientConfig;
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
import java.util.Arrays;
import java.util.Optional;

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
        Optional<String> firstArg = Arrays.stream(args).findFirst();
        if(firstArg.isEmpty()) {
            throw new RuntimeException("First argument is empty, must be a configured clientId");
        }
        String clientId = firstArg.get();

        SamlClientConfig samlClientConfig = samlConfig.getClient(clientId);
        if(samlClientConfig != null) {
            Report report = samlAssessment.run(samlClientConfig);
            LOG.info(report.asJson());
        }

        OidcClientConfig oidcClientConfig = oidcConfig.getClient(clientId);
        if(oidcClientConfig != null) {
            Report report = oidcAssessment.run(oidcClientConfig);
            LOG.info(report.asJson());
        }

        MtlsClientConfig mtlsClientConfig = mtlsConfig.getClient(clientId);
        if(mtlsClientConfig != null) {
            Report report = mtlsAssessment.run(mtlsClientConfig);
            LOG.info(report.asJson());
        }

        if(samlClientConfig == null && oidcClientConfig == null && mtlsClientConfig == null){
            throw new RuntimeException("Provided ClientId not found!");
        }

        SpringApplication.exit(applicationContext, () -> 0);
    }
}
