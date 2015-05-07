package cern.modesti.security;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RestController
public class LoginController {

  @RequestMapping(value = "/login")
  public Principal login(Principal user) {
    return user;
  }
}
