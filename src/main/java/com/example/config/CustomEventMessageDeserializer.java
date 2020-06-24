package com.example.config;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomEventMessageDeserializer implements Deserializer<EventMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventMessageDeserializer.class);
	
	@Override
	public EventMessage deserialize(String arg0, byte[] devBytes) {
		ObjectMapper mapper = new ObjectMapper();
		EventMessage developer = null;
		try {
			developer = mapper.readValue(devBytes, EventMessage.class);
		} catch (Exception e) {
			LOGGER.error("Error while deserializing the object[{}]", e);
		}
		return developer;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

}
