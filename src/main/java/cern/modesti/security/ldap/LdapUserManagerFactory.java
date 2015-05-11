/**
 *
 */
package cern.modesti.security.ldap;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;

/**
 * @author Justin Lewis Salmon
 *
 */
public class LdapUserManagerFactory implements SessionFactory {

  LdapUserManager userManager;

  /**
   * @param userManager
   */
  public LdapUserManagerFactory(LdapUserManager userManager) {
    this.userManager = userManager;
  }

  @Override
  public Class<?> getSessionType() {
    return UserIdentityManager.class;
  }

  @Override
  public Session openSession() {
    return userManager;
  }
}
