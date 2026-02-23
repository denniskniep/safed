package de.denniskniep.examplemtls.mtls.admin;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/validation")
public class ValidationController {

    private final ValidationService oidcValidationService;

    public ValidationController(ValidationService oidcValidationService) {
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
