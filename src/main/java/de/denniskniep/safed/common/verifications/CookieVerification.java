package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;

import org.openqa.selenium.Cookie;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CookieVerification implements ScanResultVerificationStrategy {

    @Override
    public List<String> extractInfos(AuthResult scanAuthResult) {
        return List.of(
                "[INFO] Cookies: \n" + asString(getCookies(scanAuthResult))
        );
    }

    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new ScanResult(scanAuthResult, ScanResultStatus.OK, new ArrayList<>());
    }

    private String asString(Set<Cookie> cookies) {
        return cookies.stream().map(Cookie::toString).collect(Collectors.joining("\n"));
    }

    private Set<Cookie> getCookies(AuthResult authResult){
        return authResult.getResponsePage().cookies();
    }
}
