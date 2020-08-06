package com.example.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.example.event.CustomEventPublisher;

@Component
@EnableKafka
@EnableTransactionManagement
public class KafaTransactionConfiguration  {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafaTransactionConfiguration.class);
	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;

	@Value("${kafka.reply.topic}")
	private String replyTopic;
	
	@Autowired
	@Qualifier("kafkaTemplate1")
	private KafkaTemplate<String,String> kafkaTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	CustomEventPublisher customEventPublisher;
	
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
						autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:1}",
						containerFactory = "kafkaListenerContainerFactory",
						errorHandler = "errorHandler")
	@Transactional(transactionManager = "chainedKafkaTransactionManager")
	public void listen(@Payload String payload,
	        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
	        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	        @Header(KafkaHeaders.CONSUMER) KafkaConsumer<String, String> consumer,
	        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
	        @Header(KafkaHeaders.OFFSET) long offset) throws Exception {
		
		//LOGGER.info("Message-without-groupid consumed from group[{}] from topic [{}] and payload[{}]", KafkaUtils.getConsumerGroupId(), topic, payload);
		LOGGER.info(">>>>>> Received transactional message(in-transaction) from topic[{}] and payload[{}]",topic, payload);
		send(payload);
		
	}
	
//	@KafkaListener(id="${kafka.reply.topic}", topics = "${kafka.reply.topic}",	
//			autoStartup = "${listen.auto.start:true}", 	concurrency = "${listen.concurrency:1}",
//			containerFactory = "kafkaListenerContainerFactory",
//			errorHandler = "errorHandler")
//	public void sendTolisten(@Payload String payload,
//								@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
//								@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//								@Header(KafkaHeaders.CONSUMER) KafkaConsumer<String, String> consumer,
//								@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
//								@Header(KafkaHeaders.OFFSET) long offset) {
//	
//			//LOGGER.info("Message-without-groupid consumed from group[{}] from topic [{}] and payload[{}]", KafkaUtils.getConsumerGroupId(), topic, payload);
//			LOGGER.info("Inside sendTo message inside transaction - info offset[{}], Parition[{}], key[{}], timestamp[{}] and Ack[{}]", offset, partition);
//			//ack.acknowledge();
//	}
	
	//@Transactional(transactionManager = "chainedKafkaTransactionManager")
	public void send(String payload) throws Exception {
		LOGGER.info("..........////////// Message sending to topic [{}] and payload[{}]", topic, payload);
		jdbcTemplate.update("insert into task(title) values ('" + new Date() + "')");
		
			// Publishing can not be in transaction becoz the chainedKafkaTransactionManager is between Message consumer & DB.
		//kafkaTemplate.send(replyTopic, payload);
			//So we can use Spring Event publisher.
		customEventPublisher.publish(payload + "=> Consumed & DB insert is success" );
		
		
		System.out.println(kafkaTemplate.inTransaction());
		LOGGER.info("DB insert statement executed...");
		//LOGGER.info("Sent to DB & Another topic");
		//TimeUnit.SECONDS.sleep(5);
		
	}
	
	@Bean(name="jdbcTemplate")
	public JdbcTemplate jdbcTemplate(DataSource mysqlDataSource) {
		return new JdbcTemplate(mysqlDataSource);
	}
	
	@Bean(name = "dataSource")
	@Primary
    public DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/kafka");
        dataSource.setUsername("root");
        dataSource.setPassword("June1980#");
 
        return dataSource;
    }
	 
	// --------------------- Consumer config
	
	@Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<String,String> kafkaConsumerFactory, ChainedKafkaTransactionManager<Object, Object> chainedTM) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setTransactionManager((PlatformTransactionManager)chainedTM);
        
        // These attributes cann be overridden @KafkaListener annotation
      //  factory.getContainerProperties().setAckMode(AckMode.MANUAL); // By setting AckMode Manual you can get the reference of Ack in the listener method args.
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setConcurrency(3);
        
        return factory;
    }
	
	@Bean(name="chainedKafkaTransactionManager")
    public ChainedKafkaTransactionManager<Object, Object> chainedTm(KafkaTransactionManager<String, String> ktm, DataSourceTransactionManager dstm) {
        return new ChainedKafkaTransactionManager<>(ktm, dstm);
    }
	
	@Bean
	public KafkaTransactionManager<String, String> kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {
	        KafkaTransactionManager<String, String> ktm = new KafkaTransactionManager<String, String>(producerFactory);
	        ktm.setNestedTransactionAllowed(true);
	        ktm.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
	        return ktm;
	}
	
	@Bean
    public DataSourceTransactionManager dstm(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
	
	@Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(config());
    }
	
	// --------------------- Producer Config
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate1(ProducerFactory<String, String> producerFactory) {
		return new KafkaTemplate<String, String>(producerFactory,true);
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
		
		configMap.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		configMap.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
		
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

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}