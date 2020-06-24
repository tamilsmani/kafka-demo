package com.example.config;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomValueSerializer implements Supplier<Serializer<String>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomKeySerializer.class);
	
	@Override
	public Serializer<String> get() {
		
		return new Serializer<String>() {
			
			@Override
			public void configure(Map<String, ?> configs, boolean isKey) {
				LOGGER.info("Inside value serializer configure method => [{}] " + configs);
				Serializer.super.configure(configs, isKey);
			}
			
			@Override
			public byte[] serialize(String topic, String data) {
				if(data != null) {
					LOGGER.info("Inside value serialize method => [{}] " + data);
					
					return data.getBytes();
					
//				    ObjectMapper objectMapper = new ObjectMapper();
//				    try {
//				      return  objectMapper.writeValueAsString(data).getBytes();
//				    } catch (Exception e) {
//				    	LOGGER.error("Error while serializing....");
//				      e.printStackTrace();
//				    }
//				    return null;
				}
				
				return null;
			}
			
			@Override
			public void close() {
			}
		};
		
	}
}