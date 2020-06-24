package com.example.config;

import javax.validation.constraints.Size;

public class EventMessage {

	@Size(max=2)
	private String payload;

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "EventMessage [payload=" + payload + "]";
	}
	
}
