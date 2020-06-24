package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterceptorBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorBean.class);
	
	public void display(String where) {
		LOGGER.info("Called from [{}]",where);
	}
}