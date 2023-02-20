package com.strategicgains.haflinger.exception;

public class SubscriptionException
extends EventingException
{
	private static final long serialVersionUID = -9035577768966794394L;

	public SubscriptionException()
	{
		super();
	}

	public SubscriptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SubscriptionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SubscriptionException(String message)
	{
		super(message);
	}

	public SubscriptionException(Throwable cause)
	{
		super(cause);
	}
}
