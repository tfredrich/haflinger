package com.strategicgains.haflinger.routing;

import com.strategicgains.haflinger.consumer.EventConsumer;

/**
 * Based on a RoutingRule (which is simply a Predicate), consumes
 * or rejects available events. If the RoutingRule tests true, the
 * event is consumed. Otherwise, it is rejected.
 * 
 * The RoutingRule is tested for each event, so time-consuming or otherwise
 * expensive rules are discouraged.
 * 
 * @author tfredrich
 *
 */
public interface SelectiveEventConsumer
extends EventConsumer, RoutingRule
{
}
