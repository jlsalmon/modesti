package cern.modesti.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface UserRepository extends MongoRepository<User, String>, QueryDslPredicateExecutor<User>/*, QuerydslBinderCustomizer<QUser>*/ {

  User findOneByUsername(@Param("username") String username);

//  @Query("{ '$or':  [                                               " +
//         "    { '_id': '?0' },                                      " +
//         "    { 'employeeId': { '$regex': '^?0', $options: 'i' } }, " +
//         "    { 'username':   { '$regex': '^?0', $options: 'i' } }, " +
//         "    { 'firstName':  { '$regex': '^?0', $options: 'i' } }, " +
//         "    { 'lastName':   { '$regex': '^?0', $options: 'i' } }  " +
//         "  ],                                                      " +
//         "  '$and': [                                               " +
//         "    { 'authorities.authority': '?1' }                     " +
//         "  ]                                                       " +
//         "}                                                         ")
//  List<User> find(@Param("query") String query, @Param("authority") String authority);

//  @Override
//  default void customize(QuerydslBindings bindings, QUser root) {
//    bindings.bind(root.username).first(StringExpression::equalsIgnoreCase);
//  }
}
