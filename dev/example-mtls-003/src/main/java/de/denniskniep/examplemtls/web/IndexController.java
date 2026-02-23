package de.denniskniep.examplemtls.web;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
    var principal = (User)authentication.getPrincipal();
    model.addAttribute("title", title);
    model.addAttribute("name", principal.getUsername());
    model.addAttribute("datetime", Instant.now().toString());
    return "index";
  }
}
