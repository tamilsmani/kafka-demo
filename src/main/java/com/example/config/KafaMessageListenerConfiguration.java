package com.example.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 
 * 1. To work on @KafkaListener, we need to enable @EnableKafka
 * 2. KafkaHeaders can be annotated with method args.
		KafkaHeaders.OFFSET
		KafkaHeaders.RECEIVED_MESSAGE_KEY
		KafkaHeaders.RECEIVED_TOPIC
		KafkaHeaders.RECEIVED_PARTITION_ID
		KafkaHeaders.RECEIVED_TIMESTAMP
		KafkaHeaders.TIMESTAMP_TYPE
		
	Manual Ack
	---------
	
	
 *
 */
@Component
@EnableKafka
public class KafaMessageListenerConfiguration {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafaMessageListenerConfiguration.class);
	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	// Non batch.. Consume message without Group ID
	@KafkaListener(topics = "${kafka.topic}",	
			autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:3}",
			containerFactory = "kafkaListenerContainerFactory")
	public void listenWihoutGroupId(@Payload String payload,
											@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
									        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
									        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
									        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
									        @Header(KafkaHeaders.OFFSET) long offset
									        ,Acknowledgment ack
									        ) {
		
		LOGGER.info("Message-without-groupid consumed from group[{}] from topic [{}] and payload[{}]", KafkaUtils.getConsumerGroupId(), topic, payload);
		LOGGER.info("Received-without-groupid message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, ack);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
		
	// Non batch.. Consume message with Group ID
	@KafkaListener(id = "${kaffa.topic.group.id}", 		topics = "${kafka.topic}",
			autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:3}",
			containerFactory = "kafkaListenerContainerFactory")
	public void listen(@Payload String payload, 
											@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
									        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
									        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
									        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
									        @Header(KafkaHeaders.OFFSET) long offset
									        ,Acknowledgment ack
								) {
		
		LOGGER.info("Message consumed from topic [{}] and payload[{}]", topic, payload);
		LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key, ack);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
	
	
	// Batch Listener with different payload - Start
	
	@KafkaListener(id = "${kaffa.topic.batch.group.id}", 		topics = "${kafka.topic}",
			autoStartup = "${listen.auto.start:true}", 			concurrency = "${listen.concurrency:3}",
			containerFactory = "kafkaBatchListenerContainerFactory")
	public void listenAsBatch(@Payload List<String> payloadList,
									        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partition,
									        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
									        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) List<Long> ts,
									        @Header(KafkaHeaders.OFFSET) List<Long> offset
									        ,Acknowledgment ack
									        ) throws Exception {
		TimeUnit.SECONDS.sleep(5);
		LOGGER.info(">>> Message list[{}] consumed from topic [{}] and payload[{}]", payloadList.size(), topic, payloadList);
		LOGGER.info(">>> Received list of message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, ack);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
	
	@KafkaListener(id = "${kaffa.topic.batch.group.id}.LM", 		topics = "${kafka.topic}",
			autoStartup = "${listen.auto.start:true}", 			concurrency = "${listen.concurrency:3}",
			containerFactory = "kafkaBatchListenerContainerFactory")
	public void listenAsBatchMessage(@Payload List<Message<?>> payloadList, Acknowledgment ack) throws Exception {
		TimeUnit.SECONDS.sleep(5);
		LOGGER.info(">>> Message(Batch-Message)  list[{}] consumed from topic [{}] and payload[{}]", 
				KafkaUtils.getConsumerGroupId() , payloadList.size(), topic, payloadList);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
	
	@KafkaListener(id = "${kaffa.topic.batch.group.id}.CR", 		topics = "${kafka.topic}",
			autoStartup = "${listen.auto.start:true}", 			concurrency = "${listen.concurrency:3}",
			containerFactory = "kafkaBatchListenerContainerFactory")
	public void listenAsBatchConsumerRecord(@Payload List<ConsumerRecord<String,String>> payloadList, Acknowledgment ack) throws Exception {
		TimeUnit.SECONDS.sleep(5);
		LOGGER.info(">>> Message(Consumer_Record)  list[{}] consumed from topic [{}] and payload[{}]", payloadList.size(), topic, payloadList);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
	// Batch Listener with different payload - End
	
	public void send(String payload) {
		LOGGER.info("Message sent to topic [{}] and payload[{}]", topic, payload);
		kafkaTemplate.send(topic, "listener-key", payload);
		
	}
	
	// --------------------- Consumer config (Batch listener config)
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaBatchListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // These attributes cann be overridden @KafkaListener annotation
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting AckMode Manual you can get the reference of Ack in the listener method args.
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.setBatchListener(true);
        return factory;
        
    }
	
	// --------------------- Consumer config
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // These attributes cann be overridden @KafkaListener annotation
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting AckMode Manual you can get the reference of Ack in the listener method args.
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
        
    }
	
	@Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(config());
    }
	
	// --------------------- Producer Config
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<String, String>(producerFactory());
	}
	
	@Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(config());
//		return new DefaultKafkaProducerFactory<>(config(),new CustomKeySerializer(), new CustomValueSerializer());
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
		configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		    
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