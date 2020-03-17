package de.gravitex.bpm.traindepartment.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class RemoveWaggonsDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		RailwayStationBusinessLogic.getInstance().removeWaggons(getWaggonNumbers(execution));
	}
}