package com.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
//public class CustomEventListener implements ApplicationListener<CustomEvent> {
public class CustomEventListener  {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventListener.class);
	
	@Value("${kafka.reply.topic}")
	private String replyTopic;
	
//	@Autowired
//	@Qualifier("kafkaTemplate1")
	private KafkaTemplate<String,String> kafkaTemplate;
	
	public CustomEventListener() {
		System.out.println("Inside CustomEventListener");
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onApplicationEvent(CustomEvent event) {
		LOGGER.info("Inside Event Listener...");
		System.out.println("Source => " + event.getSource());
		System.out.println("Msg = > " + event.getMsg());
		
		LOGGER.info("Publishing to Kafka after DB commit ...");
		//kafkaTemplate.send(replyTopic, event.getMsg());
		LOGGER.info("published to kafka after DB commit..");
	}
}