package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

import static de.denniskniep.safed.saml.scans.SamlResponseDocument.XPATH_CONDITIONS_NOT_BEFORE;

@Service
public class FutureNotBefore extends SamlBaseScanner {
    @Override
    public SamlResponseDocument beforeSigning(SamlResponseDocument document) {
        var node = document.selectNodesByXpath(XPATH_CONDITIONS_NOT_BEFORE).getFirst();

        var futureDate = Instant.now().plus(10, ChronoUnit.DAYS);

        var futureDateAsString = new DateTimeFormatterBuilder()
                .appendInstant(3)
                .toFormatter()
                .format(futureDate);

        node.setTextContent(futureDateAsString);
        return document;
    }
}
