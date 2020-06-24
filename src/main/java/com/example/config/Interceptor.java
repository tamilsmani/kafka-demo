package com.example.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Interceptor implements ProducerInterceptor<String,String>, ConsumerInterceptor<String,String>{

	private static final Logger LOGGER = LoggerFactory.getLogger(Interceptor.class);
	private InterceptorBean producer;
	private InterceptorBean consumer;
	
	@Override
	public void configure(Map<String, ?> configs) {
		LOGGER.info("Config method=");
		
		if(null !=configs.get("producer.bean") )
			producer = (InterceptorBean)configs.get("producer.bean");
		if(null != configs.get("consumer.bean"))
			consumer = (InterceptorBean)configs.get("consumer.bean");
		
	}

	@Override
	public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {
		LOGGER.info("onConsume method=");
		consumer.display("consumer==> ");
		return records;
	}

	@Override
	public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
		LOGGER.info("onCommit method=");
		
	}

	@Override
	public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
		LOGGER.info("onSend method=");
		producer.display("producer==> ");
		return record;
	}

	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
		LOGGER.info("onAcknowledgement method=");
		
	}

	@Override
	public void close() {
		LOGGER.info("On close method=");
	}

	
}
