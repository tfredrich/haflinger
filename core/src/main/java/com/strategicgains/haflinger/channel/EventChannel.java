package com.strategicgains.haflinger.channel;

/**
 * An event channel is an underlying implementation of a messaging
 * infrastructure. Note that there is no concept of 'topic' or 'queue'
 * here. A channel might equate to an topic though, depending on the
 * underlying implementation.
 * 
 * All publishing is using a publish-subscribe metaphor more in the
 * JMS 'topic' sense. There can always be multiple subscribers to a
 * channel.
 * 
 * Depending on the underlying implementation, there may be
 * multiple event types on a single channel.
 * 
 * @author toddf
 * @since Oct 18, 2012
 */
public interface EventChannel
{
	/**
	 * Publish an event to this event channel.
	 * 
	 * @param event the event instance.
	 * @return true if the event is published to the channel. Otherwise, false for non-fatal failure to publish reasons (for example, this channel cannot publish events of that type).
	 * @throws RuntimeException for non-recoverable errors.
	 */
	boolean publish(Object event);

	/**
	 * Terminate event handling on the transport and free all consumed resources.
	 */
	void shutdown();
}
