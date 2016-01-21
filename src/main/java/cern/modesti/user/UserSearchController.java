package cern.modesti.user;

import cern.modesti.predicate.RsqlExpressionBuilder;
import com.mysema.query.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class UserSearchController {

  @Autowired
  private UserRepository repository;

  @RequestMapping(value = "/users/search", method = GET, produces = "application/json")
  HttpEntity<PagedResources<User>> search(@RequestParam("query") String query, Pageable pageable, PagedResourcesAssembler assembler) {
    Page<User> users;

    if (!query.isEmpty()) {
      Predicate predicate = new RsqlExpressionBuilder<>(User.class).createExpression(query);
      log.debug(format("searching for users: %s", predicate.toString()));
      users = repository.findAll(predicate, pageable);
    } else {
      users = repository.findAll(pageable);
    }

    return new ResponseEntity<>(assembler.toResource(users), HttpStatus.OK);
  }
}
