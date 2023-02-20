package com.strategicgains.haflinger.exception;

public class ConsumptionException
extends EventingException
{
	private static final long serialVersionUID = -9035577768966794394L;

	public ConsumptionException()
	{
		super();
	}

	public ConsumptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConsumptionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConsumptionException(String message)
	{
		super(message);
	}

	public ConsumptionException(Throwable cause)
	{
		super(cause);
	}
}
