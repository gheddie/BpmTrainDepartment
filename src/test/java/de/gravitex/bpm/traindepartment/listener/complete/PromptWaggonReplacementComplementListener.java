package de.gravitex.bpm.traindepartment.listener.complete;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class PromptWaggonReplacementComplementListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		// single info stored in 'VAR_PROMPT_REPLACE_WAGGON'...
	}
}