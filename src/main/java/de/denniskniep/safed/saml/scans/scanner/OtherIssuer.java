package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.saml.scans.SamlBaseScanner;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Service
public class OtherIssuer extends SamlBaseScanner {

    @Override
    public IssuerConfig getIssuerConfig(IssuerConfig issuerConfig) {
        issuerConfig.setId(getIssuerUrl());
        return issuerConfig;
    }

    private URL getIssuerUrl(){
        try {
            return URI.create("http://malicious.example.com/").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Not a valid URL", e);
        }
    }
}
