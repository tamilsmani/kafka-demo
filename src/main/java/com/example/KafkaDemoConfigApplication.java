package com.example;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.example.config.KafaConfiguration;
import com.example.config.KafaEventMessageConfiguration;
import com.example.config.KafaMessageFilterConfiguration;
import com.example.config.KafaMessageListenerConfiguration;
import com.example.config.KafaMessageListenerValidationConfiguration;
import com.example.config.KafaProducerConsumerInterceptorConfiguration;
import com.example.config.KafaRebalancingListenerConfiguration;
import com.example.config.KafaRetryMessageConfiguration;
import com.example.config.KafaSend2ListenerConfiguration;
import com.example.config.KafaTransactionConfiguration;
import com.example.producer.xml.ProducerTransformer;

// Refer config folder for Spring-kafa code


//@SpringBootApplication
//@ComponentScan({ "com.example.config", "com.example.producer.xml" })
@SpringBootApplication
@ComponentScan(basePackageClasses = { KafaTransactionConfiguration.class }, 
			   excludeFilters={ 
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
public class KafkaDemoConfigApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaDemoConfigApplication.class);
	
	@Autowired
	ProducerTransformer producerTransformer;
	
	public static void main(String[] args) throws Exception {
		 ConfigurableApplicationContext context = new SpringApplicationBuilder(KafkaDemoConfigApplication.class).run(args);
	        context.getBean(KafkaDemoConfigApplication.class).run(context);
	        context.close();
	}

	private void run(ConfigurableApplicationContext context) throws Exception {
		 
		producerTransformer.sendValue("My-Test-message with key null");
		LOGGER.info("Sleeping for 5 seconds ........ ");
		TimeUnit.SECONDS.sleep(5);
		
		producerTransformer.sendWithKey("1", "My-Test-message with key");
		TimeUnit.SECONDS.sleep(5);
        
    }
}
