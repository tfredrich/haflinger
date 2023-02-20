package com.strategicgains.haflinger.ignite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.ignite.Ignition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * @author toddf
 * @since Feb 5, 2019
 */
public class IgniteEventChannelBuilderTest
{
	private DomainEventsTestHandler handler = new DomainEventsTestHandler();
	private DomainEventsTestIgnoredEventsHandler ignoredHandler = new DomainEventsTestIgnoredEventsHandler();
	private DomainEventsTestLongEventHandler longHandler = new DomainEventsTestLongEventHandler();
	private IgniteEventChannel channel;

	@BeforeClass
	public static void init()
	{
		Ignition.start();
	}

	@AfterClass
	public static void dump()
	{
		Ignition.stop(true);
	}

	@Before
	public void setup()
	{
		channel = new IgniteEventChannelBuilder()
			.withConfiguration(Ignition.ignite())
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
		Thread.sleep(50);
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
		Thread.sleep(50);
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
		Thread.sleep(50);
		assertEquals(0, handler.getCallCount());
		assertEquals(1, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldNotRetryEventHandler()
	throws Exception
	{
		assertEquals(0, handler.getCallCount());
		channel.publish(new ErroredEvent());
		Thread.sleep(50);
		assertEquals(1, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
		assertEquals(0, longHandler.getCallCount());
	}

	@Test
	public void shouldProcessInParallel()
	throws Exception
	{
		long started = System.currentTimeMillis();
		assertEquals(0, longHandler.getCallCount());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		channel.publish(new LongEvent());
		assertTrue("Publish is taking too long!", (System.currentTimeMillis() - started) < 3000l);
		Thread.sleep(50);
		assertEquals(0, handler.getCallCount());
		assertEquals(0, ignoredHandler.getCallCount());
//		System.out.println("longHandler instance=" + longHandler.toString());
		assertEquals(5, longHandler.getCallCount());
	}

	
	// SECTION: INNER CLASSES
	
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
