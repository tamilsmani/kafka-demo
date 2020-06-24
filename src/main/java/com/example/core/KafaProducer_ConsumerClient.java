package com.example.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.LogManager;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafaProducer_ConsumerClient {
	private static final String TOPIC_NAME ="my-replicated-demo-topic";
	
	public static void main(String ar[]) throws Exception {
		LogManager.getLogManager().reset();
		
		//log4j.logger.org.apache.kafka.clients.consumer.internals.Fetcher=WARN
		Properties props = new Properties();
	      props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
	      props.put("acks", "all"); 
	      props.put("retries", 0);
	   //   props.put("batch.size", 16384);
	     // props.put("linger.ms", 1);
	      props.put("fetch.max.bytes",1024*1024);
	     // props.put("buffer.memory", 33554432);
	      
	      props.put("key.serializer",    "org.apache.kafka.common.serialization.StringSerializer");
	      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	      
	      // Consumer config
	      props.put("group.id", "my-replicated-demo-topic-group");
	      props.put("enable.auto.commit", "false"); // It helps to read from message from beginning each time.
	      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	      
	    //  props.put("auto.commit.interval.ms", "1000");
	   //   props.put("session.timeout.ms", "30000");
	      props.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
	      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	      
	      
//	      Producer<String, String> producer = new KafkaProducer<String, String>(props);
//          producer.send(new ProducerRecord<String, String>(TOPIC_NAME, "Message-key-null" + System.currentTimeMillis()));
//          producer.send(new ProducerRecord<String, String>(TOPIC_NAME, "Message-key-null" + System.currentTimeMillis()));
//          producer.close();
          
          System.out.println("Connected & sent successfully...........");
          KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
          consumer.subscribe(Arrays.asList(TOPIC_NAME));
          for(int i=0;i<2;i++) {
        	  System.out.println("Subscribed to topic " + TOPIC_NAME);
	          ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(300000));
	          System.out.println("Started consuming........ " + records.count());
	          
	          for (ConsumerRecord<String, String> record : records) {
	        	  // print the offset,key and value for the consumer records.
	        	 System.out.printf("Consumed =>>>>> offset = %d, key = %s, value = %s, parition = %s, headers = %s \n", record.offset(), record.key(), record.value(), record.partition(), record.headers());
			}
          }

	}
}