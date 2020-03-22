package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class AllAssumementsDoneDelegate implements JavaDelegate {

	public static final Logger logger = Logger.getLogger(AllAssumementsDoneDelegate.class);

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {

		WaggonProcessInfo actuallyAssumed = (WaggonProcessInfo) execution
				.getVariable(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		logger.info("received repair assumement : " + actuallyAssumed);
		DepartmentProcessData departmentProcessData = (DepartmentProcessData) execution
				.getVariable(DepartTrainProcessConstants.VAR_DEPARTMENT_PROCESS_DATA);
		departmentProcessData.processRepairAssumption(actuallyAssumed.getWaggonNumber(),
				actuallyAssumed.getAssumedRepairDuration(), actuallyAssumed.getFacilityProcessBusinessKey());
		// alles abgeschätzt --> put them to 'VAR_ASSUMED_WAGGONS' ?
		// TODO make sub process talk to 'WaggonList' instance...
		/*
		 * boolean allWaggonsAssumed = waggonList.allWaggonsAssumed();
		 * execution.setVariable(DepartTrainProcessConstants.VAR_ALL_ASSUMEMENTS_DONE,
		 * allWaggonsAssumed);
		 */

		/*
		 * // update assumed hours... int assumedUpToNow = (int)
		 * execution.getVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS
		 * ); assumedUpToNow += actuallyAssumed.getAssumedRepairDuration();
		 * execution.setVariable(DepartTrainProcessConstants.
		 * VAR_SUMMED_UP_ASSUMED_HOURS, assumedUpToNow);
		 */
	}

	private List<String> convert(List<WaggonProcessInfo> assumedWaggons) {
		List<String> result = new ArrayList<String>();
		for (WaggonProcessInfo assumedWaggon : assumedWaggons) {
			result.add(assumedWaggon.getWaggonNumber());
		}
		return result;
	}
}