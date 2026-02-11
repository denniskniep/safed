package de.denniskniep.examplesaml.saml.admin;

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
public class SamlValidationController {
    private static final Logger LOG = LoggerFactory.getLogger(SamlValidationController.class);

    private final SamlValidationService samlValidationService;

    public SamlValidationController(SamlValidationService samlValidationService) {
        this.samlValidationService = samlValidationService;
    }

    @GetMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getIgnoredSamlErrorDescriptions() {
        return samlValidationService.getIgnoredSamlErrorDescriptions();
    }

    @GetMapping(value = "lastSeenErrorDescriptions", produces = "application/json")
    public @ResponseBody List<String> getLastSeenErrors() {
        return samlValidationService.getLastSeenErrorDescriptions();
    }

    @PostMapping(value = "ignoredErrorDescriptions", produces = "application/json")
    public @ResponseBody void setIgnoredSamlErrorDescriptions(@RequestBody List<String> errorDescriptions) {
        samlValidationService.setIgnoredSamlErrorDescriptions(errorDescriptions);
        LOG.info("Set ignored errorDescriptions: {}", String.join(",", errorDescriptions));
    }
}

