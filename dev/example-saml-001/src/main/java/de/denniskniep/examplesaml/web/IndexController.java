package de.denniskniep.examplesaml.web;

import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

  @Value("${spring.application.title}")
  private String title;

  @GetMapping("/")
  public String index(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal)authentication.getPrincipal();
    model.addAttribute("title", title);
    model.addAttribute("name", principal.getName());
    model.addAttribute("datetime", Instant.now().toString());
    return "index";
  }

  @GetMapping("/home")
  public String home(Model model) {
    model.addAttribute("title", title);
    model.addAttribute("datetime", Instant.now().toString());
    return "home";
  }
}
