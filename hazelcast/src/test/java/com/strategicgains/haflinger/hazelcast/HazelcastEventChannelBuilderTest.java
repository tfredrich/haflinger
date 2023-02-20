package com.strategicgains.haflinger.hazelcast;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.strategicgains.haflinger.routing.SelectiveEventSubscriber;

/**
 * @author toddf
 * @since Oct 4, 2012
 */
public class HazelcastEventChannelBuilderTest
{
	private DomainEventsTestHandler handler = new DomainEventsTestHandler();
	private DomainEventsTestIgnoredEventsHandler ignoredHandler = new DomainEventsTestIgnoredEventsHandler();
	private DomainEventsTestLongEventHandler longHandler = new DomainEventsTestLongEventHandler();
	private HazelcastEventChannel queue;

	@Before
	public void setup()
	{
		queue = new HazelcastEventChannelBuilder()
			.withTopic(UUID.randomUUID().toString())
			.subscribe(handler)
			.subscribe(ignoredHandler)
			.subscribe(longHandler)
		    .build();
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
		queue.publish(new HandledEvent());
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
		queue.publish(new IgnoredEvent());
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
		queue.publish(new ErroredEvent());
		Thread.sleep(50);
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
