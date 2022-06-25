package io.micronaut.transaction.support;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {
   public static final String PREFIX_PROPAGATION = "PROPAGATION_";
   public static final String PREFIX_ISOLATION = "ISOLATION_";
   public static final String PREFIX_TIMEOUT = "timeout_";
   public static final String READ_ONLY_MARKER = "readOnly";
   private TransactionDefinition.Propagation propagationBehavior = TransactionDefinition.Propagation.REQUIRED;
   private TransactionDefinition.Isolation isolationLevel = TransactionDefinition.Isolation.DEFAULT;
   private Duration timeout = TIMEOUT_DEFAULT;
   private boolean readOnly = false;
   @Nullable
   private String name;

   public DefaultTransactionDefinition() {
   }

   public DefaultTransactionDefinition(TransactionDefinition other) {
      this.propagationBehavior = other.getPropagationBehavior();
      this.isolationLevel = other.getIsolationLevel();
      this.timeout = other.getTimeout();
      this.readOnly = other.isReadOnly();
      this.name = other.getName();
   }

   public DefaultTransactionDefinition(@NonNull TransactionDefinition.Propagation propagationBehavior) {
      Objects.requireNonNull(propagationBehavior, "Argument [propagationBehavior] cannot be null");
      this.propagationBehavior = propagationBehavior;
   }

   public final void setPropagationBehavior(@NonNull TransactionDefinition.Propagation propagationBehavior) {
      if (propagationBehavior == null) {
         throw new IllegalArgumentException("Only values of propagation constants allowed");
      } else {
         this.propagationBehavior = propagationBehavior;
      }
   }

   @NonNull
   @Override
   public final TransactionDefinition.Propagation getPropagationBehavior() {
      return this.propagationBehavior;
   }

   public final void setIsolationLevel(@NonNull TransactionDefinition.Isolation isolationLevel) {
      if (isolationLevel == null) {
         throw new IllegalArgumentException("Only values of isolation constants allowed");
      } else {
         this.isolationLevel = isolationLevel;
      }
   }

   @NonNull
   @Override
   public final TransactionDefinition.Isolation getIsolationLevel() {
      return this.isolationLevel;
   }

   public final void setTimeout(@NonNull Duration timeout) {
      if (timeout != null && !timeout.isNegative()) {
         this.timeout = timeout;
      } else {
         throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
      }
   }

   @NonNull
   @Override
   public final Duration getTimeout() {
      return this.timeout != null ? this.timeout : TransactionDefinition.TIMEOUT_DEFAULT;
   }

   public final void setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
   }

   @Override
   public final boolean isReadOnly() {
      return this.readOnly;
   }

   public final void setName(String name) {
      this.name = name;
   }

   @Nullable
   @Override
   public final String getName() {
      return this.name;
   }

   public boolean equals(@Nullable Object other) {
      return this == other || other instanceof TransactionDefinition && this.toString().equals(other.toString());
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public String toString() {
      return this.getDefinitionDescription().toString();
   }

   private StringBuilder getDefinitionDescription() {
      StringBuilder result = new StringBuilder();
      result.append(this.propagationBehavior.ordinal());
      result.append(',');
      result.append(this.isolationLevel.getCode());
      if (this.timeout != TIMEOUT_DEFAULT) {
         result.append(',');
         result.append("timeout_").append(this.timeout);
      }

      if (this.readOnly) {
         result.append(',');
         result.append("readOnly");
      }

      return result;
   }
}
