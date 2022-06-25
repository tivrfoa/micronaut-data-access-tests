package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

interface InnerConsumer<I> extends CoreSubscriber<I>, Scannable {
}
