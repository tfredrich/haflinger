package com.strategicgains.haflinger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.strategicgains.haflinger.channel.EventChannel;
import com.strategicgains.haflinger.routing.EventChannelRouter;
import com.strategicgains.haflinger.routing.RoutingRule;
import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;
import com.strategicgains.haflinger.simple.SimpleEventChannelBuilder;


/**
 * @author toddf
 * @since May 18, 2011
 */
public class HaflingerTest
{
	private static final int PAUSE_MILLIS = 150;
	private DomainEventsTestHandler handler = new DomainEventsTestHandler();
	private DomainEventsTestIgnoredEventsHandler ignoredHandler = new DomainEventsTestIgnoredEventsHandler();
	private DomainEventsTestLongEventHandler longHandler = new DomainEventsTestLongEventHandler();

	@Before
	public void setup()
	{
		EventChannel q = new SimpleEventChannelBuilder()
			.subscribe(handler)
			.subscribe(ignoredHandler)
			.subscribe(longHandler)
			.build();
		Haflinger.addChannel("primary", q);
	}
	
	@After
	public void teardown()
	{
		Haflinger.shutdown();
	}

	@Test
	public void isSingleton()
	{
		assertTrue(Haflinger.instance() == Haflinger.instance());
	}

	@Test
	public void shouldNotifyEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		Haflinger.publish(new HandledEvent());
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
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
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
		Haflinger.publish(new IgnoredEvent());
		Thread.sleep(150);
		assertEquals(0, handler.getCallCount());
		assertEquals(1, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

//	@Test
//	public void shouldRetryEventHandler()
//	throws Exception
//	{
////		((SimpleEventChannel) Haflinger.getChannel("primary")).retryOnError(true);
//		assertEquals(0, handler.getCallCount());
//		Haflinger.publish(new ErroredEvent());
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
		Haflinger.publish(new ErroredEvent());
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
		Haflinger.publish(new LongEvent());
		Haflinger.publish(new LongEvent());
		Haflinger.publish(new LongEvent());
		Haflinger.publish(new LongEvent());
		Haflinger.publish(new LongEvent());
		Thread.sleep(150);
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		System.out.println("longHandler instance=" + longHandler.toString());
		assertEquals(5, longHandler.getCallCount());
	}

	@Test
	public void shouldOnlyPublishSelected()
	throws Exception
	{
		EventChannelRouter r = new EventChannelRouter();
		r.addChannel(new RoutingRule()
		{
			@Override
			public boolean test(Object event)
			{
				return (HandledEvent.class.equals(event.getClass()));
			}
		}, Haflinger.getChannel("primary"));

		assertTrue(Haflinger.removeChannel("primary"));
		assertTrue(Haflinger.addChannel("primary", r));

		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Thread.sleep(150);
		assertEquals(5, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldPublishMultipleBusses()
	throws Exception
	{
		EventChannel q = new SimpleEventChannelBuilder()
			.subscribe(handler)
			.subscribe(ignoredHandler)
			.subscribe(longHandler)
			.build();
		EventChannelRouter r = new EventChannelRouter();
		r.addChannel(new RoutingRule()
		{
			@Override
			public boolean test(Object event)
			{
				return (HandledEvent.class.equals(event.getClass()));
			}
		}, q);
		Haflinger.addChannel("secondary", r);

		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Haflinger.publish(new HandledEvent());
		Haflinger.publish(new IgnoredEvent());
		Thread.sleep(150);
		assertEquals(10, handler.getCallCount());
		assertEquals(5, ignoredHandler.getCallCount());
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
			return (HandledEvent.class.isAssignableFrom(event.getClass()));
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
			return (IgnoredEvent.class.isAssignableFrom(event.getClass()));
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
			return (LongEvent.class.isAssignableFrom(event.getClass()));
		}
	}
}
