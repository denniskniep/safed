package de.denniskniep.safed.oidc.auth.server.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("protocol/openid-connect")
public class OidcController {
    private static final Logger LOG = LoggerFactory.getLogger(OidcController.class);

    private final OidcService oidcService;

    public OidcController(OidcService oidcService) {
        this.oidcService = oidcService;
    }

    @PostMapping(
            path = "token",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public @ResponseBody TokenResponse token(@RequestBody MultiValueMap params) {
        TokenRequest tokenRequest = new ObjectMapper().convertValue(params.toSingleValueMap(), TokenRequest.class);
        LOG.debug("Token request: {}", tokenRequest.asJson());
        var response = oidcService.processTokenRequest(tokenRequest);
        if(response == null) {
            LOG.warn("Token response not found");
            return null;
        }
        LOG.debug("Token response: {}", response.asJson());
        return response;
    }

    @GetMapping(
            path = "userinfo",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody UserInfoResponse userinfo(@RequestHeader("Authorization") String authorization) {
        LOG.debug("UserInfo request with Authorization header: {}", authorization);
        var response = oidcService.processUserInfoRequest(new UserInfoRequest(authorization));
        if(response == null) {
            LOG.warn("UserInfo response not found");
            return null;
        }
        LOG.debug("UserInfo response: {}", response.asJson());
        return response;
    }
}
