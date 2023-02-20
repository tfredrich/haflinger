package com.strategicgains.haflinger.hazelcast;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.config.Config;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.strategicgains.haflinger.channel.SubscribableEventChannel;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * @author toddf
 * @since Oct 18, 2012
 */
public class HazelcastEventChannel
implements SubscribableEventChannel
{
	private static final String HAZELCAST_NAME = "Haflinger";
	private static final int DEFAULT_CAPACITY = 1000;

	private ITopic<Object> topic;
	private Map<EventSubscriber, UUID> subscriptions = new ConcurrentHashMap<>();

	public HazelcastEventChannel(String topicName, EventSubscriber... eventHandlers)
	{
		this(null, null, topicName, eventHandlers);
	}

	public HazelcastEventChannel(Config config, RingbufferConfig bufferConfig, String topicName, EventSubscriber... eventHandlers)
	{
		super();

		if (config == null) config = new Config(HAZELCAST_NAME);
		if (bufferConfig == null)
		{
			RingbufferConfig c = new RingbufferConfig(topicName);
			c.setCapacity(DEFAULT_CAPACITY);
		}

		HazelcastInstance hazelcast = Hazelcast.getOrCreateHazelcastInstance(config);
		setTopic(hazelcast.getReliableTopic(topicName));
		subscribeAll(eventHandlers);
	}

	private void subscribeAll(EventSubscriber... eventHandlers)
	{
		for (EventSubscriber handler : eventHandlers)
		{
			subscribe(handler);
		}
	}

	protected void setTopic(ITopic<Object> aTopic)
    {
		this.topic = aTopic;
    }

	@Override
	public boolean publish(Object event)
	{
		topic.publish(event);
		return true;
	}

	@Override
	public void shutdown()
	{
		topic.destroy();
	}

	@Override
	public boolean subscribe(EventSubscriber consumer)
	{
		UUID listenerId = topic.addMessageListener(new EventHandlerAdapter(consumer));
		subscriptions.put(consumer, listenerId);
		return true;
	}

	@Override
	public void unsubscribe(EventSubscriber handler)
	{
		UUID listenerId = subscriptions.get(handler);

		if (listenerId != null)
		{
			topic.removeMessageListener(listenerId);
		}
	}
}
