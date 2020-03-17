package de.gravitex.bpm.traindepartment.delegate.base;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartProcessData;

public abstract class TrainDepartmentJavaDelegate implements JavaDelegate {

	protected List<String> getWaggonNumbers(DelegateExecution execution) {
		return ((DepartProcessData) execution.getVariable(DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA)).getWaggonNumbers();
	}
	
	protected DepartProcessData getWaggonList(DelegateExecution execution) {
		return (DepartProcessData) execution.getProcessEngine().getRuntimeService().getVariable(execution.getId(),
				DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA);
	}
}
