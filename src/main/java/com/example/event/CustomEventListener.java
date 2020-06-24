package com.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomEventListener.class);
	
	@Value("${kafka.reply.topic}")
	private String replyTopic;
	
	@Autowired
	@Qualifier("kafkaTemplate1")
	private KafkaTemplate<String,String> kafkaTemplate;
	
	public CustomEventListener() {
		System.out.println("Inside con");
	}
	
	@Override
	@TransactionalEventListener
	public void onApplicationEvent(CustomEvent event) {
		LOGGER.info("Receving after DB commit so transaction is in sync...");
		System.out.println("Source => " + event.getSource());
		System.out.println("Msg = > " + event.getMsg());
		
		LOGGER.info("Publishing after DB commit in sync...");
		kafkaTemplate.send(replyTopic, event.getMsg());
		LOGGER.info("published after DB commit in sync...");
	}
}