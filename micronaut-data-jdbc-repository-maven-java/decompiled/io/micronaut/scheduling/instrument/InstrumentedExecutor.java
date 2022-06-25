package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;
import java.util.concurrent.Executor;

public interface InstrumentedExecutor extends Executor, RunnableInstrumenter {
   Executor getTarget();

   default void execute(@NonNull Runnable command) {
      this.getTarget().execute(this.instrument(command));
   }
}
