package cern.modesti.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single user entity retrieved from the authentication
 * context.
 *
 * @author Justin Lewis Salmon
 */
//@Entry(objectClasses = { "person", "top" }, base = "OU=Users,OU=Organic Units")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

  private static final long serialVersionUID = -8591164895383946725L;

  @Id
  private Integer employeeId;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

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
