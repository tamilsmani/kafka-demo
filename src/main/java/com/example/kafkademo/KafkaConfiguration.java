package com.example.kafkademo;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Component
@EnableKafka
public class KafkaConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfiguration.class);

	@Value("${kafka.servers}")
	private String kafaServers;

	@Value("${kafka.topic}")
	private String topic;

	@KafkaListener(groupId = "${kafka.topic}-group", 
			topics = "${kafka.topic}", autoStartup = "${listen.auto.start:true}", 
			concurrency = "${listen.concurrency:1}", 
			containerFactory = "kafkaListenerContainerFactory")
	@Transactional(transactionManager = "kafkaTransactionManager")
	public void listen(@Payload String payload, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Header(KafkaHeaders.CONSUMER) KafkaConsumer<String, String> consumer,
			@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts, @Header(KafkaHeaders.OFFSET) long offset)
			throws Exception {

		LOGGER.info(">>>>>> Received transactional message(in-transaction) from topic[{}] and payload[{}]", topic,
				payload);

	}

	// --------------------- Consumer config
	@Bean
	public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			ConsumerFactory<String, String> kafkaConsumerFactory, KafkaTransactionManager<String,String> kafkaTransactionManager) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.getContainerProperties().setTransactionManager((PlatformTransactionManager)kafkaTransactionManager);
		// These attributes cann be overridden @KafkaListener annotation
		// factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting
		// AckMode Manual you can get the reference of Ack in the listener method args.
		factory.setConsumerFactory(kafkaConsumerFactory);
		return factory;
	}

	@Bean
	public KafkaTransactionManager<String, String> kafkaTransactionManager(
			ProducerFactory<String, String> producerFactory) {
		KafkaTransactionManager<String, String> ktm = new KafkaTransactionManager<String, String>(producerFactory);
		ktm.setNestedTransactionAllowed(true);
		ktm.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		return ktm;
	}

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(config());
	}

	// --------------------- Producer Config
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
		return new KafkaTemplate<String, String>(producerFactory, true);
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(config());
		producerFactory.setTransactionIdPrefix("tpp-trans-tx");
		return producerFactory;
	}

	@Bean
	public Map<String, Object> config() {
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafaServers);
		//configMap.put(ProducerConfig.RETRIES_CONFIG, 1);
		//configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);

		configMap.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		//configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		//configMap.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

		// introduce a delay on the send to allow more messages to accumulate
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		// records
		//configMap.put(ConsumerConfig.GROUP_ID_CONFIG, topic);
		// automatically reset the offset to the earliest offset
		configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		return configMap;

	}
}