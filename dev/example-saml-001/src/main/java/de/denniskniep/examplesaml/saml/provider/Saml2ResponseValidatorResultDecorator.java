package de.denniskniep.examplesaml.saml.provider;

import de.denniskniep.examplesaml.saml.admin.SamlValidationService;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;

import java.io.StringWriter;
import java.util.Collection;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public class Saml2ResponseValidatorResultDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(Saml2ResponseValidatorResultDecorator.class);

    private final SamlValidationService samlValidationService;

    public Saml2ResponseValidatorResultDecorator(SamlValidationService samlValidationService) {
        this.samlValidationService = samlValidationService;
    }

    public Saml2ResponseValidatorResult decorate(Saml2ResponseValidatorResult original) {
        var wrapped = spy(original);
        doAnswer( i -> decorateResponse(i)).when(wrapped).concat(Mockito.<Saml2Error>any());
        doAnswer( i -> decorateResponse(i)).when(wrapped).concat(Mockito.<Saml2ResponseValidatorResult>any());
        doAnswer( i -> hasErrors(original)).when(wrapped).hasErrors();
        doAnswer( i -> getErrors(original)).when(wrapped).getErrors();
        return wrapped;
    }

    private Saml2ResponseValidatorResult decorateResponse(InvocationOnMock invocation) throws Throwable {
        var result = (Saml2ResponseValidatorResult)invocation.callRealMethod();
        return new Saml2ResponseValidatorResultDecorator(samlValidationService).decorate(result);
    }

    private boolean hasErrors(Saml2ResponseValidatorResult original) throws Throwable {
        return !samlValidationService.applyIgnoredErrorDescriptions(original.getErrors()).isEmpty();
    }

    private Collection<Saml2Error> getErrors(Saml2ResponseValidatorResult original) {
        var allErrors = original.getErrors();
        var filteredErrors = samlValidationService.applyIgnoredErrorDescriptions(original.getErrors());

        StringWriter sw = new StringWriter();
        sw.append("SAML Validation Errors (Apply "+filteredErrors.size()+"/"+allErrors.size() + "):");
        for (Saml2Error error : allErrors) {
            var present = filteredErrors.stream().anyMatch(e -> StringUtils.equals(error.getDescription(), e.getDescription()));
            sw.write("\n");
            sw.write((!present ? "[ignored] " : "") + error.getErrorCode() + ": " + error.getDescription());
        }
        LOG.info(sw.toString());

        return filteredErrors;
    }
}
