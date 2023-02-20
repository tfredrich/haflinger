package com.strategicgains.haflinger.simple;

import java.util.LinkedHashSet;
import java.util.Set;

import com.strategicgains.haflinger.channel.builder.ConsumableEventChannelBuilder;
import com.strategicgains.haflinger.channel.builder.SubscribableEventChannelBuilder;
import com.strategicgains.haflinger.consumer.EventConsumer;
import com.strategicgains.haflinger.consumer.EventSubscriber;
import com.strategicgains.haflinger.exception.ConsumptionException;

/**
 * Configure and build a local EventQueue that receives events only within the current JVM.
 * 
 * @author toddf
 * @since Oct 4, 2012
 */
public class SimpleEventChannelBuilder
implements SubscribableEventChannelBuilder<SimpleEventChannel, SimpleEventChannelBuilder>,
	ConsumableEventChannelBuilder<SimpleEventChannel, SimpleEventChannelBuilder>
{
	private static final long DEFAULT_POLL_DELAY = 0L;

	private Set<EventSubscriber> handlers = new LinkedHashSet<>();
	private Set<EventConsumer> consumers = new LinkedHashSet<>();
	private long pollDelay = DEFAULT_POLL_DELAY;

	public SimpleEventChannelBuilder()
	{
		super();
	}

	@Override
	public SimpleEventChannel build()
	{
		SimpleEventChannel channel = new SimpleEventChannel(pollDelay, handlers);

		for (EventConsumer consumer : consumers)
		{
			try
			{
				consumer.consume(channel);
			}
			catch (ConsumptionException e)
			{
				e.printStackTrace();
			}
		}

		return channel;
	}
    
    public SimpleEventChannelBuilder pollDelay(long millis)
    {
    	this.pollDelay = millis;
    	return this;
    }

    @Override
    public SimpleEventChannelBuilder subscribe(EventSubscriber handler)
    {
   		handlers.add(handler);
    	return this;
    }

    @Override
    public SimpleEventChannelBuilder unsubscribe(EventSubscriber handler)
    {
    	handlers.remove(handler);
    	return this;
    }

	@Override
	public SimpleEventChannelBuilder register(EventConsumer consumer)
	{
		consumers.add(consumer);
		return this;
	}

	@Override
	public SimpleEventChannelBuilder unregister(EventConsumer consumer)
	{
		consumers.remove(consumer);
		return this;
	}
}
