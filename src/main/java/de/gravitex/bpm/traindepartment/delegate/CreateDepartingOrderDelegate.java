package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.exception.RailWayException;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class CreateDepartingOrderDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		
		// initialize 'VAR_SUMMED_UP_ASSUMED_HOURS' here...
		execution.setVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS, 0);
		
		List<String> plannedWaggons = null;
		try {
			plannedWaggons = getWaggonNumbers(execution);
			RailwayStationBusinessLogic.getInstance().createDepartureOrder(plannedWaggons, execution.getBusinessKey());
		} catch (RailWayException e) {
			throw new BpmnError(DepartTrainProcessConstants.ERR_CREATE_DO);
		}
	}
}