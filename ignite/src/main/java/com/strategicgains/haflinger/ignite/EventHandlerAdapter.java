package com.strategicgains.haflinger.ignite;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ignite.lang.IgniteBiPredicate;

import com.strategicgains.haflinger.consumer.EventSubscriber;
import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * Adapts the general-purpose EventHandler of Haflinger into something that can handle Apache Ignite messages.
 * 
 * @author toddf
 * @since Feb 5, 2019
 */
public class EventHandlerAdapter
implements IgniteBiPredicate<UUID, Object>
{
	private static final ExecutorService EVENT_EXECUTOR = Executors.newCachedThreadPool();
	private static final long serialVersionUID = 3657330202723832700L;

	private EventSubscriber handler;
	private boolean isSelectiveHandler;

	public EventHandlerAdapter(EventSubscriber handler)
	{
		super();
		this.handler = handler;
		this.isSelectiveHandler = (SelectiveEventSubscriber.class.isAssignableFrom(handler.getClass()));
	}

	@Override
	public boolean apply(UUID nodeId, Object message)
	{
		if (shouldHandle(message))
		{
			processEvent(message);
		}

		return true;
	}

	private boolean shouldHandle(Object messageObject)
	{
		if (isSelectiveHandler)
		{
			return ((SelectiveEventSubscriber) handler).test(messageObject);
		}

		return true;
	}

	private void processEvent(final Object event)
	{
//		System.out.println("Processing event: " + event.toString());

		EVENT_EXECUTOR.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					handler.handle(event);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
