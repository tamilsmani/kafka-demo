package com.example.consumer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.ListenerMode;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import com.example.consumer.xml.Receiver;

/**
 *
 * <int-kafka:message-driven-channel-adapter
        id="kafkaListener"
        listener-container="container"
        auto-startup="false"
        phase="100"
        send-timeout="5000"
        mode="record"
        retry-template="template"
        recovery-callback="callback"
        error-message-strategy="ems"
        channel="someChannel"
        error-channel="errorChannel" />

<bean id="container" class="org.springframework.kafka.listener.KafkaMessageListenerContainer">
    <constructor-arg>
        <bean class="org.springframework.kafka.core.DefaultKafkaConsumerFactory">
            <constructor-arg>
                <map>
                <entry key="bootstrap.servers" value="localhost:9092" />
                ...
                </map>
            </constructor-arg>
        </bean>
    </constructor-arg>
    
    <constructor-arg>
        <bean class="org.springframework.kafka.listener.config.ContainerProperties">
            <constructor-arg name="topics" value="foo" />
        </bean>
    </constructor-arg>

</bean>
 */
//@Configuration
public class ReceiverConfig {

	@Value("${kafka.server}")
	private String kafkaServer;

	@Value("${kafka.topic}")
	private String topicName;
	
	@Autowired
	private Receiver receiver;

	@Autowired
	@Qualifier("kafaConfigMap")
	private Map<String,Object> kafaConfigMap;
	
	@Bean
	public DirectChannel receiverChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "receiverChannel")
	public Receiver receiveHandler() {
		return receiver;
	}
	
	@Bean
	public KafkaMessageDrivenChannelAdapter<String, String> kakfaMessageDrivenChannelAdapter() {
		KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter = new KafkaMessageDrivenChannelAdapter<>(container(), ListenerMode.record);
		kafkaMessageDrivenChannelAdapter.setOutputChannel(receiverChannel());

		return kafkaMessageDrivenChannelAdapter;
	}

	@Bean
	public ConcurrentMessageListenerContainer<String, String> container() {
	    ContainerProperties containerProperties = new ContainerProperties(topicName);
	    // Additional properties can be set here.....
	    return new ConcurrentMessageListenerContainer<>(consumerFactory(), containerProperties);
	}


	//@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>((HashMap<String,Object>)kafaConfigMap.get("kafaConfigMap"));
	}

	//@Bean
//	public Map<String, Object> consumerConfigs() {
//		Map<String, Object> props = new HashMap<>();
//		// list of host:port pairs used for establishing the initial connections to the
//		// Kafka cluster
//		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
//		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		// allows a pool of processes to divide the work of consuming and processing
//		// records
//		props.put(ConsumerConfig.GROUP_ID_CONFIG, "helloworld");
//		// automatically reset the offset to the earliest offset
//		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//		return props;
//	}

}