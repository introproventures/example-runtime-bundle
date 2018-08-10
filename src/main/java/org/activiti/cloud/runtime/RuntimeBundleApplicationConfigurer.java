package org.activiti.cloud.runtime;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeBundleApplicationConfigurer implements ProcessEngineConfigurationConfigurer {
	
	@Override
	public void configure(SpringProcessEngineConfiguration configuration) {
		configuration.setAsyncExecutorActivate(true);
	}
	
}