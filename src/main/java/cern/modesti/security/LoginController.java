package cern.modesti.security;

import cern.modesti.security.ldap.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RestController
public class LoginController {

  @Autowired
  Environment environmet;

  @RequestMapping(value = "/login")
  public UserDetails login(Principal principal) {
    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
    return (User) token.getPrincipal();
  }

  @RequestMapping(value = "/proxy.html", produces = "text/html")
  public String proxy() {
    String base = environmet.getRequiredProperty("modesti.base");
    return "<!DOCTYPE HTML><script src=\"http://localhost:9000/bower_components/xdomain/dist/xdomain.js\" master=\"" + base + "\"></script>";
  }

//  @RequestMapping(value = "/xdomain", produces = "text/javascript")
//  public ResponseEntity xdomain() throws IOException {
//    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
//    Resource resource = resolver.getResource("classpath:security/xdomain.min.js");
//
//    InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());
//    return new ResponseEntity(inputStreamResource, HttpStatus.OK);
//  }
}
