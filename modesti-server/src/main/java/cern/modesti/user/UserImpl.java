package cern.modesti.user;

/**
 * @author Justin Lewis Salmon
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Data
@Relation(value = "user", collectionRelation = "users")
@AllArgsConstructor
@NoArgsConstructor
public class UserImpl implements User, UserDetails {

  private static final long serialVersionUID = -8591164895383946725L;

  @Id
  private Integer employeeId;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  @JsonDeserialize(contentAs = SimpleGrantedAuthority.class)
  private Collection<? extends GrantedAuthority> authorities = new ArrayList<>();

  /** 
   * Creates a new user from the OAuth Oidc user 
   * @param user 
   */
  @SuppressWarnings("unchecked")
  public UserImpl(OidcUser user) {
    this.username = user.getName();
    this.employeeId = ((Long) user.getAttributes().get("cern_person_id")).intValue();
    this.firstName = user.getGivenName();
    this.lastName = user.getFamilyName();
    this.email = user.getEmail();
    this.authorities = user.getAuthorities();
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

  public boolean isAdmin() {
    return authorities.stream().anyMatch(role -> "modesti-administrators".equals(role.getAuthority()));
  }
}
