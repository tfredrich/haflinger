package com.strategicgains.haflinger.channel;

import com.strategicgains.haflinger.consumer.EventSubscriber;

public interface Subscribable
{
	/**
	 * Subscribe a handler to this event channel for the given eventTypes.
	 * 
	 * @param handler an event handler that implements the {@link EventSubscriber} interface.
	 * @return true if the subscription was successful. Otherwise, false.
	 */
	boolean subscribe(EventSubscriber handler);

	/**
	 * Remove a subscription from the underlying event channel.
	 * 
	 * @param handler an {@link EventSubscriber}
	 */
	void unsubscribe(EventSubscriber handler);
}
