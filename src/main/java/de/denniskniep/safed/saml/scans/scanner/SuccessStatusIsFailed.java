package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import org.springframework.stereotype.Service;

import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_STATUS_CODE;

@Service
public class SuccessStatusIsFailed extends SamlBaseScanner {
    @Override
    public SamlResponseDocument beforeSigning(SamlResponseDocument document) {
        var node = document.selectNodesByXpath(XPATH_STATUS_CODE).getFirst();

        node.setTextContent("urn:oasis:names:tc:SAML:2.0:status:AuthnFailed");
        return document;
    }
}

