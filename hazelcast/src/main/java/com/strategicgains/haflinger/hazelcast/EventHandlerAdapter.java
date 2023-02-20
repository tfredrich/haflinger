package com.strategicgains.haflinger.hazelcast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.strategicgains.haflinger.consumer.EventSubscriber;
import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * @author toddf
 * @since Oct 5, 2012
 */
public class EventHandlerAdapter
implements MessageListener<Object>
{
	private static final ExecutorService EVENT_EXECUTOR = Executors.newCachedThreadPool();

	private EventSubscriber handler;
	private boolean isSelectiveHandler;

	public EventHandlerAdapter(EventSubscriber handler)
	{
		super();
		this.handler = handler;
		isSelectiveHandler = (SelectiveEventSubscriber.class.isAssignableFrom(handler.getClass()));
	}

	@Override
	public void onMessage(Message<Object> message)
	{
		// TODO: implement logging
//		System.out.println("Message received: " + message.toString());

		if (shouldHandle(message.getMessageObject()))
		{
			processEvent(message.getMessageObject());
		}
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
