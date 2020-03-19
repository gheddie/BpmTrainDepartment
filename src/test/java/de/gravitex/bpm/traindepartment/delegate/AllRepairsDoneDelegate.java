package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;

public class AllRepairsDoneDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String waggonNumber = (String) execution.getVariable(DepartTrainProcessConstants.VAR_REPAIRED_WAGGON);
		System.out.println("processRepairCallback : " + waggonNumber);
		((DepartmentProcessData) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA))
						.processRepairCallback(waggonNumber);
	}
}