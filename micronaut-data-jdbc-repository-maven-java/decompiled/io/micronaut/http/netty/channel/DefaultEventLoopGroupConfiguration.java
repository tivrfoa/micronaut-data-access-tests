package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import java.time.Duration;
import java.util.Optional;

@EachProperty(
   value = "micronaut.netty.event-loops",
   primary = "default"
)
public class DefaultEventLoopGroupConfiguration implements EventLoopGroupConfiguration {
   private final int numThreads;
   private final Integer ioRatio;
   private final boolean preferNativeTransport;
   private final String name;
   private final String executor;
   private final Duration shutdownQuietPeriod;
   private final Duration shutdownTimeout;

   @ConfigurationInject
   public DefaultEventLoopGroupConfiguration(
      @Parameter String name,
      @Bindable(defaultValue = "0") int numThreads,
      @Nullable Integer ioRatio,
      @Bindable(defaultValue = "false") boolean preferNativeTransport,
      @Nullable String executor,
      @Nullable Duration shutdownQuietPeriod,
      @Nullable Duration shutdownTimeout
   ) {
      this.name = name;
      this.numThreads = numThreads;
      this.ioRatio = ioRatio;
      this.preferNativeTransport = preferNativeTransport;
      this.executor = executor;
      this.shutdownQuietPeriod = (Duration)Optional.ofNullable(shutdownQuietPeriod).orElse(Duration.ofSeconds(2L));
      this.shutdownTimeout = (Duration)Optional.ofNullable(shutdownTimeout).orElse(Duration.ofSeconds(15L));
   }

   public DefaultEventLoopGroupConfiguration() {
      this.name = "default";
      this.numThreads = 0;
      this.ioRatio = null;
      this.preferNativeTransport = false;
      this.executor = null;
      this.shutdownQuietPeriod = Duration.ofSeconds(2L);
      this.shutdownTimeout = Duration.ofSeconds(15L);
   }

   @Override
   public int getNumThreads() {
      return this.numThreads;
   }

   @Override
   public Optional<Integer> getIoRatio() {
      return Optional.ofNullable(this.ioRatio);
   }

   @Override
   public Optional<String> getExecutorName() {
      return Optional.ofNullable(this.executor);
   }

   @Override
   public boolean isPreferNativeTransport() {
      return this.preferNativeTransport;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public Duration getShutdownQuietPeriod() {
      return this.shutdownQuietPeriod;
   }

   @Override
   public Duration getShutdownTimeout() {
      return this.shutdownTimeout;
   }
}
