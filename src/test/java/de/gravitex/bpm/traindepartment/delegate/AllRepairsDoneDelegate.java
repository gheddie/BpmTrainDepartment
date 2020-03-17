package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonList;

public class AllRepairsDoneDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		((WaggonList) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_WAGGON_LIST))
						.processRepairCallback((String) execution.getVariable(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON));
	}
}