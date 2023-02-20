package com.strategicgains.haflinger.ignite;

/**
 * @author toddf
 * @since Oct 5, 2012
 */
public class ErroredEvent
extends HandledEvent
{
    private static final long serialVersionUID = -2659798178870960451L;
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
