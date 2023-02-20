package com.strategicgains.haflinger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strategicgains.haflinger.channel.EventChannel;


/**
 * Haflinger defines a static public interface for publishing and consuming domain events.
 * Raising an event pushes it to zero or more configured {@link EventChannel}s to be handled asynchronously
 * by a subscribed EventHandler implementor or EventConsumer implementor, depending on the
 * EventChannel implementation.
 *
 * Domain events are publish-subscribe, where when an event is raised, all subscribed
 * Handler instances are notified of an event.
 * 
 * All raised events are handled asynchronously. However, they may NOT be published
 * asynchronously, depending on the underlying transport implementation.
 * 
 * @author toddf
 * @since May 12, 2011
 */
public class Haflinger
{
	private static final Logger LOG = LoggerFactory.getLogger(Haflinger.class);
	private static final Haflinger INSTANCE = new Haflinger();

	private Map<String, EventChannel> channels = new LinkedHashMap<>();

	private Haflinger()
	{
		super();
	}

	/**
	 * Get the Singleton instance of DomainEvents.
	 */
	public static Haflinger instance()
	{
		return INSTANCE;
	}

	/**
	 * Publish an event to a named event channel.
	 * The name of the bus is assigned during a call to addChannel(String, EventChannel).
	 *
	 * Event publishing can only occur after transports are setup and added.
	 * 
	 * @param channelName the name of a specific event channel.
	 * @param event the Object as an event to publish.
	 */
	public static void publish(String channelName, Object event)
	{
		instance()._publish(channelName, event);
	}

	/**
	 * Publish an event, passing it to applicable consumers asynchronously.
	 *
	 * Event publishing can only occur after event busses are setup.
	 * 
	 * @param event the Object as an event to publish.
	 * @throws Exception 
	 */
	public static void publish(Object event)
	{
		instance()._publish(event);
	}

	/**
	 * Register an {@link EventChannel} with Haflinger.
	 * 
	 * @param name the transport name.  Must be unique within Haflinger.
	 * @param channel an EventChannel instance.
	 * @return true if the name is unique and the event bus was added.  Otherwise, false.
	 */
	public static boolean addChannel(String name, EventChannel channel)
	{
		return instance()._addChannel(name, channel);
	}

	/**
	 * Register an {@link EventChannel} with Haflinger using the channel's fully-qualified
	 * classname as the name.
	 * 
	 * @param channel an EventChannel instance.
	 * @return true if the name is unique and the transport was added.  Otherwise, false.
	 */
	public static boolean addChannel(EventChannel channel)
	{
		return instance()._addChannel(channel.getClass().getName(), channel);
	}
	
	/**
	 * Get a registered {@link EventChannel} by name.
	 * 
	 * @param name the name of a channel given at the time of calling addChannel(String, EventChannel).
	 * @return an Transport instance, or null if 'name' not found.
	 */
	public static EventChannel getChannel(String name)
	{
		return instance()._getChannel(name);
	}

	public static boolean removeChannel(String name)
	{
		return instance()._removeChannel(name);
	}
	
	/**
	 * Shutdown all the event transports, releasing their resources cleanly.
	 * 
	 * shutdown() should be called at application termination to cleanly release
	 * all consumed resources.
	 */
	public static void shutdown()
	{
		instance()._shutdown();
	}

	private boolean _addChannel(String name, EventChannel channel)
	{
		if (!channels.containsKey(name))
		{
			channels.put(name, channel);
			return true;
		}
		
		return false;
	}
	
	private EventChannel _getChannel(String name)
	{
		EventChannel channel = channels.get(name);

		if (channel == null)
		{
			throw new RuntimeException("Unknown channel name: " + name);
		}

		return channel;
	}
	
	private boolean _hasChannels()
	{
		return (channels != null);
	}

	private boolean _removeChannel(String name)
	{
		return (channels.remove(name) != null);
	}

	/**
	 * Raise an event on all event channels, passing it to applicable handlers asynchronously.
	 * 
	 * @param event
	 */
	private void _publish(Object event)
	{
		assert(_hasChannels());

		for (Entry<String, EventChannel> entry : channels.entrySet())
		{
			try
			{
				entry.getValue().publish(event);
			}
			catch(RuntimeException e)
			{
				LOG.error("Publishing error occurred on channel: " + entry.getKey(), e);
			}
		}
	}

	/**
	 * Raise an event on a named event bus, passing it to applicable consumers asynchronously.
	 * 
	 * @param name the name of an event bus, assigned during calls to addEventBus(String, Transport).
	 * @param event the event to publish.
	 */
	private void _publish(String name, Object event)
	{
		assert(_hasChannels());

		try
		{
			_getChannel(name).publish(event);
		}
		catch(RuntimeException e)
		{
			LOG.error("Publishing error occurred to channel: " + name, e);			
		}
	}

	private void _shutdown()
	{
		for (EventChannel transport : channels.values())
		{
			transport.shutdown();
		}
		
		channels.clear();
	}
}
