package com.strategicgains.haflinger.consumer;

import com.strategicgains.haflinger.channel.EventChannel;
import com.strategicgains.haflinger.exception.EventingException;

/**
 * Defines the callback interface to handle messages from channel subscriptions.
 * 
 * Implementations of this interface are registered with an {@link EventChannel} via
 * a call to subscribe(EventHandler). There is no processing of the event object
 * before it is sent to the handler from the channel.
 * 
 * Depending on the underlying implementation of the transport system, the act of
 * subscribing may materialize a subscription in an external messaging system.
 * 
 * @author toddf
 * @since May 12, 2011
 */
public interface EventSubscriber
{
	/**
	 * Process the given event.
	 * 
	 * @param event an event or message.
	 * @throws EventingException if handler fails to process the event.
	 */
	public void handle(Object event)
	throws EventingException;
}
