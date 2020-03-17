package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonList;

public class RemoveWaggonsDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		List<String> waggonsToRemove = ((WaggonList) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGON_LIST))
				.getWaggonNumbers();
		RailwayStationBusinessLogic.getInstance().removeWaggons(waggonsToRemove);
	}
}