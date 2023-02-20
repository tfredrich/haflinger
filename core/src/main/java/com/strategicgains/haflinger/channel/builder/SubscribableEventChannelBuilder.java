package com.strategicgains.haflinger.channel.builder;

import com.strategicgains.haflinger.channel.EventChannel;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * @author toddf
 * @since Oct 4, 2012
 */
public interface SubscribableEventChannelBuilder<T extends EventChannel, B extends SubscribableEventChannelBuilder<?, ?>>
{
	public B subscribe(EventSubscriber subscriber);
	public B unsubscribe(EventSubscriber subscriber);
	public T build();
}
