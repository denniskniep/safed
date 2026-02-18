package de.denniskniep.exampleoidc.oidc.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/validation")
public class OidcValidationController {
    private static final Logger LOG = LoggerFactory.getLogger(OidcValidationController.class);

    private final OidcValidationService oidcValidationService;

    public OidcValidationController(OidcValidationService oidcValidationService) {
        this.oidcValidationService = oidcValidationService;
    }

    @GetMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getIgnoredOidcErrorDescriptions() {
        return oidcValidationService.getIgnoredOidcErrorDescriptions();
    }


    @GetMapping(value = "lastSeenErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getLastSeenErrors() {
        return oidcValidationService.getLastSeenErrorDescriptions();
    }

    @PostMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody void setIgnoredOidcErrorDescriptions(@RequestBody List<String> errorDescriptions) {
        oidcValidationService.setIgnoredOidcErrorDescriptions(errorDescriptions);
    }
}
