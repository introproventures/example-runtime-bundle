package org.activiti.cloud.runtime.example;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.codahale.metrics.annotation.Timed;

public abstract class AbstractTimedJavaDelegate implements JavaDelegate {

	@Timed
	@Override
	public abstract void execute(DelegateExecution execution);

}
