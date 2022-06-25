package io.micronaut.transaction.support;

import io.micronaut.core.annotation.NonNull;
import java.io.Flushable;

public interface TransactionSynchronization extends Flushable {
   default void suspend() {
   }

   default void resume() {
   }

   default void flush() {
   }

   default void beforeCommit(boolean readOnly) {
   }

   default void beforeCompletion() {
   }

   default void afterCommit() {
   }

   default void afterCompletion(@NonNull TransactionSynchronization.Status status) {
   }

   public static enum Status {
      COMMITTED,
      ROLLED_BACK,
      UNKNOWN;
   }
}
