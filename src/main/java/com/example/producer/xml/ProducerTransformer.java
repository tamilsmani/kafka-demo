package com.example.producer.xml;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class ProducerTransformer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProducerTransformer.class);
	
	@Value("${kafka.topic}")
	private String topic;
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	public Message<?> send(Message<?> payload) {
		
		LOGGER.info(">>>>>> Publishing Spring message to topic[{}] and payload[{}]", topic, payload);
		// kafkaTemplate.send(topic, payload);
		return payload;
	}
	
	public void sendValue(String payload) {
		
		ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload);
		LOGGER.info("Sent message to topic[{}] and payload[{}]", topic, payload);
		
		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
		    @Override
		    public void onSuccess(SendResult<String, String> result) {
		    	LOGGER.info(">>> Success call back from inside send method [{}]", result);
		    }

		    @Override
		    public void onFailure(Throwable ex) {
		    	LOGGER.info(">>> Failure call back[{}]", ex.getMessage());
		    }

		});
		
	}
	
	public void sendWithKey(String key, String payload) {
		
		ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);
		LOGGER.info("Sent message to topic[{}] and payload[{}]", topic, payload);
		
		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
		    @Override
		    public void onSuccess(SendResult<String, String> result) {
		    	LOGGER.info(">>> Success call back from inside send method [{}]", result);
		    }

		    @Override
		    public void onFailure(Throwable ex) {
		    	LOGGER.info(">>> Failure call back[{}]", ex.getMessage());
		    }

		});
		
	}

	public void process(Message<?> payload) {
		LOGGER.info(">>>>>> Sending message[{}]", payload);
		ProducerRecord<String,Message<?>> producerRecord = new ProducerRecord<String, Message<?>>(topic, payload);
		//producerRecord.headers().add(key, value)
		//ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload);
		ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(payload);
		
		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
		    @Override
		    public void onSuccess(SendResult<String, String> result) {
		    	LOGGER.info(">>> Success call back[{}]", result);
		    }

		    @Override
		    public void onFailure(Throwable ex) {
		    	LOGGER.info(">>> Failure call back[{}]", ex.getMessage()); 
		    }

		});
		LOGGER.info("Sent message to topic[{}] and payload[{}]", topic, payload);
	}
	
}