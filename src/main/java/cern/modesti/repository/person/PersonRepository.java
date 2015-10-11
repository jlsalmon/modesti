package cern.modesti.repository.person;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface PersonRepository extends ReadOnlyRepository<Person, String> {

  @Query(value = "SELECT person_id as id, "
               + "first_name || ' ' || last_name as name, "
               + "username as username "
               + "FROM   persons_mv "
               + "WHERE  at_cern = 'Y' AND cern_class = 'STAF' "
               + "AND   (person_id  LIKE UPPER(:id || '%') "
               + "OR     cernid     LIKE UPPER(:id || '%') "
               + "OR     first_name || ' ' || last_name LIKE UPPER('%' || :name || '%'))", nativeQuery = true)
  @Cacheable("persons")
  List<Person> findByIdOrName(@Param("id") String id, @Param("name") String name);
}
