package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import java.io.Closeable;

public interface LifeCycle<T extends LifeCycle> extends Closeable, AutoCloseable {
   boolean isRunning();

   @NonNull
   default T start() {
      return (T)this;
   }

   @NonNull
   default T stop() {
      return (T)this;
   }

   default void close() {
      this.stop();
   }

   @NonNull
   default T refresh() {
      this.stop();
      this.start();
      return (T)this;
   }
}
