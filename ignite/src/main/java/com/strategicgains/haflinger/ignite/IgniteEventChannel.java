package com.strategicgains.haflinger.ignite;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;

import com.strategicgains.haflinger.channel.SubscribableEventChannel;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * @author toddf
 * @since Feb 5, 2019
 */
public class IgniteEventChannel
implements SubscribableEventChannel
{
	private static final String IGNITE_NAME = "Haflinger";

	private String topic;
	private IgniteMessaging ignite;
	private Map<EventSubscriber, EventHandlerAdapter> subscriptions = new ConcurrentHashMap<>();
	private boolean isOrdered = true;

	public IgniteEventChannel(String topic, EventSubscriber... eventHandlers)
	{
		this(topic, Ignition.ignite(IGNITE_NAME).message(), eventHandlers);
	}

	public IgniteEventChannel(String topic, Ignite ignite, EventSubscriber... eventHandlers)
	{
		this(topic, ignite.message(), eventHandlers);
	}

	public IgniteEventChannel(String topic, Ignite ignite, ClusterGroup clusterGroup, EventSubscriber... eventHandlers)
	{
		this(topic, ignite.message(clusterGroup), eventHandlers);
	}

	public IgniteEventChannel(String topic, IgniteMessaging messaging, EventSubscriber... eventHandlers)
	{
		super();
		this.topic = topic;
		this.ignite = messaging;
		subscribeAll(eventHandlers);
	}

	private void subscribeAll(EventSubscriber... eventHandlers)
	{
		if (eventHandlers == null) return;

		for (EventSubscriber handler : eventHandlers)
		{
			subscribe(handler);
		}
	}

	public String getTopic()
	{
		return topic;
	}

	public IgniteEventChannel setTopic(String name)
	{
		this.topic = name;
		return this;
	}

	public boolean isOrdered()
	{
		return isOrdered;
	}

	public IgniteEventChannel ordered(boolean value)
	{
		this.isOrdered = value;
		return this;
	}

	@Override
	public boolean subscribe(EventSubscriber handler)
	{
		EventHandlerAdapter listener = new EventHandlerAdapter(handler);
		ignite.localListen(topic, listener);
		subscriptions.put(handler, listener);
		return true;
	}

	@Override
	public void unsubscribe(EventSubscriber handler)
	{
		EventHandlerAdapter listener = subscriptions.get(handler);

		if (listener != null)
		{
			ignite.stopLocalListen(topic, listener);
		}
	}

	public void unsubscribeAll()
	{
		subscriptions.values().stream().forEach(listener -> ignite.stopLocalListen(topic, listener));
	}

	@Override
	public boolean publish(Object event)
	{
		try
		{
			if (isOrdered)
			{
				ignite.sendOrdered(topic, event, 0);
			}
			else
			{
				ignite.send(topic, event);
			}

			return true;
		}
		catch (IgniteException e)
		{
			return false;
		}
	}

	@Override
	public void shutdown()
	{
		unsubscribeAll();
	}
}
