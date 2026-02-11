package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class CookieVerification implements ScanResultVerificationStrategy {

    @Override
    public ScanResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        getCookies(firstPositiveAuthResult);

        return new ScanResult(scanAuthResult, ScanResultStatus.OK, new ArrayList<>());
    }

    private Map<String, String> getCookies(AuthResult authResult){
        // TODO: implement

        /*List<NameValuePair> responseHeaders = authResult.getResponsePage().getWebResponse().getResponseHeaders();
        Optional<NameValuePair> cookies = responseHeaders.stream().filter(r -> StringUtils.containsIgnoreCase(r.getName(), "Set-Cookie")).findFirst();
        if(cookies.isEmpty()){
            return new HashedMap<>();
        }
        NameValuePair cookieHeader = cookies.get();*/
        return new HashedMap<>();
    }
}
