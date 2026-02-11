package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.data.SamlRequestData;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OtherInResponseTo extends SamlBaseScanner {
    @Override
    public SamlRequestData getSamlRequestData(SamlRequestData samlRequestData) {
        samlRequestData.setId(UUID.randomUUID().toString());
        return samlRequestData;
    }
}
