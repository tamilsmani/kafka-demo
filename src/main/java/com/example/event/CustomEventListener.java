package com.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventListener.class);
	
	@Override
	@TransactionalEventListener
	public void onApplicationEvent(CustomEvent event) {
		LOGGER.info("Event Received - {}", event.getMsg());
	}
}