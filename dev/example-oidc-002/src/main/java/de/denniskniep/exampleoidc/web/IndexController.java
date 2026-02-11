package de.denniskniep.exampleoidc.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;

@Controller
public class IndexController {

  @Value("${spring.application.title}")
  private String title;

  @GetMapping("/")
  public String index(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    OidcUser principal = (OidcUser)authentication.getPrincipal();
    model.addAttribute("title", title);
    model.addAttribute("name", principal.getName());
    model.addAttribute("datetime", Instant.now().toString());
    return "index";
  }
}
