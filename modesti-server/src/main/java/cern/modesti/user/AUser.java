package cern.modesti.user;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract class for different User implementations
 * 
 * @author Ivan Prieto Barreiro
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AUser implements User {
  @Id
  protected Integer employeeId;

  protected String username;

  protected String firstName;

  protected String lastName;

  protected String email;
  
  @JsonDeserialize(contentAs = SimpleGrantedAuthority.class)
  protected Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
  
  @Override
  public boolean isAdmin() {
    return authorities.stream().anyMatch(role -> "modesti-administrators".equals(role.getAuthority()));
  }
  
  @Override
  public String getMail() {
    return this.email;
  }
}
