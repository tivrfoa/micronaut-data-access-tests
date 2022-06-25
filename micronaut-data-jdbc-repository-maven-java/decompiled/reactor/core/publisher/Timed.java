package reactor.core.publisher;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public interface Timed<T> extends Supplier<T> {
   T get();

   Duration elapsed();

   Duration elapsedSinceSubscription();

   Instant timestamp();
}
