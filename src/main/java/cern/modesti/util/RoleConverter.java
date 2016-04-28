package cern.modesti.util;

import cern.modesti.user.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * @author Justin Lewis Salmon
 */
@Component
public class RoleConverter implements Converter<String, GrantedAuthority> {

  @Override
  public GrantedAuthority convert(String query) {
    return new Role(query);
  }
}
