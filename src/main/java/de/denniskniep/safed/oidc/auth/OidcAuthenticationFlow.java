package de.denniskniep.safed.oidc.auth;

import de.denniskniep.safed.common.auth.AuthenticationFlow;
import de.denniskniep.safed.common.auth.RequestDataWithBody;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Map;

// Todo: rename to client?? (and server)
public class OidcAuthenticationFlow extends AuthenticationFlow<OidcAuthenticationRequest> {

    private final URL idpOidcEndpointUrl;

    public OidcAuthenticationFlow(URL idpOidcEndpointUrl) {
        this.idpOidcEndpointUrl = idpOidcEndpointUrl;
    }

    @Override
    protected boolean isRequestToIdp(RequestDataWithBody request) {
        return request.getMethod().equals("GET") && StringUtils.startsWithIgnoreCase(request.getUrl(), idpOidcEndpointUrl.toString());
    }

    @Override
    protected OidcAuthenticationRequest parse(RequestDataWithBody request) {
        Map<String, String> queryParams = request.getQueryParams();
        var oidcRequestData = new OidcAuthenticationRequest();
        oidcRequestData.setState(queryParams.get("state"));
        oidcRequestData.setNonce(queryParams.get("nonce"));
        oidcRequestData.setResponseType(queryParams.get("response_type"));
        oidcRequestData.setClientId(queryParams.get("client_id"));
        oidcRequestData.setScopes(queryParams.get("scope"));
        oidcRequestData.setRedirectUri(queryParams.get("redirect_uri"));
        return oidcRequestData;
    }
}