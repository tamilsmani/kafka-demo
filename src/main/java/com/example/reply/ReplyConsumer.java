package com.example.reply;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class ReplyConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplyConsumer.class);
	
	@Value("${kafka.reply.topic}")
	private String replyTopic;
	
	@KafkaListener(topics = "${kafka.reply.topic}", containerFactory = "kafkaListenerContainerFactory")
	@SendTo
	public String replyMessage(String payload) throws InterruptedException {
		LOGGER.info("Reply topic message[{}]" , payload);
		return payload + "-REPLY";
	}

	
	@Bean
    public NewTopic kReplies() {
        return TopicBuilder.name(replyTopic)
            .partitions(3)
            .replicas(3)
            .build();
    }
}