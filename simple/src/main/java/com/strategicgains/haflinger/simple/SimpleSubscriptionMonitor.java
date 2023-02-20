package com.strategicgains.haflinger.simple;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.strategicgains.haflinger.consumer.EventSubscriber;
import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * A thread that receives published events and sends them to subscribers.
 * Registered event {@link EventSubscriber}s will be called for whatever event types each can process.
 * 
 * {@link EventSubscriber}s are called using an Executor pool that grows dynamically as needed, so
 * they are run asynchronously.
 * 
 * Events that have no consumers are simply removed from the queue and ignored.
 * 
 * @author toddf
 * @since May 17, 2011
 */
public class SimpleSubscriptionMonitor
extends Thread
{
	// SECTION: CONSTANTS

	private static final Executor EVENT_EXECUTOR = Executors.newCachedThreadPool();

	
	// SECTION: INSTANCE METHODS

	private Map<Class<?>, List<EventSubscriber>> handlersByEvent = new ConcurrentHashMap<Class<?>, List<EventSubscriber>>();
	private Set<EventSubscriber> handlers = new LinkedHashSet<>();
	private boolean shouldShutDown = false;
	private SimpleEventChannel events;
	private long delay;


	// SECTION: CONSTRUCTORS

	public SimpleSubscriptionMonitor(SimpleEventChannel queue, long pollDelayMillis)
	{
		super();
		setDaemon(true);
		this.delay = pollDelayMillis;
		this.events = queue;
	}

	
	// SECTION: INSTANCE METHODS

	public synchronized boolean register(EventSubscriber handler)
	{
		return handlers.add(handler);
	}

	public synchronized boolean unregister(EventSubscriber handler)
	{
		return handlers.remove(handler);
	}

	public void shutdown()
	{
		shouldShutDown = true;
		System.out.println("Event monitor notified for shutdown.");

		synchronized(events)
		{
			events.notifyAll();
		}
	}
	
	// SECTION: RUNNABLE/THREAD

	@Override
	public void run()
	{
		System.out.println("Event monitor starting...");

		while(!shouldShutDown)
		{
			try
			{
				synchronized (events)
				{
					if (events.isEmpty())
					{
						events.wait(delay);		// Support wake-up via events.notify()
					}
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				System.err.println("Interrupted (use shutdown() to terminate).  Continuing...");
				continue;
			}

			Object event = null;

			while ((event = events.poll()) != null)
			{
				processEvent(event);
			}
		}
		
		System.out.println("Event monitor exiting...");
		handlers.clear();
		handlersByEvent.clear();
	}

	/**
	 * Runs each appropriate EventHandler in an Executor.
	 * 
	 * @param event
	 */
	private void processEvent(final Object event)
    {
	    System.out.println("Processing event: " + event.toString());
	    for (final EventSubscriber handler : handlers)
	    {
    		EVENT_EXECUTOR.execute(new Runnable(){
				@Override
                public void run()
                {
			    	try
			    	{
		    			if (shouldHandle(handler, event))
		    			{
		    				handler.handle(event);
		    			}
			    	}
			    	catch(Exception e)
			    	{
			    		e.printStackTrace();
			    		
//			    		if (shouldReRaiseOnError)
//			    		{
//			    			System.out.println("Event handler failed. Re-publishing event: " + event.toString());
//			    			try
//			    			{
//								events.publish(event);
//							}
//			    			catch (Exception e1)
//			    			{
//								e1.printStackTrace();
//							}
//			    		}
			    	}
                }

				private boolean shouldHandle(EventSubscriber handler, Object event)
				{
					if (isSelectiveHandler(handler))
					{
						return ((SelectiveEventSubscriber) handler).test(event);
					}

					return true;
				}

				private boolean isSelectiveHandler(EventSubscriber handler)
				{
					return (SelectiveEventSubscriber.class.isAssignableFrom(handler.getClass()));
				}
    		});
	    }
    }
}
