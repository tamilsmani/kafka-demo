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
import org.springframework.transaction.annotation.Transactional;

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
import com.example.event.CustomEventListener;
import com.example.event.CustomEventPublisher;

@SpringBootApplication
@ComponentScan(basePackageClasses = { KafaTransactionConfiguration.class ,CustomEventListener.class, CustomEventPublisher.class}, 
			   excludeFilters={ 
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageListenerValidationConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaSend2ListenerConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaMessageFilterConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRetryMessageConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaEventMessageConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaProducerConsumerInterceptorConfiguration.class),
					   @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=KafaRebalancingListenerConfiguration.class),
					   })
//Run your MYSQL server & publish the Event
/**
 * 
 * When the method in transaction, the publish method will wait until the completion of DB call.
 * In case of exception it will be event will not be published.
 * 
 *
 */
public class SpringEventListenerTransactionDemoApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringEventListenerTransactionDemoApplication.class);
										
	@Autowired
	private CustomEventPublisher customEventPublisher;
	
	@Autowired
	private KafaTransactionConfiguration kafaTransactionConfiguration;
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(SpringEventListenerTransactionDemoApplication.class).run(args);
		context.getBean(SpringEventListenerTransactionDemoApplication.class).run(context);
        context.close();
	}

	private void run(ConfigurableApplicationContext context) throws Exception {
		send("input-payload");
		
		TimeUnit.SECONDS.sleep(2000);
    }
	
	@Transactional(transactionManager = "dstm")
	public void send(String payload) throws Exception {
		//kafaTransactionConfiguration.getJdbcTemplate().execute("create table task (title varchar(255));");
		kafaTransactionConfiguration.getJdbcTemplate().update("insert into task(title) values ('" + payload + "')");
		customEventPublisher.publish(payload );
		
 		if(false) {
 			throw new NullPointerException();
 		}
		LOGGER.info("Event Published...");
	}
	
	
}


