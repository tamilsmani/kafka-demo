package com.example.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.MessageHandler;
/**
 * 
 *
 * <int-kafka:outbound-channel-adapter id="kafkaOutboundChannelAdapter"
                                    kafka-template="template"
                                    auto-startup="false"
                                    channel="inputToKafka"
                                    topic="foo"
                                    sync="false"
                                    message-key-expression="'bar'"
                                    send-failure-channel="failures"
                                    send-success-channel="successes"
                                    error-message-strategy="ems"
                                    partition-id-expression="2">
</int-kafka:outbound-channel-adapter>

<bean id="template" class="org.springframework.kafka.core.KafkaTemplate">
    <constructor-arg>
        <bean class="org.springframework.kafka.core.DefaultKafkaProducerFactory">
            <constructor-arg>
                <map>
                    <entry key="bootstrap.servers" value="localhost:9092" />
                    ... <!-- more producer properties -->
                </map>
            </constructor-arg>
        </bean>
    </constructor-arg>
</bean>
 * 
 *
 */
//@Configuration
public class ProducerChannelConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProducerChannelConfig.class);

	
	@Value("${kafka.server}")
	private String kafaServer;

	@Value("${kafka.topic}")
	private String topic;
	
	@Bean
	public DirectChannel producerChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "producerChannel")
	public MessageHandler handler() {
		LOGGER.info("Inside Producer handler....");
		
		KafkaProducerMessageHandler<String, String> handler = new KafkaProducerMessageHandler<>(kafkaTemplate());
		handler.setMessageKeyExpression(new LiteralExpression("demo-key"));
		handler.setTopicExpression(new LiteralExpression(topic));
		handler.setOutputChannel(producerChannel());

		return handler;
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(constructProducerFactory());
	}

	@Bean
	public ProducerFactory<String, String> constructProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfig());
	}

	@Bean(name = "kafaConfigMap")
	public Map<String, Object> producerConfig() {
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put(ProducerConfig.RETRIES_CONFIG, 0);
		configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		
		configMap.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		    
//		configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafaServer);
//		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		
		// introduce a delay on the send to allow more messages to accumulate
		configMap.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		// records
		configMap.put(ConsumerConfig.GROUP_ID_CONFIG, topic);
		// automatically reset the offset to the earliest offset
		configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		return configMap;

	}
	
//	@Bean
//	  public Producer producer() {
//	    return new Producer();
//	  }
	
}