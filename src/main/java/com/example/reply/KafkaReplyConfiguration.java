package com.example.reply;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaReplyConfiguration {

	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;
	
	@Value("${kafka.reply.topic}")	
	private String replyTopic;
	
	@Value("${kaffa.topic.group.id}")
	private String replyTopicGroupId;
	
//	@Bean
//	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
//		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//		factory.setConsumerFactory(consumerFactory());
//		factory.setReplyTemplate(kafkaTemplate());
//		return factory;
//	}

	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }
	
	@Bean
	public ReplyingKafkaTemplate<String, String, String> replyKafkaTemplate(ProducerFactory<String, String> producerFactory,
			ConcurrentMessageListenerContainer<String, String> replyContainer) {

		return new ReplyingKafkaTemplate<String, String, String>(producerFactory, replyContainer);
	}

	@Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {

        ConcurrentMessageListenerContainer<String, String> repliesContainer = containerFactory.createContainer(replyTopic);
        repliesContainer.getContainerProperties().setGroupId(replyTopicGroupId);
        repliesContainer.setAutoStartup(true);
        return repliesContainer;
    }
	
//	@Bean
//	public KafkaMessageListenerContainer<String, String> replyContainer(ConsumerFactory<String, String> consumerFactory) {
//		ContainerProperties containerProperties = new ContainerProperties(replyTopic);
//		
//		return new KafkaMessageListenerContainer<String, String>(consumerFactory, containerProperties);
//	}

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(config());
	}

	// Sending an message
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<String, String>(createProducerFactory());
	}

	@Bean
	public ProducerFactory<String, String> createProducerFactory() {
		return new DefaultKafkaProducerFactory<>(config());
	}

	@Bean
	public Map<String, Object> config() {
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafaServer);
		configMap.put(ProducerConfig.RETRIES_CONFIG, 0);
		configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);

		configMap.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

//			configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafaServer);
//			configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//			configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//			configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//			configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		// introduce a delay on the send to allow more messages to accumulate
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		// records
		configMap.put(ConsumerConfig.GROUP_ID_CONFIG, topic);
		// automatically reset the offset to the earliest offset
		configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		return configMap;

	}

}