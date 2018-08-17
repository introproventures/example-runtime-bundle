package org.activiti.cloud.runtime.example;

import org.activiti.engine.delegate.DelegateExecution;

public class ProcessInputDocumentJavaDelegate extends AbstractTimedJavaDelegate {
	@Override
	public void execute(DelegateExecution execution) {
	    Document document =  new Document(
	    		(String) execution.getVariable("documentId"),
	    		(String) execution.getVariable("loanId"),
	    		(String) execution.getVariable("documentCategory"),
	    		Document.STATUS_NEW
	    );
	    
		execution.setVariable("document", document);
	}
}
