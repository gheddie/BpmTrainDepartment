package de.gravitex.bpm.traindepartment.delegate.base;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonList;

public abstract class DepartmentJavaDelegate implements JavaDelegate {

	@SuppressWarnings("unchecked")
	protected List<String> getWaggonNumbers(DelegateExecution execution) {
		return ((WaggonList) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGON_LIST)).getWaggonNumbers();
	}
}
