/**
 *
 */
package cern.modesti.security.ldap;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

/**
 * @author Justin Lewis Salmon
 *
 */
public class LdapGroupManagerFactory implements SessionFactory {

  LdapGroupManager groupManager;

  /**
   * @param groupEntityManager
   */
  public LdapGroupManagerFactory(LdapGroupManager groupManager) {
    this.groupManager = groupManager;
  }

  @Override
  public Class<?> getSessionType() {
    return GroupIdentityManager.class;
  }

  @Override
  public Session openSession() {
    return groupManager;
  }
}