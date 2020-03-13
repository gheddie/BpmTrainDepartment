package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;

public class PlaceReplacementWaggonsDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String track = (String) execution.getVariable(DepartTrainProcessConstants.VAR_REPLACE_WAGGON_TARGET_TRACK);
		String[] deliveredReplacementWaggons = (String[]) execution.getVariable(DepartTrainProcessConstants.VAR_DELIVERED_REPLACMENT_WAGGONS);
		List<String> deliveredReplacementWaggonsList = new ArrayList<String>();
		for (String waggonNumber : deliveredReplacementWaggons) {
			deliveredReplacementWaggonsList.add(waggonNumber);
		}
		RailwayStationBusinessLogic.getInstance().addWaggonsToTrack(track, deliveredReplacementWaggonsList);
	}
}