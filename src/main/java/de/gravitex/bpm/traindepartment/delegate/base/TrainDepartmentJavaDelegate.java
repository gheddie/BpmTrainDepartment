package de.gravitex.bpm.traindepartment.delegate.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public abstract class TrainDepartmentJavaDelegate implements JavaDelegate {

	protected List<String> getWaggonNumbers(DelegateExecution execution) {
		return ((DepartmentProcessData) execution.getVariable(DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA))
				.getWaggonNumbers();
	}

	protected List<WaggonProcessInfo> getWaggons(DelegateExecution execution) {
		return new ArrayList<WaggonProcessInfo>(((DepartmentProcessData) getProcessData(execution)).getWaggons());
	}

	protected DepartmentProcessData getProcessData(DelegateExecution execution) {
		return RailwayStationBusinessLogic.getDepartmentProcessData(execution);
	}
}
