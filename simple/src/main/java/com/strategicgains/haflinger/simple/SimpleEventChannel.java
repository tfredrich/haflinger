package com.strategicgains.haflinger.simple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.strategicgains.haflinger.channel.ConsumableEventChannel;
import com.strategicgains.haflinger.channel.SubscribableEventChannel;
import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * Note: Do not use for production.
 * 
 * An Event Channel within the current JVM. Messages will never be published outside the existing JVM.
 * Uses an in-memory concurrent queue to store messages. There are no durability guarantees--if the JVM
 * is terminated before messages are consumed, unconsumed messages are lost.
 * 
 * While this event channel is both subscribable and consumable, choose only one method for receiving
 * messages from this channel as both are destructive, in that both methods remove items from the
 * queue. If both get() and subscribe() are used simultaneous, results will be inconsistent.
 * 
 * 
 * @author toddf
 * @since Oct 18, 2012
 */
public class SimpleEventChannel
implements SubscribableEventChannel, ConsumableEventChannel
{
	private Queue<Object> queue = new ConcurrentLinkedQueue<>();
	private SimpleSubscriptionMonitor monitor;

	public SimpleEventChannel(EventSubscriber... handlers)
	{
		this(0L, Arrays.asList(handlers));
	}

	public SimpleEventChannel(long pollDelayMillis, EventSubscriber... handlers)
	{
		this(pollDelayMillis, Arrays.asList(handlers));
	}

	public SimpleEventChannel(long pollDelayMillis, Collection<EventSubscriber> handlers)
	{
		super();
		initializeMonitor(pollDelayMillis, handlers);
	}

	/**
	 * @param handlers
	 */
	private void initializeMonitor(long pollDelayMillis, Collection<EventSubscriber> handlers)
	{
		monitor = new SimpleSubscriptionMonitor(this, pollDelayMillis);

		for (EventSubscriber handler : handlers)
		{
			monitor.register(handler);
		}

		monitor.start();
	}

	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	public Object poll()
	{
		return queue.poll();
	}

	@Override
	public boolean publish(Object event)
	{
		boolean isAdded = queue.add(event);

		synchronized (this)
		{
			notifyAll();
		}

		return isAdded;
	}

	@Override
	public void shutdown()
	{
		queue.clear();
		queue = null;
	}

    @Override
    public boolean subscribe(EventSubscriber handler)
    {
		return monitor.register(handler);
    }

    @Override
    public void unsubscribe(EventSubscriber handler)
    {
    	monitor.unregister(handler);
    }

	@Override
	public Object get()
	{
		Object result;
		while((result = poll()) != null);
		return result;
	}

	@Override
	public Object get(long timeoutMillis)
	{
		Object result = null;
		long startMillis = System.currentTimeMillis();
		long maxTimeout = startMillis + timeoutMillis;

		while(startMillis < maxTimeout)
		{
			result = poll();

			if (result != null) return result;
		}

		return result;
	}

	@Override
	public void commit()
	{
	}
}
