package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_CONDITIONS_NOT_ON_OR_AFTER;
import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_ISSUE_INSTANT;

@Service
public class Expired extends SamlBaseScanner {
    @Override
    public SamlResponseDocument beforeSigning(SamlResponseDocument document) {

        var issuedDate = Instant.now().minus(10, ChronoUnit.DAYS);
        var issuedDateAsString = new DateTimeFormatterBuilder()
                .appendInstant(3)
                .toFormatter()
                .format(issuedDate);

        var nodeIssuedAt = document.selectNodesByXpath(XPATH_ISSUE_INSTANT).getFirst();
        nodeIssuedAt.setTextContent(issuedDateAsString);

        var expiredDate = issuedDate.plus(1, ChronoUnit.HOURS);
        var expiredDateAsString = new DateTimeFormatterBuilder()
                .appendInstant(3)
                .toFormatter()
                .format(expiredDate);

        var nodeNotAfter = document.selectNodesByXpath(XPATH_CONDITIONS_NOT_ON_OR_AFTER).getFirst();
        nodeNotAfter.setTextContent(expiredDateAsString);

        return document;
    }
}
