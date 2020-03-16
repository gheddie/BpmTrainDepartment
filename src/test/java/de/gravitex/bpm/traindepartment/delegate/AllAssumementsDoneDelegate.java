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
		
		/*
		if (execution.getVariable(DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS) == null) {
			execution.setVariable(DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS, new ArrayList<WaggonRepairInfo>());
		}
		*/
		// List<WaggonRepairInfo> assumedWaggons = (List<WaggonRepairInfo>) execution.getVariable(DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS);
		WaggonRepairInfo actuallyAssumed = (WaggonRepairInfo) execution.getVariable(DepartTrainProcessConstants.VAR_SINGLE_FACILITY_PROCESS_WAGGON);
		
		// assumedWaggons.add(actuallyAssumed);
		
		WaggonList waggonList = (WaggonList) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGON_LIST);
		waggonList.processRepairAssumption(actuallyAssumed.getWaggonNumber(), actuallyAssumed.getAssumedRepairDuration());
		
		// all waggons assumed?
		/*
		List<String> waggonsToAssume = (List<String>) execution.getVariable(DepartTrainProcessConstants.VAR_WAGGONS_TO_ASSUME);
		boolean allAssumed = RailTestUtil.areListsEqual(convert(assumedWaggons),
				waggonsToAssume);
				*/
		
		// alles abgeschätzt?
		boolean allWaggonsAssumed = waggonList.allWaggonsAssumed();
		execution.setVariable(DepartTrainProcessConstants.VAR_ALL_ASSUMEMENTS_DONE, allWaggonsAssumed);
		
		if (allWaggonsAssumed) {
			List<WaggonRepairInfo> fakedAssumedWaggons = new ArrayList<WaggonRepairInfo>();
			fakedAssumedWaggons.add(WaggonRepairInfo.fromWaggonNumber("PETER"));
			execution.setVariable(DepartTrainProcessConstants.VAR_ASSUMED_WAGGONS, fakedAssumedWaggons);
		}
		
		// update assumed hours...
		int assumedUpToNow = (int) execution.getVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS);
		assumedUpToNow += actuallyAssumed.getAssumedRepairDuration();
		execution.setVariable(DepartTrainProcessConstants.VAR_SUMMED_UP_ASSUMED_HOURS, assumedUpToNow);
	}

	private List<String> convert(List<WaggonRepairInfo> assumedWaggons) {
		List<String> result = new ArrayList<String>();
		for (WaggonRepairInfo assumedWaggon : assumedWaggons) {
			result.add(assumedWaggon.getWaggonNumber());
		}
		return result;
	}
}