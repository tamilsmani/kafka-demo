package com.example.consumer.xml;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class Receiver implements MessageHandler{
	private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);
	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;
	
	@Value("${kaffa.topic.group.id}")
	private String groupId;
	
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		LOGGER.info(">>>>>>>>>>>>>>>>>>>>Received message from topic[{}] and payload[{}]", message.getPayload());
	}
	
    @KafkaListener(id = "${kaffa.topic.group.id}", topics = "${kafka.topic}")
    public void receive(ConsumerRecord<?, ?> cr) {
    	LOGGER.info(">>>>>>>>>>>>>>>>>>>>Received message via receive method from topic[{}] and payload[{}]", topic , cr.value());
    }
    
//    @KafkaListener(id = "${kaffa.topic.group.id}", topics = "${kafka.topic}")
//    public void receive(@Payload String payload,
//                        @Headers MessageHeaders headers) {
//
//    }
    
}
