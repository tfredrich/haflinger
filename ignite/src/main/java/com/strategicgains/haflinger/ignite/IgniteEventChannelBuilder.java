package com.strategicgains.haflinger.ignite;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import com.strategicgains.haflinger.channel.builder.SubscribableEventChannelBuilder;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * @author toddf
 * @since Oct 5, 2012
 */
public class IgniteEventChannelBuilder
implements SubscribableEventChannelBuilder<IgniteEventChannel, IgniteEventChannelBuilder>
{
	private static final String DEFAULT_TOPIC_NAME = "domain-events";

	private Ignite config = null;
	private String topicName = DEFAULT_TOPIC_NAME;
	private boolean isOrdered = true;
	private Set<EventSubscriber> handlers = new LinkedHashSet<>();

	public IgniteEventChannelBuilder()
	{
		super();
	}

	/**
	 * Very 'thin' (as in not at all) veneer to set underlying Ignite
	 * configuration. Yes, this exposes the underlying implementation. Bummer.
	 * 
	 * @param configuration Ignite instance.
	 * @return this builder to facilitate method chaining.
	 */
	public IgniteEventChannelBuilder withConfiguration(Ignite configuration)
	{
		this.config = configuration;
		return this;
	}

	/**
	 * Sets the topic name on the channel.
	 * 
	 * @param topicName the name of the topic to use.
	 * @return this builder to facilitate method chaining.
	 */
	public IgniteEventChannelBuilder withTopic(String topicName)
	{
		this.topicName = topicName;
		return this;
	}

	/**
	 * Allows messages to be sent unordered. Default is ordered.
	 * 
	 * @return this builder to facilitate method chaining.
	 */
	public IgniteEventChannelBuilder unordered()
	{
		this.isOrdered = false;
		return this;
	}

	/**
	 * Ensures ordering of messages on the channel.
	 * This is the default.
	 * 
	 * @return this builder to facilitate method chaining.
	 */
	public IgniteEventChannelBuilder ordered()
	{
		this.isOrdered = true;
		return this;
	}

	@Override
	public IgniteEventChannelBuilder subscribe(EventSubscriber handler)
	{
		handlers.add(handler);
		return this;
	}

	@Override
	public IgniteEventChannelBuilder unsubscribe(EventSubscriber handler)
	{
		handlers.remove(handler);
		return this;
	}

	@Override
	public IgniteEventChannel build()
	{
		if (config == null) config = Ignition.ignite();

		return new IgniteEventChannel(topicName, config, handlers.toArray(new EventSubscriber[0]))
			.ordered(isOrdered);
	}
}
