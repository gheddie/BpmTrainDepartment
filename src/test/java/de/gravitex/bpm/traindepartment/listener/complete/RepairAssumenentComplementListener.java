package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.delegate.WaggonRepairInfo;
import de.gravitex.bpm.traindepartment.logic.DepartTrainProcessConstants;
import de.gravitex.bpm.traindepartment.util.HashMapBuilder;

public class RepairAssumenentComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		String assumedWaggon = (String) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_SINGLE_WAGGON_TO_ASSUME);
		System.out.println("calling back waggon assumement: " + assumedWaggon);
		int singleAssumedTime = (int) delegateTask.getProcessEngine().getRuntimeService()
				.getVariable(delegateTask.getExecutionId(), DepartTrainProcessConstants.VAR_ASSUMED_TIME);
		delegateTask.getProcessEngine().getRuntimeService()
				.correlateMessage(DepartTrainProcessConstants.MSG_REPAIR_ASSUMED,
						(String) delegateTask.getProcessEngine().getRuntimeService().getVariable(delegateTask.getExecutionId(),
								DepartTrainProcessConstants.VAR_DEP_PROC_BK),
						HashMapBuilder.create()
								.withValuePair(DepartTrainProcessConstants.VAR_SINGLE_WAGGON_TO_ASSUME,
										WaggonRepairInfo.fromValues(assumedWaggon, singleAssumedTime,
												delegateTask.getExecution().getBusinessKey()))
								.build());
	}
}