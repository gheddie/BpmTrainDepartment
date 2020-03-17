package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.WaggonList;
import de.gravitex.bpm.traindepartment.logic.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.util.RailTestUtil;

public class AllAssumementsDoneDelegate implements JavaDelegate {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		WaggonRepairInfo actuallyAssumed = (WaggonRepairInfo) execution.getVariable(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		WaggonList waggonList = (WaggonList) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGON_LIST);
		waggonList.processRepairAssumption(actuallyAssumed.getWaggonNumber(), actuallyAssumed.getAssumedRepairDuration(), actuallyAssumed.getFacilityProcessBusinessKey());
		// alles abgeschätzt --> put them to 'VAR_ASSUMED_WAGGONS' ?
		// TODO make sub process talk to 'WaggonList' instance...
		boolean allWaggonsAssumed = waggonList.allWaggonsAssumed();
		execution.setVariable(DepartTrainProcessConstants.VAR_ALL_ASSUMEMENTS_DONE, allWaggonsAssumed);
		
		/*
		// update assumed hours...
		int assumedUpToNow = (int) execution.getVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS);
		assumedUpToNow += actuallyAssumed.getAssumedRepairDuration();
		execution.setVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS, assumedUpToNow);
		*/
	}

	private List<String> convert(List<WaggonRepairInfo> assumedWaggons) {
		List<String> result = new ArrayList<String>();
		for (WaggonRepairInfo assumedWaggon : assumedWaggons) {
			result.add(assumedWaggon.getWaggonNumber());
		}
		return result;
	}
}