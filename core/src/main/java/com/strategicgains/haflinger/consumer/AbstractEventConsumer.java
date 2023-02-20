package com.strategicgains.haflinger.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strategicgains.haflinger.channel.Consumable;
import com.strategicgains.haflinger.exception.ConsumptionException;

public abstract class AbstractEventConsumer
implements EventConsumer
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEventConsumer.class);
	private static final long DEFAULT_TIMEOUT_MILLIS = 1000l;

	private long timeoutMillis;
	private boolean shuttingDown = false;

	public AbstractEventConsumer()
	{
		this(DEFAULT_TIMEOUT_MILLIS);
	}

	public AbstractEventConsumer(long timeoutMillis)
	{
		super();
		this.timeoutMillis = timeoutMillis;
	}

	@Override
	public void consume(Consumable consumable)
	throws ConsumptionException
	{
		LOG.info("Consumer starting");

		do
		{
			Object event = consumable.get(timeoutMillis);

			try
			{
				if (event != null)
				{
					processEvent(event);
					consumable.commit();
				}
			}
			catch (Exception e)
			{
				LOG.error("Error consuming event: " + event.toString(), e);
			}
		}
		while (!shuttingDown);

		LOG.info("Consumer shutting down");
	}

	@Override
	public void shutdown()
	{
		this.shuttingDown = true;
	}

	protected abstract void processEvent(Object event)
	throws Exception;
}
