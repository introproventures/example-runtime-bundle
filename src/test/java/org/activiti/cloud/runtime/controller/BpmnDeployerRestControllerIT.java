package org.activiti.cloud.runtime.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class BpmnDeployerRestControllerIT {
    
    @Value("classpath:/org/activiti/cloud/runtime/controller/TestProcess.bpmn20.xml")
    Resource bpmnFile;
    
    @Autowired
    private BpmnDeployerRestController bpmnDeployerRestController;
    
    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskService taskService;
    
    @Test
    public void testBpmnDeploy() throws IOException {
        // given
        String deploymentName = "test-deployment";
        String resourceName = "TestProcess.bpmn20.xml"; // Should have .bpmn suffix or .bpmn20.xml to trigger deployment
        
        // when
        String deploymentId = bpmnDeployerRestController.doDeployBpmn(deploymentName, resourceName, bpmnFile.getInputStream());
        
        // then
        assertThat(deploymentId).isNotNull();
        
        // and when
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                                                                      .deploymentId(deploymentId)
                                                                      .list();
        
        // then
        assertThat(processDefinitions).hasSize(1);
        
        // given
        ProcessDefinition processDefinition = processDefinitions.get(0);
        
        // when
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
            .processDefinitionId(processDefinition.getId())
            .name("test-process-instance")
            .start();
        
        // then
        assertThat(runtimeService.createProcessInstanceQuery().list()).hasSize(1);
        
        // and given
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        
        // when
        taskService.complete(task.getId());

        // then
        assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult()).isNull();
        
        // Cloud connector service task should be active 
        assertThat(runtimeService.createProcessInstanceQuery().list()).hasSize(1);
        
    }

}
