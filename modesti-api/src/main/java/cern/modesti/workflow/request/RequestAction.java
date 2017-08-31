package cern.modesti.workflow.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents an action on a workflow request.
 *
 * @author Ivan Prieto Barreiro
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAction {
	private Action action;
	private String creator;

	public enum Action {
		CREATOR
	}
}
