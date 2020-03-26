package de.gravitex.bpm.traindepartment.delegate.base;

import de.gravitex.bpm.traindepartment.logic.DepartmentProcessData;
import de.gravitex.bpm.traindepartment.logic.WaggonProcessInfo;

public abstract class ReplaceWaggonsJavaDelegate extends TrainDepartmentJavaDelegate {

	protected void replaceWaggons(DepartmentProcessData processData, WaggonProcessInfo[] deliveredWaggons) {
		// replace them in process waggons
		for (WaggonProcessInfo deliveredWaggon : deliveredWaggons) {
			// remove old
			processData.removeWaggon(deliveredWaggon.getReplacementForWaggon());			
			// add new
			processData.addWaggon(deliveredWaggon);
		}
	}
}