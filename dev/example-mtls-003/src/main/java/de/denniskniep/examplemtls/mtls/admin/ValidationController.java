package de.denniskniep.examplemtls.mtls.admin;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/validation")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getIgnoredOidcErrorDescriptions() {
        return validationService.getIgnoredOidcErrorDescriptions();
    }


    @GetMapping(value = "lastSeenErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getLastSeenErrors() {
        return validationService.getLastSeenErrorDescriptions();
    }

    @PostMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody void setIgnoredOidcErrorDescriptions(@RequestBody List<String> errorDescriptions) {
        validationService.setIgnoredOidcErrorDescriptions(errorDescriptions);
    }
}
