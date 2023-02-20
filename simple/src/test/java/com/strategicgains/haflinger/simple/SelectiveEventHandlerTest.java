package com.strategicgains.haflinger.simple;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.strategicgains.haflinger.routing.EventChannelRouter;
import com.strategicgains.haflinger.routing.RoutingRule;
import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;
import com.strategicgains.haflinger.simple.SimpleEventChannel;

/**
 * @author toddf
 * @since Oct 5, 2012
 */
public class SelectiveEventHandlerTest
{
	private static final int PAUSE_MILLIS = 300;
	private DomainEventsTestHandler handler = new DomainEventsTestHandler();
	private DomainEventsTestIgnoredEventsHandler ignoredHandler = new DomainEventsTestIgnoredEventsHandler();
	private DomainEventsTestLongEventHandler longHandler = new DomainEventsTestLongEventHandler();
	private SimpleEventChannel queue;

	@Before
	public void setup()
	{
		queue = new SimpleEventChannel(0L, Arrays.asList(handler, ignoredHandler, longHandler));
	}
	
	@After
	public void teardown()
	{
		queue.shutdown();
	}

	@Test
	public void shouldNotifyEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
		queue.publish(new HandledEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(1, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldNotifyEventHandlerMultipleTimes()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		queue.publish(new HandledEvent());
		queue.publish(new IgnoredEvent());
		queue.publish(new HandledEvent());
		queue.publish(new IgnoredEvent());
		queue.publish(new HandledEvent());
		queue.publish(new IgnoredEvent());
		queue.publish(new HandledEvent());
		queue.publish(new IgnoredEvent());
		queue.publish(new HandledEvent());
		queue.publish(new IgnoredEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(5, handler.getCallCount());
		assertEquals(5, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldNotNotifyEventHandler()
	throws Exception
	{
		assertEquals(0, ignoredHandler.getCallCount());
		queue.publish(new IgnoredEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(0, handler.getCallCount());
		assertEquals(1, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

//	@Test
//	public void shouldRetryEventHandler()
//	throws Exception
//	{
////		queue.retryOnError(true);
//
//		assertEquals(0, handler.getCallCount());
//		queue.publish(new ErroredEvent());
//		Thread.sleep(PAUSE_MILLIS);
//		assertEquals(6, handler.getCallCount());
//		assertEquals(0, ignoredHandler.getCallCount());
//		assertEquals(0, longHandler.getCallCount());
//	}

	@Test
	public void shouldNotRetryEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		queue.publish(new ErroredEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(1, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldProcessInParallel()
	throws Exception
	{
		assertEquals(0, longHandler.getCallCount());
		queue.publish(new LongEvent());
		queue.publish(new LongEvent());
		queue.publish(new LongEvent());
		queue.publish(new LongEvent());
		queue.publish(new LongEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(5, longHandler.getCallCount());
	}

	@Test
	public void shouldOnlyPublishSelected()
	throws Exception
	{
		EventChannelRouter c = new EventChannelRouter();
		c.addChannel(new RoutingRule()
		{	
			@Override
			public boolean test(Object event)
			{
				return (HandledEvent.class.equals(event.getClass()));
			}
		}, queue);

		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		c.publish(new HandledEvent());
		c.publish(new IgnoredEvent());
		c.publish(new HandledEvent());
		c.publish(new IgnoredEvent());
		c.publish(new HandledEvent());
		c.publish(new IgnoredEvent());
		c.publish(new HandledEvent());
		c.publish(new IgnoredEvent());
		c.publish(new HandledEvent());
		c.publish(new IgnoredEvent());
		Thread.sleep(PAUSE_MILLIS);
		assertEquals(5, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	
	// SECTION: INNER CLASSES

	private class HandledEvent
	{
		public void kerBlooey()
		{
			// do nothing.
		}
	}
	
	private class ErroredEvent
	extends HandledEvent
	{
		private int occurrences = 0;

		@Override
		public void kerBlooey()
		{
			if (occurrences++ < 5)
			{
				throw new RuntimeException("KER-BLOOEY!");
			}
		}
	}
	
	private class IgnoredEvent
	{
	}
	
	private class LongEvent
	{
	}

	private static class DomainEventsTestHandler
	implements SelectiveEventSubscriber
	{
		private int callCount = 0;

		@Override
		public void handle(Object event)
		{
			assert(HandledEvent.class.isAssignableFrom(event.getClass()));

			++callCount;
			((HandledEvent) event).kerBlooey();
		}
		
		public int getCallCount()
		{
			return callCount;
		}

		@Override
		public boolean test(Object event)
		{
			return (event instanceof HandledEvent || event instanceof ErroredEvent);
		}		
	}

	private static class DomainEventsTestIgnoredEventsHandler
	implements SelectiveEventSubscriber
	{
		private int callCount = 0;

		@Override
		public void handle(Object event)
		{
			assert(event.getClass().equals(IgnoredEvent.class));
			++callCount;
		}
		
		public int getCallCount()
		{
			return callCount;
		}

		@Override
		public boolean test(Object event)
		{
			return (event instanceof IgnoredEvent);
		}		
	}

	private static class DomainEventsTestLongEventHandler
	implements SelectiveEventSubscriber
	{
		private int callCount = 0;

		@Override
		public void handle(Object event)
		{
			assert(event.getClass().equals(LongEvent.class));
			++callCount;
			try
            {
				// pretend the long event takes 1 second to process...
				System.out.println("Event handler " + this.toString() + " going to sleep..." + callCount);
	            Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
	            e.printStackTrace();
            }
		}
		
		public int getCallCount()
		{
			return callCount;
		}

		@Override
		public boolean test(Object event)
		{
			return (event instanceof LongEvent);
		}		
	}
}
