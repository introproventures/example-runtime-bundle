package org.activiti.cloud.runtime.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BpmnDeployerRestController {

    private final RepositoryService repositoryService;
    
    public BpmnDeployerRestController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
    
    @PostMapping(value="/v1/extension/deploy-bpmn/{deploymentName}/{resourceName}", consumes=MediaType.APPLICATION_XML_VALUE)
    public String deployBpmn(@PathVariable String deploymentName, 
                             @PathVariable String resourceName, 
                             @RequestBody String bpmnModelXml) {
        return doDeployBpmn(deploymentName, resourceName, new ByteArrayInputStream(bpmnModelXml.getBytes()));
    }
    
    public String doDeployBpmn(String deploymentName, String resourceName, InputStream xmlStream) {
        
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .name(deploymentName)
                .addInputStream(resourceName, xmlStream);
        
        Deployment deployment = deploymentBuilder.deploy();
        
        return deployment.getId();
    }    
}
