package cern.modesti.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

  private static final long serialVersionUID = -8591164895383946725L;

  @Id
  private Integer id;

  @Indexed
  private String username;

  @Indexed
  private String firstName;

  @Indexed
  private String lastName;

  @Indexed
  private String email;

  @Indexed
  @JsonDeserialize(contentAs = Role.class)
  private List<Role> authorities = new ArrayList<>();

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
