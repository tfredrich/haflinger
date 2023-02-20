Haflinger
=========

Haflinger is an asynchronous eventing library that supports composable event pipelines for in-memory (intra-JVM), across Java processes (inter-JVM) and system-wide, publish-subscribe eventing (via Kafka or AWS SQS/SNS).

This simple Java library provides a Singleton interface (see Haflinger class) to publish events (which are just POJOs) throughout the domain layer. Builder (GoF) classes exist for creating event channels and subscribing to them.

Subscribing to an event channel is simply done via an EventHandler implementor, which implements the handle(Object) method. Each EventHandler that is subscribed will receive every event unless it implements the SelectiveEventHandler interface, which allows the event handler to implement the isSelected(Object) method to check the message against selection criteria.

Essentially, Haflinger makes the implementation of eventing simple, abstracting out the complexity of various messaging systems, allowing the simplicity of an in-memory queue be the metaphor for local, across JVM, or enterprise-wide event generation.

Supported Use Cases
-------------------

![Haflinger Use Cases](https://bytebucket.org/tfredrich/halflinger/raw/a4a55ba60b65e3e99265c6b57d76f743bb03a455/docs/images/Overview.png?token=7f4fb1c99632c7ba625a0d5c77a3c81d4f2261c7 "Haflinger Use Cases")

Configuring an Event Channel
============================

To get started, the first thing we need is an EventChannel implementation to pass messages on. This only needs to happen at application startup.

Haflinger currently supports these implementations of EventChannel:
* SimpleEventChannel (in 'core') which is a simple in-memory queue.
* HazelcastEventChannel (in 'hazelcast') which uses Hazelcast to send messages across JVMs.
* IgniteEventChannel (in 'ignite') which uses Apache Ignite to send messages across JVMs.

For our example, we'll use the SimpleEventChannel, but know that each EventChannel implementation has its own builder interface.

Simple Use Case
---------------

Let's do the top example from the simple use cases below:

![Simple Use Case](https://bytebucket.org/tfredrich/halflinger/raw/47d84a12c331716aee4a05128f6b890b3bb0f8d8/docs/images/Simple_Cases.png?token=663a08c472cb2b84f7865328f5b846bb7f72b4c3 "Simple Use Case")

Using a Builder
---------------

```java
SubscribableEventChannel simpleChannel = new SimpleEventChannelBuilder()
	.subscribe(new DomainEventsTestHandler())	// Optional. Do this here or later as shown below.
	.shouldRetryOnError(true)					// Optional. Defaults to false.
	.build();									// Build the EventChannel.
```

SimpleEventChannel allows local (within the JVM) subscriptions. And since we're using SimpleEventChannel, we must create subscriptions within this same process. However, if we were using one of the distributed event channels
(e.g. Ignite, or Hazelcast), we wouldn't need to.

EventHandler Implementation
---------------------------

First up, before we subscribe to the even channel, we have to create an EventHandler implementation which requires a handle(Object) method.

```java
public class DomainEventsTestHandler
implements EventHandler
{
	/**
	 * Process the incoming message. Note that all messages are POJOs.
	 */
	@Override
	public void handle(Object message)
	{
		Event event = (Event) message;
		System.out.println(event.getData());
	}
}
```

Creating a Subscription
-----------------------

Now that we have our EventHandler implementation, we subscribe to the event channel as follows:

```java
simpleChannel.subscribe(new DomainEventsTestHandler());
```

*Note* that this can also be done via the builder above.

Configuring DomainEvents
========================

Once the EventChannel is configured, you can either use it directly to publish events, or you can add it to the Singleton DomainEvents object to allow publishing from anywhere
in your application. Since DomainEvents allows multiple EventChannel instances to be added, you can publish events to multiple channels at once. Channels are added by name, where the name 
for each channel must be unique.

For example, you may want a 'local' channel that is configured to send certain events only within the JVM and a 'remote' channel that sends events to JVMs in a cluster. By default,
DomainEvents will send all events to all channels. However, you can configure a RoutingEventChannel with routing rules to selectively publish to the channels (see 'Advanced Use Cases' section below).

```java
DomainEvents.addChannel("simple", simpleChannel);
```

That's all there is to it. Now we're ready to publish events.

Publishing Events
=================

Send your event POJOs to the event channels configured in DomainEvents.

```java
// Define arbitrary POJOs to describe your event(s)
public class Event
{
	private String data;

	public Event(String data)
	{
		this.data = data;
	}

	public String getData()
	{
		return data;
	}
}

...


// Publish a new event...
DomainEvents.publish(new Event("something happened!"));
```

That's all. Events are just simply plain-old Java objects. DomainEvents will take care of delivering your message to the registered EventChannel instances.

Cleaning Up
===========

At application shutdown, we need to cleanup the resource being held by the EventChannel implementation(s) and allow them to finish processing messages or deallocating resources
as necessary.

If your using the EventChannel directly, simply call:

```java
	simpleChannel.shutdown();
```

Or, if you're using the Singleton, DomainEvents (recommended), call:

```java
	DomainEvents.shutdown();
```

Advanced Use Cases
==================

Here's how to implement some more complex use cases in Haflinger, including publish routing and selective subscriptions.

Publish Routing
---------------

Use publish routing when you have multiple EventChannels configured but don't want DomainEvents to publish all events to all channels (which is the default behavior). In other
words, when you want to have messages published to certain channels based on some selection criteria use publish routing, which is accomplished by use of the
*RoutingEventChannel* class.

In this use case, we setup a RoutingEventChannel, which contains one or more EventChannel references with routing rules for each. The routing rules determine
whether a given event is published to a particular channel. This allows us to have messages routed to particular channels or selectively publish messages to a single channel.

![Selective Publishing](https://bytebucket.org/tfredrich/halflinger/raw/47d84a12c331716aee4a05128f6b890b3bb0f8d8/docs/images/Publish_Routing.png?token=0dd63bb390619835cd8933f2f325b30e46fa7c20 "Selective Publishing")

For this example, let's say that we want event types published to a different channel, like the Datatype Channel Enterprise Integration Pattern (EIP). We'll assume, for now, that we only
have two event types as follows:

```java
public class FirstEvent
{
	private String data;

	public FirstEvent(String data)
	{
		this.data = data;
	}

	public String getData()
	{
		return data;
	}
}

public class SecondEvent
{
	private int data;

	public SecondEvent(int data)
	{
		this.data = data;
	}

	public int getData()
	{
		return data;
	}
}
```

First, we'll create two channels--one for each type:

```java
SubscribableEventChannel firstChannel = new SimpleEventChannelBuilder()
	.subscribe(new FirstTypeHandler())
	.build();							// Build the EventChannel.

SubscribableEventChannel secondChannel = new SimpleEventChannelBuilder()
	.subscribe(new SecondTypeHandler())
	.build();							// Build the EventChannel.
```

OK, with the channels defined, now we create a RoutingEventChannel and set some RoutingRules. A RoutingRule is simply an interface implementation that defines a method, boolean appliesTo(Object event).
The RoutingRule implementation along with the channels defined above, get set on the RoutingEventChannel like so:

```java
RoutingEventChannel routedChannel = new RoutingEventChannel();
routedChannel.addChannel(new FirstRoutingRule(), firstChannel);
routedChannel.addChannel(new SecondRoutingRule(), secondChannel);

public class FirstRoutingRule
implements RoutingRule
{
	@Override
	public boolean appliesTo(Object event)
	{
		return FirstEvent.class.isAssignableFrom(event.getClass());
	}
}

public class SecondRoutingRule
implements RoutingRule
{
	@Override
	public boolean appliesTo(Object event)
	{
		return SecondEvent.class.isAssignableFrom(event.getClass());
	}
}
```

Now we can either add the RoutingEventChannel instance, router, to DomainEvents with a name or use it directly to publish events.

```java
DomainEvents.addChannel("routed", routedChannel);

DomainEvents.publish(new FirstEvent("something happened!"));
DomainEvents.publish(new SecondEvent(42));
```

OR...

```java
routedChannel.publish(new FirstEvent("something happened!"));
routedChannel.publish(new SecondEvent(42));
```

*Note* that in this example, we didn't create any subscribers to the two channels. That's an exercise for the reader--which is just like above... ;)  Or checkout the *Selective Subscriptions*
section, below. That might give you a hint.

Selective Subscriptions
-----------------------

This use case is the converse of *Publish Routing*, in that, here only EventHandlers receive messages from a channel when some selection criteria are met. Use this pattern when
you want an EventHandler to ignore some messages on a channel. Selective subscriptions are accomplished by using a SelectiveEventHandler implementation and overriding
*boolean isSelected(Object)* method.

For this use case, we create a SelectiveEventHandler, which allows the messaging system to determine whether a message should be sent to the subscriber or not. The SelectiveEventHandler
interface has an additional method, boolean isSelected(Object), which is called by EventChannel to determine whether the EventHandler should receive the message. This potentially has the
down-side that, for enterprise-wide messaging infrastructures (like Kafka or AWS SQS/SNS), the message is actually sent before it is determined to be deliverable. Care must be taken to avoid this
use case and create an actual selective subscription (for example, via header selection) at subscribe time for these types of channels.

![Selective Subscription](https://bytebucket.org/tfredrich/halflinger/raw/47d84a12c331716aee4a05128f6b890b3bb0f8d8/docs/images/Subscribe_Routing.png?token=433178174bf4fde55062a8d73882c9e6ba706abe "Selective Subscription")

```java
public class DomainEventsSelectiveHandler
implements SelectiveEventHandler
{
	/**
	 * Process the incoming message.
	 */
	@Override
	public void handle(Object message)
	{
		Event event = (Event) message;
		System.out.println(event.getData());
	}

	/**
	 * Determine if this EventHandler cares about this message.
	 */
	@Override
	public boolean isSelected(Object message)
	{
		return SecondEvent.class.isAssignableFrom(message.getClass());
	}
}
```

Simply by subscribing this *DomainEventsSelectiveHandler* to a channel will cause Haflinger to call isSelected() before sending the message to the handler, only sending messages
of type *SecondEvent*.

About the Name
--------------

Eventing (also known as horse trials) is an equestrian event where a single horse and rider combination compete against other combinations across the three disciplines of dressage, cross-country, and show jumping.

The Haflinger is a breed of horse developed in Austria and northern Italy during the late nineteenth century. Haflinger horses are relatively small, are always chestnut in color, have distinctive gaits described as energetic but smooth, and are well-muscled yet elegant. Haflingers, developed for use in mountainous terrain, are known for their hardiness.

So... a small, elegant, hardy library that supports three disciplines of messaging.

![A Haflinger](https://s-media-cache-ak0.pinimg.com/236x/a8/8d/a8/a88da87da7cedbf1ba1e1fc881cc6faf.jpg "A Haflinger")