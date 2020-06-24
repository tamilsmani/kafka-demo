package com.example.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
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
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
public class KafaRebalancingListenerConfiguration  {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafaRebalancingListenerConfiguration.class);
	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}-event-message")
	private String topic;
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	@Autowired
	private ConsumerAwareRebalanceListenerImpl consumerAwareRebalanceListener;
	
	private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<TopicPartition, OffsetAndMetadata>();
	
	@Bean
	public KafkaListenerErrorHandler errorHandler() {
		return new KafkaListenerErrorHandler() {
		@Override
			public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
				LOGGER.info("In Error handler []", ((MethodArgumentNotValidException)exception.getCause()).getBindingResult());
				return null;
			}
		};
	}
	
	
	@KafkaListener(id="${kafka.topic}", topics = "${kafka.topic}",	
						autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:3}",
						containerFactory = "kafkaListenerContainerFactory",
						errorHandler = "errorHandler")
	public void listen(@Payload String payload,
	        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
	        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	        @Header(KafkaHeaders.CONSUMER) KafkaConsumer<String, String> consumer,
	        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
	        @Header(KafkaHeaders.OFFSET) long offset) {
		
		consumerAwareRebalanceListener.perisitOffsetInfo(topic, partition, offset);
		
		//LOGGER.info("Message-without-groupid consumed from group[{}] from topic [{}] and payload[{}]", KafkaUtils.getConsumerGroupId(), topic, payload);
		//LOGGER.info("Received message info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition, key);
		//ack.acknowledge();
	}
		
	public void send(String payload) {
		LOGGER.info("Message sent to topic [{}] and payload[{}]", topic, payload);
		kafkaTemplate.send(topic, payload);
		
	}
	
	// --------------------- Consumer config
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // These attributes cann be overridden @KafkaListener annotation
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting AckMode Manual you can get the reference of Ack in the listener method args.
        factory.getContainerProperties().setConsumerRebalanceListener(consumerAwareRebalanceListener);
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