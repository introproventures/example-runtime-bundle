package org.activiti.cloud.runtime.example;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.runtime.ProcessInstance;

public class CheckLoanWorkflowStartedJavaDelegate extends AbstractTimedJavaDelegate {
	@Override
	public void execute(DelegateExecution execution) {
		RuntimeService runtimeService = Context.getProcessEngineConfiguration().getRuntimeService();
		
		String loanId = (String) execution.getVariable("loanId");
		
		ProcessInstance loanProcess = runtimeService.createProcessInstanceQuery()
				.processInstanceBusinessKey(loanId)
				.singleResult();
		
		execution.setVariable("loanProcessId", (loanProcess != null) ? loanProcess.getId() : null );
		
	}
}
