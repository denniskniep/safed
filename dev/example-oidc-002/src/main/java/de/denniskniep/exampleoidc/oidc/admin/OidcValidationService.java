package de.denniskniep.exampleoidc.oidc.admin;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OidcValidationService {

    private List<String> ignoredOidcErrorDescriptions = new ArrayList<>();
    private List<String> lastErrorDescriptions = new ArrayList<>();

    public void setIgnoredOidcErrorDescriptions(List<String> ignoredOidcErrorDescriptions) {
        this.ignoredOidcErrorDescriptions = ignoredOidcErrorDescriptions;
        this.lastErrorDescriptions = new ArrayList<>();
    }

    public List<String> getIgnoredOidcErrorDescriptions() {
        return this.ignoredOidcErrorDescriptions;
    }

    public List<OAuth2Error> applyIgnoredErrorDescriptions(Collection<OAuth2Error> errors) {
        var allIgnoredDescriptionsArePresent = ignoredOidcErrorDescriptions
                .stream()
                .allMatch(pattern -> patternMatchesAnyError(errors, pattern));

        if (!allIgnoredDescriptionsArePresent) {
            return errors.stream().toList();
        }

        var filteredErrors = errors
                .stream()
                .filter(e -> !errorMatchesAnyPattern(e))
                .toList();

        this.lastErrorDescriptions = filteredErrors.stream().map(OAuth2Error::getDescription).toList();
        return filteredErrors;
    }

    private boolean patternMatchesAnyError(Collection<OAuth2Error> errors, String ignoredPattern) {
        if (errors == null || errors.isEmpty() || ignoredPattern == null) {
            return false;
        }

        for (OAuth2Error error : errors) {
            if (Pattern.matches(ignoredPattern, error.getDescription())) {
                return true;
            }
        }

        return false;
    }

    private boolean errorMatchesAnyPattern(OAuth2Error error) {
        if (error == null || ignoredOidcErrorDescriptions == null || ignoredOidcErrorDescriptions.isEmpty()) {
            return false;
        }

        for (String ignoredPattern : ignoredOidcErrorDescriptions) {
            if (Pattern.matches(ignoredPattern, error.getDescription())) {
                return true;
            }
        }

        return false;
    }

    public List<String> getLastSeenErrorDescriptions() {
        return this.lastErrorDescriptions;
    }
}
