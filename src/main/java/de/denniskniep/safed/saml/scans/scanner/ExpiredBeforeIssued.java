package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.scans.SamlBaseScanner;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_CONDITIONS_NOT_ON_OR_AFTER;

@Service
public class ExpiredBeforeIssued extends SamlBaseScanner {
    @Override
    public SamlResponseDocument beforeSigning(SamlResponseDocument document) {
        var expiredDate = Instant.now().minus(10, ChronoUnit.DAYS);
        var expiredDateAsString = new DateTimeFormatterBuilder()
                .appendInstant(3)
                .toFormatter()
                .format(expiredDate);

        var node = document.selectNodesByXpath(XPATH_CONDITIONS_NOT_ON_OR_AFTER).getFirst();
        node.setTextContent(expiredDateAsString);
        return document;
    }
}
