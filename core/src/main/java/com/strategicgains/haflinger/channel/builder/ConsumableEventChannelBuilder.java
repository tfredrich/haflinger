package com.strategicgains.haflinger.channel.builder;

import com.strategicgains.haflinger.channel.Consumable;
import com.strategicgains.haflinger.channel.EventChannel;
import com.strategicgains.haflinger.consumer.EventConsumer;

/**
 * Allow (un)registration of {@link EventConsumer}s with the {@link EventChannel}.
 * 
 * @author tfredrich
 *
 * @param <T> The type of the underlying EventChannel
 * @param <B> The type of this builder.
 */
public interface ConsumableEventChannelBuilder<T  extends Consumable & EventChannel, B extends ConsumableEventChannelBuilder<?, ?>>
{
	public B register(EventConsumer consumer);
	public B unregister(EventConsumer consumer);
}
