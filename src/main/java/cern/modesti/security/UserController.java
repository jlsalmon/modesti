package cern.modesti.security;

import cern.modesti.security.ldap.User;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/users")
public class UserController {

  /**
   *
   * @return
   */
  @RequestMapping(method = GET)
  public ResponseEntity<Resources<User>> getUsers() {
    List<User> users = new ArrayList<>();

    // Need to check who is potentially assignable to the current task of a request.
    //
    // a. Use activiti UserQuery.memberOfGroup().
    //    - Necessary? Quicker to have users stored locally in DB than making LDAP call, but too much reliance on activiti?
    //    - Are the users ever actually used within activiti? don't think so
    //
    // b. Call LDAP server and ask for all users who are a member of the candidate groups of the task to be delegated.
    //    - Too slow? Maybe, maybe not
    //
    // c. Syncronise LDAP users at startup to a Repository. Query that from REST. Maybe it can be made flexible enough to remove need for controller

    Resources<User> resources = new Resources<>(users);
    resources.add(linkTo(methodOn(UserController.class).getUsers()).withSelfRel());

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}
