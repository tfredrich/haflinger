package com.strategicgains.haflinger.exception;

public class PublishingException
extends EventingException
{
	private static final long serialVersionUID = -9035577768966794394L;

	public PublishingException()
	{
		super();
	}

	public PublishingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PublishingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PublishingException(String message)
	{
		super(message);
	}

	public PublishingException(Throwable cause)
	{
		super(cause);
	}
}
