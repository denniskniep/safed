package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.scans.SamlBaseScanner;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import org.springframework.stereotype.Service;

import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_NAME_ID;

@Service
public class BreakSignature extends SamlBaseScanner {
    @Override
    public SamlResponseDocument afterSigning(SamlResponseDocument document) {
        var node = document.selectNodesByXpath(XPATH_NAME_ID).getFirst();
        node.setTextContent("manipulated");
        return document;
    }
}
