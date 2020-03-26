package de.gravitex.bpm.traindepartment.listener.base;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public abstract class TrainDepartmentTaskListener implements TaskListener {

	protected DepartmentProcessData getProcessData(DelegateExecution execution) {
		return RailwayStationBusinessLogic.getProcessData(execution);
	}
}