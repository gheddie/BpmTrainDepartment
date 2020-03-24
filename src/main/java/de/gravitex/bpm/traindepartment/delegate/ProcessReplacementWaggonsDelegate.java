package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.enumeration.WaggonState;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class ProcessReplacementWaggonsDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		// integrate waggons to the system
		String[] deliveredWaggons = (String[]) execution.getProcessEngine().getRuntimeService()
				.getVariable(execution.getId(), DtpConstants.NotQualified.VAR.VAR_DELIVERED_REPLACMENT_WAGGONS);
		for (String deliveredWaggon : deliveredWaggons) {
			getProcessData(execution).addWaggon(WaggonProcessInfo.fromValues(deliveredWaggon, WaggonState.REPLACED));
		}

		// put waggons to replacement track
		String track = (String) execution.getVariable(DtpConstants.NotQualified.VAR.VAR_REPLACE_WAGGON_TARGET_TRACK);
		String[] deliveredReplacementWaggons = (String[]) execution
				.getVariable(DtpConstants.NotQualified.VAR.VAR_DELIVERED_REPLACMENT_WAGGONS);
		List<String> deliveredReplacementWaggonsList = new ArrayList<String>();
		for (String waggonNumber : deliveredReplacementWaggons) {
			deliveredReplacementWaggonsList.add(waggonNumber);
		}
		RailwayStationBusinessLogic.getInstance().addWaggonsToTrack(track, deliveredReplacementWaggonsList);
	}
}