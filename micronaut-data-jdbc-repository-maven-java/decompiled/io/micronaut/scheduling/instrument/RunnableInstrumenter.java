package io.micronaut.scheduling.instrument;

public interface RunnableInstrumenter {
   default Runnable instrument(Runnable command) {
      return command;
   }
}
