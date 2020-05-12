package cern.modesti.user;

/**
 * @author Justin Lewis Salmon
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author Justin Lewis Salmon
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Relation(value = "user", collectionRelation = "users")
@NoArgsConstructor
public class UserImpl extends AUser implements LdapUser {

  private static final long serialVersionUID = -8591164895383946725L;

  public UserImpl(int employeeId, String username, String firstName, String lastName,
      String email, Collection<? extends GrantedAuthority> authorities) {
    super(employeeId, username, firstName, lastName, email, authorities);
  }
  
  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}
