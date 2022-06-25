package reactor.core;

import org.reactivestreams.Publisher;

public interface CorePublisher<T> extends Publisher<T> {
   void subscribe(CoreSubscriber<? super T> var1);
}
