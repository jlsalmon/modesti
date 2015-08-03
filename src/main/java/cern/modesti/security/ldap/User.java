package cern.modesti.security.ldap;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

  private Integer id;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  private Collection<? extends GrantedAuthority> authorities;

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