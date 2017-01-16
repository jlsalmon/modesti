package cern.modesti.user;

/**
 * @author Justin Lewis Salmon
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
//@Entry(objectClasses = { "person", "top" }, base = "OU=Users,OU=Organic Units")
@Data
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
  private List<SimpleGrantedAuthority> authorities = new ArrayList<>();

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
    return authorities.stream().anyMatch(role -> role.getAuthority().equals("modesti-administrators"));
  }
}