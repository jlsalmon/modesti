package cern.modesti.security.ldap;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface UserRepository extends MongoRepository<User, String> {

  User findOneByUsername(@Param("username") String username);

  List<User> findByAuthoritiesAuthorityContains(@Param("role") String role);
}
