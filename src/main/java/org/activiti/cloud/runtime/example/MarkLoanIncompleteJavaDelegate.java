package org.activiti.cloud.runtime.example;

import org.activiti.engine.delegate.DelegateExecution;

public class MarkLoanIncompleteJavaDelegate extends AbstractTimedJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		Loan loan = (Loan) execution.getVariable("loan");
		
		loan.setStatus(Loan.STATUS_INCOMPLETE);		
		
		execution.setVariable("loan", loan);
	}

}
