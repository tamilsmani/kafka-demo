package com.example.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerAwareRebalanceListenerImpl implements ConsumerAwareRebalanceListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAwareRebalanceListenerImpl.class);
	
	private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<TopicPartition, OffsetAndMetadata>();
	
	public void perisitOffsetInfo(String topic, int partition, long offset) {
		TopicPartition part = new TopicPartition(topic, partition);
		OffsetAndMetadata metadata = new OffsetAndMetadata(offset, "Commit");
		
		currentOffsets.put(part, metadata);
		
			
	}
	/**
2020-02-05 14:25:28.313  INFO 98880 --- [emo-topic-1-C-1] c.e.c.ConsumerAwareRebalanceListenerImpl : Inside onPartitionsAssigned partitions[[my-replicated-demo-topic-2]]
2020-02-05 14:25:28.314  INFO 98880 --- [emo-topic-0-C-1] c.e.c.ConsumerAwareRebalanceListenerImpl : Inside onPartitionsAssigned currentoffset[{my-replicated-demo-topic-0=OffsetAndMetadata{offset=10, leaderEpoch=null, metadata='Commit'}, my-replicated-demo-topic-1=OffsetAndMetadata{offset=18, leaderEpoch=null, metadata='Commit'}, my-replicated-demo-topic-2=OffsetAndMetadata{offset=3, leaderEpoch=null, metadata='Commit'}}]
2020-02-05 14:25:28.314  INFO 98880 --- [emo-topic-0-C-1] c.e.c.ConsumerAwareRebalanceListenerImpl : Inside onPartitionsAssigned partitions[[my-replicated-demo-topic-0]]
2020-02-05 14:25:36.785  INFO 98880 --- [emo-topic-1-C-1] c.e.c.ConsumerAwareRebalanceListenerImpl : Assigning partition[my-replicated-demo-topic-2] and offset[OffsetAndMetadata{offset=3, leaderEpoch=null, metadata='Commit'}]
2020-02-05 14:27:22.680  INFO 98880 --- [emo-topic-0-C-1] c.e.c.ConsumerAwareRebalanceListenerImpl : Assigning partition[my-replicated-demo-topic-0] and offset[OffsetAndMetadata{offset=10, leaderEpoch=null, metadata='Commit'}]

	  
	 */
	@Override
	public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsAssigned currentoffset[{}]", currentOffsets);
		LOGGER.info("Inside onPartitionsAssigned partitions[{}]", partitions);
		// Assign respective partitions offset
		for(TopicPartition partition: partitions) {
			
			for(Map.Entry<TopicPartition, OffsetAndMetadata> entry : currentOffsets.entrySet()) {
				if(partition.partition() == entry.getKey().partition()) {
					LOGGER.info("Assigning partition[{}] and offset[{}]", partition, entry.getValue());
					consumer.seek(partition, entry.getValue());
				}
			}
			
		}
		// Above logic to assign the partions which should be stored externally. The above map is in-memory and it will be empty every time u start.
	
	
	}
	@Override
	public void onPartitionsLost(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsLost[{}]", partitions);
	}
	
	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsRevoked[{}]", partitions);
	}
	
	@Override
	public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsRevokedAfterCommit[{}]", partitions);
		consumer.commitSync(currentOffsets);
	    //currentOffsets.clear();
		
	}
	@Override
	public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsRevokedBeforeCommit[{}]", currentOffsets);
		
	}
	@Override
	public void onPartitionsLost(Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsLost[{}]", partitions);
	}
	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		LOGGER.info("Inside onPartitionsAssigned[{}]", partitions);
	}
}
