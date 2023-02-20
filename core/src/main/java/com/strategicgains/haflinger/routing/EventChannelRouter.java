package com.strategicgains.haflinger.routing;

import java.util.LinkedHashSet;
import java.util.Set;

import com.strategicgains.haflinger.channel.EventChannel;

/**
 * Selectively publishes to {@link EventChannel}s based on {@link RoutingRule}s. If the
 * rule associated with a channel applies, the message is published to that channel.
 * 
 * @author tfredrich
 * @since 3 Jun 2016
 */
public class EventChannelRouter
implements EventChannel
{
	private Set<RoutedEventChannel> channels = new LinkedHashSet<>();
	
	public boolean addChannel(RoutingRule rule, EventChannel channel)
	{
		return channels.add(new RoutedEventChannel(rule, channel));
	}

	@Override
	public boolean publish(Object event)
	{
		boolean isPublished = false;

		for(RoutedEventChannel route : channels)
		{
			if (route.test(event))
			{
				isPublished = route.publish(event);
			}
		}

		return isPublished;
	}

	@Override
	public void shutdown()
	{
		for(RoutedEventChannel route : channels)
		{
			route.shutdown();
		}
	}

	private class RoutedEventChannel
	implements RoutingRule
	{
		public RoutingRule rule;
		public EventChannel channel;

		public RoutedEventChannel(RoutingRule rule, EventChannel channel)
		{
			this.rule = rule;
			this.channel = channel;
		}

		public boolean publish(Object event)
		{
			return channel.publish(event);
		}

		public void shutdown()
		{
			channel.shutdown();
		}

		@Override
		public boolean test(Object t)
		{
			return rule.test(t);
		}
	}
}
