//package cern.modesti.workflow.task;
//
//import cern.modesti.request.Request;
//import lombok.Data;
//import org.activiti.engine.task.DelegationState;
//import org.springframework.hateoas.core.Relation;
//
//import java.util.Set;
//
///**
// * This class represents a single task inside a workflow process instance
// * associated with a {@link Request}.
// *
// * @author Justin Lewis Salmon
// */
//@Data
//@Relation(collectionRelation = "tasks")
//public class TaskInfo {
//  private final String name;
//  private final String description;
//  private final String owner;
//  private final String assignee;
//  private final DelegationState delegationState;
//  private final Set<String> candidateGroups;
//}
