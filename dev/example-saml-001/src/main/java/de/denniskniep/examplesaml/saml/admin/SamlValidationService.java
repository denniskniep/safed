package de.denniskniep.examplesaml.saml.admin;

import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SamlValidationService {

    private List<String> ignoredSamlErrorDescriptions = new ArrayList<>();
    private List<String> lastErrorDescriptions = new ArrayList<>();

    public void setIgnoredSamlErrorDescriptions(List<String> ignoredSamlErrorDescriptions) {
        this.ignoredSamlErrorDescriptions = ignoredSamlErrorDescriptions;
        this.lastErrorDescriptions = new ArrayList<>();
    }

    public List<String> getIgnoredSamlErrorDescriptions() {
        return this.ignoredSamlErrorDescriptions;
    }

    public Collection<Saml2Error> applyIgnoredErrorDescriptions(Collection<Saml2Error> errors) {
        var allIgnoredDescriptionsArePresent = ignoredSamlErrorDescriptions
                .stream()
                .allMatch(pattern -> patternMatchesAnyError(errors, pattern));


        if (!allIgnoredDescriptionsArePresent) {
            return errors;
        }

        var filteredErrors = errors
                .stream()
                .filter(e -> !errorMatchesAnyPattern(e))
                .toList();

        this.lastErrorDescriptions = filteredErrors.stream().map(Saml2Error::getDescription).toList();

        return filteredErrors;
    }


    private boolean patternMatchesAnyError(Collection<Saml2Error> errors, String ignoredPattern) {
        if (errors == null || errors.isEmpty() || ignoredPattern == null) {
            return false;
        }

        for (Saml2Error error : errors) {
            if(Pattern.matches(ignoredPattern, error.getDescription())){
                return true;
            }
        }

        return false;
    }

    private boolean errorMatchesAnyPattern(Saml2Error error) {
        if (error == null || ignoredSamlErrorDescriptions == null || ignoredSamlErrorDescriptions.isEmpty()) {
            return false;
        }

        for (String ignoredPattern : ignoredSamlErrorDescriptions) {
            if(Pattern.matches(ignoredPattern, error.getDescription())){
                return true;
            }
        }

        return false;
    }

    public List<String> getLastSeenErrorDescriptions() {
        return this.lastErrorDescriptions;
    }

}
