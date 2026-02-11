package de.denniskniep.examplesaml.saml.provider;

import de.denniskniep.examplesaml.saml.admin.SamlValidationService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2ParameterNames;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.Optional;

public class OpenSaml4AuthenticationProviderDecorator {

    private static final String ASSERTION_SIGNATURE_VALIDATOR = "responseSignatureValidator";
    private final SamlValidationService samlValidationService;

    public OpenSaml4AuthenticationProviderDecorator(SamlValidationService samlValidationService) {
        this.samlValidationService = samlValidationService;
    }

    public OpenSaml4AuthenticationProvider decorate(OpenSaml4AuthenticationProvider authenticationProvider) {
        try {
            var originalValidator = getOriginalValidator(authenticationProvider);
            setAssertionSignatureValidatorField(authenticationProvider, (t) -> convert(originalValidator, t));

        } catch (Exception e) {
            throw new RuntimeException("reflection failed during setting assertionSignatureValidator!", e);
        }
        authenticationProvider.setResponseValidator(createCustomResponseValidator());
        return authenticationProvider;
    }

    // Additionally verify the RelayState!
    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2ResponseValidatorResult> createCustomResponseValidator(){
            var original = OpenSaml4AuthenticationProvider.createDefaultResponseValidator();
            return (responseToken) -> {
                var result = original.convert(responseToken);

                var currentRequest = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                        .filter(ServletRequestAttributes.class::isInstance)
                        .map(ServletRequestAttributes.class::cast)
                        .map(ServletRequestAttributes::getRequest)
                        .orElseThrow();

                var originalRelayState = responseToken.getToken().getAuthenticationRequest().getRelayState();
                var currentRelayState = currentRequest.getParameter(Saml2ParameterNames.RELAY_STATE);
                if (originalRelayState != null && !originalRelayState.equals(currentRelayState)) {
                    result = result.concat(new Saml2Error("invalid_relay_state", "RelayState in response: " + (currentRelayState == null ? "<null>" : currentRelayState) + " differs from sent RelayState: "+ originalRelayState));
                }
                return result;
            };
    }

    private Saml2ResponseValidatorResult convert(Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2ResponseValidatorResult> originalValidator, OpenSaml4AuthenticationProvider.ResponseToken token) {
        var result = originalValidator.convert(token);
        return new Saml2ResponseValidatorResultDecorator(samlValidationService).decorate(result);
    }

    private static Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2ResponseValidatorResult> getOriginalValidator(OpenSaml4AuthenticationProvider authenticationProvider) throws IllegalAccessException, NoSuchFieldException {
        var field = getAssertionSignatureValidatorField(authenticationProvider);
        return (Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2ResponseValidatorResult>) field.get(authenticationProvider);
    }

    private static Field getAssertionSignatureValidatorField(OpenSaml4AuthenticationProvider authenticationProvider) throws NoSuchFieldException {
        Field field = authenticationProvider.getClass().getDeclaredField(ASSERTION_SIGNATURE_VALIDATOR);
        field.setAccessible(true);
        return field;
    }

    private static void setAssertionSignatureValidatorField(OpenSaml4AuthenticationProvider authenticationProvider, Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2ResponseValidatorResult> wrappingValidator) throws NoSuchFieldException, IllegalAccessException {
        var field = getAssertionSignatureValidatorField(authenticationProvider);
        field.set(authenticationProvider, wrappingValidator);
    }
}
