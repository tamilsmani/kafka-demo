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

@SpringBootApplication
@ComponentScan(basePackageClasses = { KafaSend2ListenerConfiguration.class }, 
			   excludeFilters={ 
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerValidationConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRebalancingListenerConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageFilterConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRetryMessageConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaProducerConsumerInterceptorConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaEventMessageConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaTransactionConfiguration.class)
					   })
public class KafkaSend2ListenerDemoApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSend2ListenerDemoApplication.class);
	
	@Autowired
	KafaSend2ListenerConfiguration kafaSend2ListenerConfiguration;
	
	public static void main(String[] args) throws Exception {
		 ConfigurableApplicationContext context = new SpringApplicationBuilder(KafkaSend2ListenerDemoApplication.class).run(args);
	        context.getBean(KafkaSend2ListenerDemoApplication.class).run(context);
	        context.close();
	}

	private void run(ConfigurableApplicationContext context) throws Exception {
		 
		//kafaSend2ListenerConfiguration.send("My-sendTo-Message test");
		LOGGER.info("Sleeping for 5 seconds ........ ");
		TimeUnit.SECONDS.sleep(1000);
        
    }
}
