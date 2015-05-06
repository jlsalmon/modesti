package cern.modesti.security;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RestController
public class UserController {

  @RequestMapping(value = "/user", method = GET)
  public Principal getUser(Principal user) {
    return user;
  }
}
