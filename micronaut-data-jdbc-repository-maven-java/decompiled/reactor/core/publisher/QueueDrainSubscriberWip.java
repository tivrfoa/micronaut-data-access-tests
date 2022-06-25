package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicInteger;

class QueueDrainSubscriberWip extends QueueDrainSubscriberPad0 {
   final AtomicInteger wip = new AtomicInteger();
}
