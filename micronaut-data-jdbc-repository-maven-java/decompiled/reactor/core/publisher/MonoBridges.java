package reactor.core.publisher;

import java.util.function.Function;
import org.reactivestreams.Publisher;

final class MonoBridges {
   static <R> Mono<R> zip(Function<? super Object[], ? extends R> combinator, Mono<?>[] monos) {
      return Mono.zip(combinator, monos);
   }

   static Mono<Void> when(Publisher<?>[] sources) {
      return Mono.when(sources);
   }
}
