package cern.modesti.user;

import cern.modesti.predicate.RsqlExpressionBuilder;
import com.mysema.query.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.naming.InvalidNameException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class UserSearchController {

  @Autowired
  private UserService userService;

//  @Autowired
//  private UserRepository userRepository;

  @RequestMapping(value = "/users/search/findOneByUsername", method = GET, produces = "application/json")
  HttpEntity<Resource<User>> findOneByUsername(@RequestParam("username") String username) {
    User user = userService.findOneByUsername(username);
    if (user == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Resource<User> resource = new Resource<>(user);
    resource.add(linkTo(methodOn(UserSearchController.class).findOneByUsername(username)).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @RequestMapping(value = "/users/search", method = GET, produces = "application/json")
  HttpEntity<Resources<User>> search(@RequestParam("query") String query) throws InvalidNameException {
    Iterable<User> users = null;

    // TODO: use LdapRepository for this once https://github.com/spring-projects/spring-ldap/issues/374 is fixed.
    //
    // authorities.authority =in= (modesti-creators,modesti-administrators)
    // authorities.authority =in= (modesti-creators,modesti-administrators) and (username == jus or firstName == jus or lastName == jus)
    // (username == jus or firstName == jus or lastName == jus)

//    if (!query.isEmpty()) {
//      Predicate predicate = new RsqlExpressionBuilder<>(User.class).createExpression(query);
//      log.debug(format("searching for users: %s", predicate.toString()));
//      users = userRepository.findAll(predicate);
//    } else {
//      users = userRepository.findAll();
//    }

    String[] roles = new String[0];
    if (query.contains("authorities.authority")) {
      Pattern p = Pattern.compile("authorities\\.authority =in= \\((\\S*)\\)");
      Matcher m = p.matcher(query);

      while(m.find()) {
        String token = m.group(1);
        roles = token.split(",");
      }
    }

    String name = null;
    if (query.contains("username")) {
      name = query.split(" or ")[1].split(" == ")[1];
    }

    users = userService.findByUsernameOrNameAndRole(name, roles);

    Resources<User> resources = new Resources<>(users);

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}
