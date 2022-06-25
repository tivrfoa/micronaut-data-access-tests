package reactor.core.publisher;

import reactor.util.annotation.NonNull;

public abstract class GroupedFlux<K, V> extends Flux<V> {
   @NonNull
   public abstract K key();
}
