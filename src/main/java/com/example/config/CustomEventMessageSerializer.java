package com.example.config;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomEventMessageSerializer implements Serializer<EventMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventMessageDeserializer.class);
	
	@Override
	public byte[] serialize(String topic, EventMessage data) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsBytes(data);
		} catch (Exception e) {
			LOGGER.error("Error while serializing the object[{}]", e);
		}
		return null;
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
