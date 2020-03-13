package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class RemoveWaggonsDelegate implements JavaDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		RailwayStationBusinessLogic.getInstance().removeWaggons((List<String>) execution.getVariable(DepartTrainProcessConstants.VAR_PLANNED_WAGGONS));
	}
}