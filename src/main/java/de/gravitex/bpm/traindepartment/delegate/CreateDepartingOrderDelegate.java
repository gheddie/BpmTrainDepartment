package de.gravitex.bpm.traindepartment.delegate;

import java.util.List;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.exception.RailWayException;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class CreateDepartingOrderDelegate extends TrainDepartmentJavaDelegate {
	
	public static final Logger logger = Logger.getLogger(CreateDepartingOrderDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		// initialize 'VAR_SUMMED_UP_ASSUMED_HOURS' here...
		execution.setVariable(DtpConstants.NotQualified.VAR.VAR_SUMMED_UP_ASSUMED_HOURS, 0);
		List<String> plannedWaggons = null;
		try {
			plannedWaggons = getWaggonNumbers(execution);
			RailwayStationBusinessLogic.getInstance().createDepartureOrder(plannedWaggons, execution.getBusinessKey());
			logger.info("succesfully created a departing order...");
		} catch (RailWayException e) {
			logger.error("error creating a departing order: " + e.getMessage());
			throw new BpmnError(DtpConstants.NotQualified.ERROR.ERR_CREATE_DO);
		}
	}
}