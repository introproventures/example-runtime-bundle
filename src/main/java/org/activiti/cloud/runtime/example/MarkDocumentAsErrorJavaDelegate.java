package org.activiti.cloud.runtime.example;

import org.activiti.engine.delegate.DelegateExecution;

public class MarkDocumentAsErrorJavaDelegate extends AbstractTimedJavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		Document document = (Document) execution.getVariable("document");
		
		document.setStatus(Document.STATUS_ERROR);
		
		execution.setVariable("document", document);
	}

}
