package cern.modesti.security.ldap;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Created by jussy on 31/08/15.
 */
@Component
public class RoleConverter implements Converter<String, GrantedAuthority> {

  @Override
  public GrantedAuthority convert(String query) {
    return new Role(query);
  }
}