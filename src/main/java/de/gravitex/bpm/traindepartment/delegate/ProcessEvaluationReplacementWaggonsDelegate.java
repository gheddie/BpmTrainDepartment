package de.gravitex.bpm.traindepartment.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import de.gravitex.bpm.traindepartment.delegate.base.TrainDepartmentJavaDelegate;
import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.DtpConstants;
import de.gravitex.bpm.traindepartment.logic.RailwayStationBusinessLogic;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public class ProcessEvaluationReplacementWaggonsDelegate extends TrainDepartmentJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		DepartmentProcessData processData = getProcessData(execution);
		
		WaggonProcessInfo[] deliveredWaggons = (WaggonProcessInfo[]) execution
				.getVariable(DtpConstants.DepartTrain.VAR.VAR_DELIVERED_EVALUATION_REPLACMENT_WAGGONS);
		List<String> waggonNumbersToAdd = new ArrayList<String>();
		List<String> waggonNumbersToRemove = new ArrayList<String>();
		for (WaggonProcessInfo deliveredWaggon : deliveredWaggons) {
			waggonNumbersToAdd.add(deliveredWaggon.getWaggonNumber());
			waggonNumbersToRemove.add(deliveredWaggon.getReplacementForWaggon());
			processData.addWaggon(deliveredWaggon);
		}
		// add them to replacement track		
		RailwayStationBusinessLogic.getInstance()
				.addWaggonsToTrack(processData.getDepartingOrder().getReplacementTrack(), waggonNumbersToAdd);
		
		// TODO remove on hashmap does not work?!?
		for (WaggonProcessInfo deliveredWaggon : deliveredWaggons) {
			// remove old
			processData.removeWaggon(deliveredWaggon.getReplacementForWaggon());			
			// add new
			processData.addWaggon(deliveredWaggon);
		}
		
		// processData.removeWaggons(waggonNumbersToRemove);
	}
}