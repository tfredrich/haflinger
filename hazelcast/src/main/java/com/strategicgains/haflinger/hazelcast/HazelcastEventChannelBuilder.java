package com.strategicgains.haflinger.hazelcast;

import java.util.LinkedHashSet;
import java.util.Set;

import com.hazelcast.config.Config;
import com.hazelcast.config.RingbufferConfig;
import com.strategicgains.haflinger.channel.builder.SubscribableEventChannelBuilder;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * @author toddf
 * @since Oct 5, 2012
 */
public class HazelcastEventChannelBuilder
implements SubscribableEventChannelBuilder<HazelcastEventChannel, HazelcastEventChannelBuilder>
{
	private static final String DEFAULT_TOPIC_NAME = "haflinger";

	private Config config = null;
	private RingbufferConfig bufferConfig = null;
	private String topicName = DEFAULT_TOPIC_NAME;
	private Set<EventSubscriber> handlers = new LinkedHashSet<>();

	public HazelcastEventChannelBuilder()
	{
		super();
	}

	/**
	 * Very 'thin' (as in not at all) veneer to set underlying Hazelcast
	 * configuration. Yes, this exposes the underlying implementation. Bummer.
	 * 
	 * @param configuration Hazelcast Config instance.
	 * @return this builder to facilitate method chainging.
	 */
	public HazelcastEventChannelBuilder withConfiguration(Config configuration)
	{
		this.config = configuration;
		return this;
	}

	public HazelcastEventChannelBuilder withBufferConfiguration(RingbufferConfig configuration)
	{
		this.bufferConfig = configuration;
		return this;
	}

	public HazelcastEventChannelBuilder withTopic(String topicName)
	{
		this.topicName = topicName;
		return this;
	}

	@Override
	public HazelcastEventChannelBuilder subscribe(EventSubscriber handler)
	{
		handlers.add(handler);
		return this;
	}

	@Override
	public HazelcastEventChannelBuilder unsubscribe(EventSubscriber handler)
	{
		handlers.remove(handler);
		return this;
	}

	@Override
	public HazelcastEventChannel build()
	{
		return new HazelcastEventChannel(config, bufferConfig, topicName, handlers.toArray(new EventSubscriber[0]));
	}
}
