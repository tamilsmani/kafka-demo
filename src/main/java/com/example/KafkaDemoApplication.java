package com.example;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;

import com.example.config.KafaConfiguration;
import com.example.config.KafaEventMessageConfiguration;
import com.example.config.KafaMessageFilterConfiguration;
import com.example.config.KafaMessageListenerConfiguration;
import com.example.config.KafaMessageListenerValidationConfiguration;
import com.example.config.KafaProducerConsumerInterceptorConfiguration;
import com.example.config.KafaRebalancingListenerConfiguration;
import com.example.config.KafaRetryMessageConfiguration;
import com.example.config.KafaSend2ListenerConfiguration;
import com.example.producer.xml.ProducerTransformer;

// Refer config folder for Spring-kafa code


@SpringBootApplication
@ImportResource({ "classpath*:META-INF/kafka*.xml" })
@ComponentScan( basePackages = { "com.example.producer.xml", "com.example.consumer.xml"} , 
	excludeFilters = {
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerValidationConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaSend2ListenerConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageFilterConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRetryMessageConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaEventMessageConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaProducerConsumerInterceptorConfiguration.class),
			   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRebalancingListenerConfiguration.class)
})
public class KafkaDemoApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaDemoApplication.class);
	
	@Autowired
	ProducerTransformer producerTransformer;
	
	@Value("${kafka.topic}")
	private String topic;

	@Autowired
	@Qualifier("processChannel")
	private DirectChannel processChannel;
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(KafkaDemoApplication.class).run(args);
        context.getBean(KafkaDemoApplication.class).run(context);
        context.close();
	        
		//SpringApplication.run(KafkaDemoApplication.class, args);
	}

	private void run(ConfigurableApplicationContext context) throws Exception {
		 
        MessageChannel inputToKafka = context.getBean("inputToKafka", MessageChannel.class);
        inputToKafka.send(
        		MessageBuilder.withPayload("demo-data").
        		setHeader(KafkaHeaders.TOPIC, topic)
        		.build());
		//producerTransformer.send("My-Test-message");
		TimeUnit.SECONDS.sleep(5000);
        LOGGER.info("Waiting for consumer to consume the message ........ ");
        TimeUnit.SECONDS.sleep(5000);
        
    }
}
