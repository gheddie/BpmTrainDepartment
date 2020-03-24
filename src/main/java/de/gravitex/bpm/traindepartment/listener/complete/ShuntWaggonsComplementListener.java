package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import de.gravitex.bpm.traindepartment.logic.DtpConstants;

public class ShuntWaggonsComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		delegateTask.getProcessEngine().getRuntimeService().correlateMessage(DtpConstants.NotQualified.MESSAGE.MSG_SH_DONE);
	}
}