package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.config.SamlClientConfig;
import org.springframework.stereotype.Service;

@Service
public class NoSignature extends SamlBaseScanner {

    @Override
    public SamlClientConfig getSamlClientConfig(SamlClientConfig samlClientConfig) {
        samlClientConfig.setSignAssertion(false);
        samlClientConfig.setSignDocument(false);
        return samlClientConfig;
    }
}
