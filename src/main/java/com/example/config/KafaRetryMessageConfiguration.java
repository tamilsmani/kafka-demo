package com.example.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * Retry - 2 times.
 * Recovery callback configured
 * RetryListener configured
 
 
 S13). Retry Configuration
---------------------------------
	- RetryTemplate should be configured with RetryPolicy & BackoffPolicy.
	- We can configure retrylistener on the retry template to trace the rettry open, onerror & close.
	- When all the retries are exhausted then it calls RecoveryCallback.
	- In the recovery call be we get consumer Record / Acknowledgement object.
	- Here we can move to another topic / DLX.
	
	Auto offset commit / Manual offset commit
	------------------------------------------
		- If the consumer factory is configured with Auto ACK mode   then on exeuction of RecoveryCallback-> recover method the offset will be committed.
		- If the consumer factory is configured with MANUAL ACK mode then on exeuction of RecoveryCallback-> recover method the offset will NOT be committed. To do that, we have to get the Acknowledgment Object and manually ACK the offset.
 
 	1). - factory.getContainerProperties().setAckMode(AckMode.MANUAL); // IN CASE DO U NEED MANUAL ACK OR YOU DON'T WANT TO COMMIT WHEN AN EXCEPTION (That case Manual ACK is required.)
 
 */
@Component
@EnableKafka
public class KafaRetryMessageConfiguration  {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafaRetryMessageConfiguration.class);
	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;
	
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	@Bean
	public KafkaListenerErrorHandler errorHandler() {
		return new KafkaListenerErrorHandler() {
		@Override
			public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
				LOGGER.info("In Custom Error handler []", exception.getCause());
				throw exception;
			}
		};
	}

	@KafkaListener(id="${kafka.topic}-endpoint-id", groupId = "${kafka.topic}", topics = "${kafka.topic}",
						autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:3}",
						containerFactory = "kafkaListenerContainerFactory"
						//errorHandler = "errorHandler"
						
//						topicPartitions = @TopicPartition(topic = "${kafka.topic}"
//						  	partitionOffsets = {
//						  			@PartitionOffset(partition = "1", initialOffset = "15"), 
//							}
//						)
		)
	//@Transactional(transactionManager = "kafkaTransactionManager")
	public void listen(@Payload String payload,
	        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
	        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	        @Header(KafkaHeaders.CONSUMER) KafkaConsumer<String, String> consumer,
	        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
	        @Header(KafkaHeaders.OFFSET) long offset) throws Exception {
		
		
		LOGGER.info("Message offset[{}] parition[{}]", offset, partition);
		LOGGER.info("Received message topic[{}] and payload[{}] & throws Exception",topic, payload);
		if(true) {
			throw new InterruptedException("Manually throwing an exception");
		}
		//ack.acknowledge();
	}
	
	@Transactional(transactionManager = "kafkaTransactionManager")
	public void send(String payload) {
		LOGGER.info("Message sent to topic [{}] and payload[{}]", topic, payload);
		kafkaTemplate.send(topic, payload);
		
	}
	
	// --------------------- Consumer config
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(RetryTemplate retryTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // These attributes cann be overridden @KafkaListener annotation
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting AckMode Manual you can get the reference of Ack in the listener method args.
        factory.setConsumerFactory(consumerFactory());
        //factory.setConcurrency(3);
        
        factory.setRetryTemplate(retryTemplate);
        // What we should do when all retries are failed.
        
        
        /**
         * ***In case of factory.getContainerProperties().setAckMode(AckMode.MANUAL); the below steps helps to ack the offset to the container.. ***

If you have a RecoveryCallback that handles the record when retries are exhausted, and you are NOT using manual acks, the container will commit the offset.
If you are using MANUAL acks, you can commit the offset in the recovery callback.

The context object passed to the callback has an attribute RetryingMessageListenerAdapter.CONTEXT_ACKNOWLEDGMENT ('acknowledgment').
It also has CONTEXT_CONSUMER and CONTEXT_RECORD.
         */
        factory.setRecoveryCallback(new RecoveryCallback<String>() {
        	@Override
        	public String recover(RetryContext context) throws Exception {
        		LOGGER.error("Recovery call back...[{}]", context.getLastThrowable());
                ConsumerRecord<String,String> record = (ConsumerRecord<String,String>) context.getAttribute(RetryingMessageListenerAdapter.CONTEXT_RECORD);
                //kafkaTemplate.send(record.topic(), record.value());
                Acknowledgment ack = (Acknowledgment) context.getAttribute(RetryingMessageListenerAdapter.CONTEXT_ACKNOWLEDGMENT);
                ack.acknowledge();
                
                return null;
        	}
        });
        return factory;
        
    }
	
	@Bean
	public RetryTemplate retryTemplate() {
		  Map<Class<? extends Throwable>, Boolean> retryMapping = new HashMap<>();
		  retryMapping.put(ListenerExecutionFailedException.class, true); // Spring Kafa wrapped the execption with ListenerExecutionFailedException
		  
		  
		  RetryTemplate retryTemplate = new RetryTemplate();
		  
		  FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		  fixedBackOffPolicy.setBackOffPeriod(1000l);
	
		  SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(2, retryMapping);
		  
		  retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
		  retryTemplate.setRetryPolicy(retryPolicy);
		  
		  retryTemplate.registerListener(new RetryListener() {
					@Override
					public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
						LOGGER.info("Inside retry-open [{}] & callback[{}]", context, callback);
						return true;
					}
					
					@Override
					public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
							Throwable throwable) {
						LOGGER.info("Inside retry-onError [{}] & callback[{}] & exception[{}]", context, callback, throwable);
						
					}
					
					@Override
					public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
							Throwable throwable) {
						LOGGER.info("Inside retry-close [{}] & callback[{}] & exception[{}]", context, callback, throwable);
						
					}
		});
		  
		  
//		  retryTemplate.execute(new RetryCallback() {
//			  @Override
//			public Object doWithRetry(RetryContext context) throws Throwable {
//				LOGGER.info("Inside retry-close [{}] & callback[{}] & exception[{}]", context, callback, throwable);
//				return null;
//			}
//		  }, new RecoveryCallback() {
//			  @Override
//			  public String recover(RetryContext context) throws Exception {
//	        		LOGGER.error("Recovery call back...[{}]", context);
//	                ConsumerRecord<String,String> record = (ConsumerRecord<String,String>) context.getAttribute(RetryingMessageListenerAdapter.CONTEXT_RECORD);
//	                //kafkaTemplate.send(record.topic(), record.value());
//	                return null;
//	        	}
//		  }, retryState)
		  
		  return retryTemplate;
	};
	
	@Bean(name = "kafkaTransactionManager")
	public KafkaTransactionManager<String, String> kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {
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
		return new KafkaTemplate<String, String>(producerFactory);
	}
	
	@Bean
	public ProducerFactory<String, String> producerFactory() {
		DefaultKafkaProducerFactory<String, String> producerFactory =  new DefaultKafkaProducerFactory<>(config());
		producerFactory.setTransactionIdPrefix("tamil-tx");
		return producerFactory;
		
//		return new DefaultKafkaProducerFactory<>(config(),new CustomKeySerializer(), new CustomValueSerializer());
	}

	@Bean
	public Map<String, Object> config() {
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafaServer);
		configMap.put(ProducerConfig.RETRIES_CONFIG, 1);
		configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		configMap.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100000);
		
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
		//configMap.put(ConsumerConfig.GROUP_ID_CONFIG, topic);
		// automatically reset the offset to the earliest offset
		//configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		return configMap;

	}
}