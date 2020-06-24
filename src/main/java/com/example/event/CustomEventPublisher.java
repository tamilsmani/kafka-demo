package com.example.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CustomEventPublisher {
	
	CustomEventPublisher() {
		System.out.println("in");
	}
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	public void publish(String msg) {
		applicationEventPublisher.publishEvent(new CustomEvent(this, msg));
	}
}