package cern.modesti.security;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class CustomGroupEntityManager extends GroupEntityManager {

  @Override
  public Group createNewGroup(String groupId) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void insertGroup(Group group) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void updateGroup(Group updatedGroup) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void deleteGroup(String groupId) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public GroupQuery createNewGroupQuery() {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public List<Group> findGroupsByUser(String userId) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean isNewGroup(Group group) {
    throw new UnsupportedOperationException("not implemented yet");
  }
}
