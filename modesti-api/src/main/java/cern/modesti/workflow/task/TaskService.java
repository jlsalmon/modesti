package cern.modesti.workflow.task;

import cern.modesti.user.User;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface TaskService {
  TaskInfo getActiveTask(String requestId);

  TaskInfo execute(String requestId, String taskName, TaskAction action, User user);

  List<TaskInfo> getTasks(String requestId);

  TaskInfo getTask(String requestId, String taskName);
}
