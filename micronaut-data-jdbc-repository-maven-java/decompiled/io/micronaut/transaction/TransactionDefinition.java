package io.micronaut.transaction;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.support.DefaultTransactionDefinition;
import java.time.Duration;

public interface TransactionDefinition {
   TransactionDefinition DEFAULT = new DefaultTransactionDefinition();
   TransactionDefinition READ_ONLY = new DefaultTransactionDefinition() {
      {
         this.setReadOnly(true);
      }
   };
   Duration TIMEOUT_DEFAULT = Duration.ofMillis(-1L);

   @NonNull
   default TransactionDefinition.Propagation getPropagationBehavior() {
      return TransactionDefinition.Propagation.REQUIRED;
   }

   @NonNull
   default TransactionDefinition.Isolation getIsolationLevel() {
      return TransactionDefinition.Isolation.DEFAULT;
   }

   @NonNull
   default Duration getTimeout() {
      return TIMEOUT_DEFAULT;
   }

   default boolean isReadOnly() {
      return false;
   }

   @Nullable
   default String getName() {
      return null;
   }

   @NonNull
   static TransactionDefinition of(@NonNull TransactionDefinition.Propagation propagationBehaviour) {
      return new DefaultTransactionDefinition(propagationBehaviour);
   }

   public static enum Isolation {
      DEFAULT(-1),
      READ_UNCOMMITTED(1),
      READ_COMMITTED(2),
      REPEATABLE_READ(4),
      SERIALIZABLE(8);

      private final int code;

      private Isolation(int code) {
         this.code = code;
      }

      public int getCode() {
         return this.code;
      }

      public static TransactionDefinition.Isolation valueOf(int code) {
         switch(code) {
            case 1:
               return READ_UNCOMMITTED;
            case 2:
               return READ_COMMITTED;
            case 3:
            case 5:
            case 6:
            case 7:
            default:
               return DEFAULT;
            case 4:
               return REPEATABLE_READ;
            case 8:
               return SERIALIZABLE;
         }
      }
   }

   public static enum Propagation {
      REQUIRED,
      SUPPORTS,
      MANDATORY,
      REQUIRES_NEW,
      NOT_SUPPORTED,
      NEVER,
      NESTED;
   }
}
