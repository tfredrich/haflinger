package com.strategicgains.haflinger.simple;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * @author toddf
 * @since Oct 4, 2012
 */
public class SimpleEventChannelBuilderTest
{
	private DomainEventsTestHandler handler = new DomainEventsTestHandler();
	private DomainEventsTestIgnoredEventsHandler ignoredHandler = new DomainEventsTestIgnoredEventsHandler();
	private DomainEventsTestLongEventHandler longHandler = new DomainEventsTestLongEventHandler();
	private SimpleEventChannel channel;

	@Before
	public void setup()
	{
		channel = new SimpleEventChannelBuilder()
			.subscribe(handler)
			.subscribe(ignoredHandler)
			.subscribe(longHandler)
		    .build();
	}
	
	@After
	public void teardown()
	{
		channel.shutdown();
	}

	@Test
	public void shouldNotifyEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		channel.publish(new HandledEvent());
		Thread.sleep(150);
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
		channel.publish(new HandledEvent());
		channel.publish(new IgnoredEvent());
		channel.publish(new HandledEvent());
		channel.publish(new IgnoredEvent());
		channel.publish(new HandledEvent());
		channel.publish(new IgnoredEvent());
		channel.publish(new HandledEvent());
		channel.publish(new IgnoredEvent());
		channel.publish(new HandledEvent());
		channel.publish(new IgnoredEvent());
		Thread.sleep(150);
		assertEquals(5, handler.getCallCount());
		assertEquals(5, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldNotNotifyEventHandler()
	throws Exception
	{
		assertEquals(0, ignoredHandler.getCallCount());
		channel.publish(new IgnoredEvent());
		Thread.sleep(150);
		assertEquals(0, handler.getCallCount());
		assertEquals(1, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

//	@Test
//	public void shouldRetryEventHandler()
//	throws Exception
//	{
//		channel = new SimpleEventChannelBuilder()
//			.subscribe(handler)
//			.subscribe(ignoredHandler)
//			.subscribe(longHandler)
////			.shouldRepublishOnError(true)
//			.build();
//
//		assertEquals(0, handler.getCallCount());
//		channel.publish(new ErroredEvent());
//		Thread.sleep(150);
//		assertEquals(6, handler.getCallCount());
//		assertEquals(0, ignoredHandler.getCallCount());
//		assertEquals(0, longHandler.getCallCount());
//	}

	@Test
	public void shouldNotRetryEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		channel.publish(new ErroredEvent());
		Thread.sleep(150);
		assertEquals(1, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldProcessInParallel()
	throws Exception
	{
		assertEquals(0, longHandler.getCallCount());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		Thread.sleep(150);
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(5, longHandler.getCallCount());
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
			return(event instanceof LongEvent);
		}		
	}
}
