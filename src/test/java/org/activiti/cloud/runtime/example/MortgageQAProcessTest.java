package org.activiti.cloud.runtime.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class MortgageQAProcessTest extends PluggableActivitiTestCase {

    @Deployment(resources={"mortgageQA/documentQAProcess.bpmn", "mortgageQA/documentQAProcess.drl", "mortgageQA/loanQAProcess.bpmn", "mortgageQA/loanQAProcess.drl"})
    public void testStartValidDocumentQAProcess() throws Exception
    {
        String loanId = "123456789";
        String documentId = "1234567890";
        String documentCategory = "Loan Application";

    	// given
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("documentId", documentId);
        variableMap.put("documentCategory", documentCategory);
        variableMap.put("loanId", loanId);
        variableMap.put("loanApplicationDate", new Date());
        
        // when
        ProcessInstance documentProcess = runtimeService.startProcessInstanceByKey("documentQAProcess", variableMap);
        assertNotNull(documentProcess.getId());
        String documentProcessId = documentProcess.getId();
        
        // then
        System.out.println("document process id: " + documentProcess.getId() + " " + documentProcess.getProcessDefinitionId());
        Document document = (Document) runtimeService.getVariable(documentProcess.getId(), "document");
        assertThat(document.isValid()).isTrue();
        
        ProcessInstance loanProcess = runtimeService.createProcessInstanceQuery()
        		.processInstanceBusinessKey(loanId)
        		.singleResult();
        
        assertThat(loanProcess).isNotNull();

        while(documentProcess != null) {
        	documentProcess = runtimeService.createProcessInstanceQuery()
            		.processInstanceBusinessKey(documentProcessId)
            		.singleResult();

        	Thread.sleep(1000);
        }
        
        assertThat(runtimeService.createExecutionQuery().processInstanceId(documentProcessId).list()).isEmpty();
        assertThat(runtimeService.createExecutionQuery().processInstanceId(loanProcess.getId()).activityId("receiveDocument").singleResult()).isNotNull();

        Loan loan = (Loan) runtimeService.getVariable(loanProcess.getId(), "loan");
        assertThat(loan.getDocuments()).hasSize(1);
        assertThat(loan.getStatus()).isEqualTo(Loan.STATUS_INCOMPLETE);
        assertThat(loan.isValid()).isFalse();
    }


    @Deployment(resources={"mortgageQA/documentQAProcess.bpmn", "mortgageQA/documentQAProcess.drl", "mortgageQA/loanQAProcess.bpmn", "mortgageQA/loanQAProcess.drl"})
    public void testStartValidLoanQAProcessSync() throws Exception
    {
        // given
        Date loanApplicationDate = new Date();
        String loanIds[] = new String[5];
        for(int i=0; i<loanIds.length; i++) {
        	loanIds[i] = String.format("loan%05d", i+1);
        }
        
        // when
        for(int i=0, j=0; i<loanIds.length; i++, j++) {
        	String documentId = String.format("doc%07d", j+1);
        	String loanId = loanIds[i];
        	
            startDocumentQAProcessSync(documentId, loanId, "Loan Application", loanApplicationDate);
            startDocumentQAProcessSync(documentId, loanId, "Loan Signature", loanApplicationDate);
            startDocumentQAProcessSync(documentId, loanId, "Loan Approval", loanApplicationDate);
            startDocumentQAProcessSync(documentId, loanId, "Loan Completion", loanApplicationDate);
            
            Thread.sleep(50);

            System.out.println(
         		       "----- LoanId="+ loanId +
            		   ", Jobs="+ managementService.createJobQuery().list().size()+
					   ", Timers="+ managementService.createTimerJobQuery().list().size()+
	                   ", Suspended="+ managementService.createSuspendedJobQuery().list().size()+
	                   ", Processes="+ runtimeService.createProcessInstanceQuery().list().size()+
	                   ", Executions="+ runtimeService.createExecutionQuery().list().size());
            
        }
        
        // then
        do {
        	Thread.sleep(1000);
        	System.out.println("----- Sync="+ managementService.createJobQuery().list().size()+
					   ", Timers="+ managementService.createTimerJobQuery().list().size()+
	                   ", Suspended="+ managementService.createSuspendedJobQuery().list().size()+
	                   ", Processes="+ runtimeService.createProcessInstanceQuery().list()+
	                   ", Executions="+ runtimeService.createExecutionQuery().list());
        	
        	// there should be no timers to retry async jobs
        	//assertThat(managementService.createTimerJobQuery().list()).isEmpty();
        } while(!runtimeService.createExecutionQuery().list().isEmpty());
        
        for(String loanId: loanIds) 
        	assertLoanProcessCompleted(loanId);

        assertThat(runtimeService.createExecutionQuery().list()).isEmpty();
        
    }
    
    @Deployment(resources={"mortgageQA/documentQAProcess.bpmn", "mortgageQA/documentQAProcess.drl", "mortgageQA/loanQAProcess.bpmn", "mortgageQA/loanQAProcess.drl"})
    public void testStartValidLoanQAProcessAsync() throws Exception
    {
        // given
        String documentId = "1234567890";
        Date loanApplicationDate = new Date();
        
        // when
        String loanId = "asyn00001";
        startDocumentQAProcessAsync(documentId, loanId, "Loan Application", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId, "Loan Signature", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId, "Loan Approval", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId, "Loan Completion", loanApplicationDate);

        String loanId2 = "asyn00002";
        startDocumentQAProcessAsync(documentId, loanId2, "Loan Application", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId2, "Loan Signature", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId2, "Loan Approval", loanApplicationDate);
        startDocumentQAProcessAsync(documentId, loanId2, "Loan Completion", loanApplicationDate);

        // then
        do {
        	Thread.sleep(1000);
        	System.out.println("----- Async="+ managementService.createJobQuery().list().size()+
					   ", Timers="+ managementService.createTimerJobQuery().list().size()+
	                   ", Suspended="+ managementService.createSuspendedJobQuery().list().size()+
	                   ", Processes="+ runtimeService.createProcessInstanceQuery().list()+
	                   ", Executions="+ runtimeService.createExecutionQuery().list());
        	
        	//assertThat(managementService.createTimerJobQuery().list()).isEmpty();
        } while(!runtimeService.createExecutionQuery().list().isEmpty());
        
        assertLoanProcessCompleted(loanId);
        assertLoanProcessCompleted(loanId2);

        assertThat(runtimeService.createExecutionQuery().list()).isEmpty();
        
    }
    
    private void assertLoanProcessCompleted(String loanId) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
            	.processInstanceBusinessKey(loanId)
            	.singleResult();
        
        assertThat(processInstance).isNotNull();

        HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
            	.processInstanceId(processInstance.getId())
            	.variableName("loan")
            	.singleResult();
        
        Loan loan = (Loan) variableInstance.getValue();
        assertThat(loan.getDocuments()).hasSize(4);
        assertThat(loan.getStatus()).isEqualTo(Loan.STATUS_COMPLETE);
        assertThat(loan.isValid()).isTrue();
    	
    }
    
    private void startDocumentQAProcessAsync(String documentId, String loanId, String documentCategory, Date loanApplicationDate) {
        new Thread(new Runnable() {
        	
			@Override
			public void run() {
				startDocumentQAProcessSync(documentId, loanId, documentCategory, loanApplicationDate);
			}
        	
        }).start();
    	
    }

    private void startDocumentQAProcessSync(String documentId, String loanId, String documentCategory, Date loanApplicationDate) {
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("documentId", documentId);
        variableMap.put("documentCategory", documentCategory);
        variableMap.put("loanId", loanId);
        variableMap.put("loanApplicationDate", loanApplicationDate);

        runtimeService.startProcessInstanceByKey("documentQAProcess", variableMap);
    }
    
    @Deployment(resources={"mortgageQA/documentQAProcess.bpmn", "mortgageQA/documentQAProcess.drl", "mortgageQA/loanQAProcess.bpmn", "mortgageQA/loanQAProcess.drl"})
    public void testStartInvalidDocumentQAProcess() throws Exception
    {
        String loanId = "invalid";
        String documentId = "invalid";
        String documentCategory = "Loan Something";

    	// given
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("documentId", documentId);
        variableMap.put("documentCategory", documentCategory);
        variableMap.put("loanId", loanId);
        variableMap.put("loanApplicationDate", new Date());
        
        // when
        ProcessInstance documentProcess = runtimeService.startProcessInstanceByKey("documentQAProcess", variableMap);
        
        // then
        assertNotNull(documentProcess.getId());
        HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
        	.processInstanceId(documentProcess.getId())
        	.variableName("document")
        	.singleResult();        
        
        Document document = (Document) variableInstance.getValue();
        assertThat(document.getStatus()).isEqualTo(Document.STATUS_ERROR);
        
        assertThat(runtimeService.createExecutionQuery().list()).isEmpty();
    }    
}
