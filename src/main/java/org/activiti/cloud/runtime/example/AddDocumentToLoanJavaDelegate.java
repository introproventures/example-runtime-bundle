package org.activiti.cloud.runtime.example;

import org.activiti.engine.delegate.DelegateExecution;

public class AddDocumentToLoanJavaDelegate extends AbstractTimedJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		Loan loan = (Loan) execution.getVariable("loan");
		Document document = (Document) execution.getVariable("document");
		
		loan.getDocuments().add(document);
		
		execution.setVariable("loan", loan);
	}
}
