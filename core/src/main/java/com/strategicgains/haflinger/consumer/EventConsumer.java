package com.strategicgains.haflinger.consumer;

import com.strategicgains.haflinger.channel.Consumable;
import com.strategicgains.haflinger.exception.ConsumptionException;

/**
 * Defines the interface for creation of custom event consumers that use a 'pull' model.
 * 
 * A {@link Consumable} is passed to the consumer, where the consumer then
 * calls get() or get(long) to retrieve messages from the EventChannel.
 * 
 *  All threading or executor management is up to the EventConsumer implementation.
 * 
 * @author tfredrich
 * @since 2 Jun 2016
 */
public interface EventConsumer
{
	/**
	 * Start consuming events from a {@link Consumable}
	 */
	void consume(Consumable consumable)
	throws ConsumptionException;

	void shutdown();
}
