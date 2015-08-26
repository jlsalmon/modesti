package cern.modesti.security.ldap;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface UserRepository extends MongoRepository<User, Integer> {
}
