package io.micronaut.data.model.runtime;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.TransactionDefinition;
import java.util.Optional;

public abstract class AbstractPreparedDataOperation<R> implements PreparedDataOperation<R> {
   private final StoredDataOperation<R> storedDataOperation;
   private final MethodInvocationContext<?, R> context;

   protected AbstractPreparedDataOperation(MethodInvocationContext<?, R> context, StoredDataOperation<R> storedDataOperation) {
      this.storedDataOperation = storedDataOperation;
      this.context = context;
   }

   @NonNull
   @Override
   public final Optional<Object> getAttribute(CharSequence name) {
      return this.context.getAttribute(name);
   }

   @NonNull
   @Override
   public final <T> Optional<T> getAttribute(CharSequence name, Class<T> type) {
      return this.context.getAttribute(name, type);
   }

   @NonNull
   @Override
   public final ConvertibleValues<Object> getAttributes() {
      return this.context.getAttributes();
   }

   @NonNull
   @Override
   public final Argument<R> getResultArgument() {
      return this.storedDataOperation.getResultArgument();
   }

   @NonNull
   @Override
   public final AnnotationMetadata getAnnotationMetadata() {
      return this.storedDataOperation.getAnnotationMetadata();
   }

   @NonNull
   @Override
   public final Optional<TransactionDefinition> getTransactionDefinition() {
      return this.storedDataOperation.getTransactionDefinition();
   }
}
