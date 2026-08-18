package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CookieVerification implements ScanResultVerificationStrategy {

    public static final String UNSTABLE_VALUE = "<unstable value>";

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        return List.of(
            new Evidence(EvidenceStatus.INFO, "Cookies", String.join(", ", getCookies(scanAuthResult).keySet()))
        );
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        // What cookies are present on a successful authentication.
        // Expectation is that a cookie (e.g. session cookie) is missing when auth is not successful
        var firstCookies = getCookies(firstPositiveAuthResult);
        var secondCookies = getCookies(secondPositiveAuthResult);
        var scanCookies = getCookies(scanAuthResult);

        var expectedCookiesOnBothSides = intersectedCookies(firstCookies, secondCookies);
        var actualCookiesOnBothSides = intersectedCookies(firstCookies, scanCookies);

        var cookieDiff = diff(expectedCookiesOnBothSides, actualCookiesOnBothSides);
        ScanResultStatus status = ScanResultStatus.OK;
        if(!cookieDiff.isEmpty()){
            status = ScanResultStatus.VULNERABLE;
        }

        var evidences = List.of(
            new Evidence(EvidenceStatus.INFO, "Cookies.Expected", asString(expectedCookiesOnBothSides)),
            new Evidence(EvidenceStatus.INFO, "Cookies.Current", asString(actualCookiesOnBothSides)),
            new Evidence(EvidenceStatus.from(status), "Cookies.Diff", cookieDiff.size() + " diff between cookies was detected. "+ String.join(",", cookieDiff))
        );
        return new VerificationResult(status, evidences);
    }

    private List<String> diff(Map<String, String> cookiesA, Map<String, String> cookiesB) {
        var cookieDiff = new ArrayList<String>();

        for (var cookieName : cookiesA.keySet()){
            String cookieAValue = cookiesA.get(cookieName);
            String cookieBValue = cookiesB.get(cookieName);

            if(StringUtils.equals(cookieAValue, cookieBValue)){
                // Both cookies exist and value match!
                continue;
            }

            if(cookieAValue != null && cookieBValue != null && (StringUtils.equals(cookieAValue, UNSTABLE_VALUE) || StringUtils.equals(cookieAValue, UNSTABLE_VALUE))){
                // Both cookies exist, but it must not match!
                continue;
            }

            if(cookieBValue == null){
                cookieDiff.add("'"+cookieName + "' does not exist");
                continue;
            }

            if(!StringUtils.equals(cookieAValue, cookieBValue)){
                cookieDiff.add("The values for '" + cookieName + "' are not equal");
                continue;
            }

            throw new RuntimeException("Case not handled!");
        }

        for (var cookieName : cookiesB.keySet()){
            String cookieAValue = cookiesA.get(cookieName);

            if(cookieAValue == null){
                cookieDiff.add("'"+cookieName + "' does not exist");
            }
        }

        return cookieDiff;
    }

    private String asString(Map<String, String> cookies){
      return cookies
              .entrySet()
              .stream()
              .map(kv -> kv.getKey() + ":" +  kv.getValue())
              .sorted()
              .collect(Collectors.joining(", "));
    }

    private Map<String, String> intersectedCookies(Map<String, String> cookiesA, Map<String, String> cookiesB){
        var cookies = new HashMap<String, String>();
        var sortedCookiesA = cookiesA.keySet().stream().sorted().toList();

        for (String cookieName : sortedCookiesA) {
            String cookieAValue = cookiesA.get(cookieName);
            String cookieBValue = cookiesB.get(cookieName);

            if(StringUtils.equals(cookieAValue, cookieBValue)){
                // pin the value if equal!
                cookies.put(cookieName, cookieAValue);
            } else if(cookieAValue != null && cookieBValue != null){
                // only expect the name if not equal!
                cookies.put(cookieName, UNSTABLE_VALUE);
            }
        }
        return cookies;
    }



    private Map<String, String> getCookies(AuthResult authResult){
        var cookies = new HashMap<String, String>();
        if(authResult.getResponsePage() == null){
            return cookies;
        }

        for (Cookie cookie : authResult.getResponsePage().cookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }
}
