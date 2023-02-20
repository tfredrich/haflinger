package com.strategicgains.haflinger.routing;

import com.strategicgains.haflinger.consumer.EventSubscriber;

/**
 * Based on a RoutingRule (which is simply a Predicate), handles
 * or rejects available events. If the RoutingRule tests true, the
 * event is processed. Otherwise, it is rejected.
 * 
 * The RoutingRule is tested for each event, so time-consuming or otherwise
 * expensive rules are discouraged.
 * 
 * @author tfredrich
 *
 */
public interface SelectiveEventSubscriber
extends EventSubscriber, RoutingRule
{
}
