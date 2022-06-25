package io.micronaut.http.netty.channel;

import io.micronaut.core.naming.Named;
import java.time.Duration;
import java.util.Optional;

public interface EventLoopGroupConfiguration extends Named {
   String EVENT_LOOPS = "micronaut.netty.event-loops";
   String DEFAULT = "default";
   String DEFAULT_LOOP = "micronaut.netty.event-loops.default";
   long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2L;
   long DEFAULT_SHUTDOWN_TIMEOUT = 15L;

   int getNumThreads();

   Optional<Integer> getIoRatio();

   Optional<String> getExecutorName();

   boolean isPreferNativeTransport();

   default Duration getShutdownQuietPeriod() {
      return Duration.ofSeconds(2L);
   }

   default Duration getShutdownTimeout() {
      return Duration.ofSeconds(15L);
   }
}
