package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import io.micronaut.transaction.interceptor.DefaultTransactionAttribute;
import io.micronaut.transaction.support.DefaultTransactionDefinition;
import java.time.Duration;
import java.util.Optional;

public class DefaultStoredDataOperation<R> implements StoredDataOperation<R> {
   public static final DefaultTransactionDefinition NO_TRANSACTION = new DefaultTransactionDefinition();
   private final ExecutableMethod<?, ?> method;
   private TransactionDefinition transactionDefinition;

   public DefaultStoredDataOperation(ExecutableMethod<?, ?> method) {
      this.method = method;
   }

   @NonNull
   @Override
   public final Optional<TransactionDefinition> getTransactionDefinition() {
      if (this.transactionDefinition == null) {
         AnnotationValue<TransactionalAdvice> annotation = this.method.getAnnotation(TransactionalAdvice.class);
         if (annotation != null) {
            DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
            attribute.setName(this.method.getDeclaringType().getSimpleName() + "." + this.method.getMethodName());
            attribute.setReadOnly(annotation.isTrue("readOnly"));
            annotation.intValue("timeout").ifPresent(value -> attribute.setTimeout(Duration.ofSeconds((long)value)));
            Class[] noRollbackFors = annotation.classValues("noRollbackFor");
            attribute.setNoRollbackFor(noRollbackFors);
            annotation.enumValue("propagation", TransactionDefinition.Propagation.class).ifPresent(attribute::setPropagationBehavior);
            annotation.enumValue("isolation", TransactionDefinition.Isolation.class).ifPresent(attribute::setIsolationLevel);
            this.transactionDefinition = attribute;
         } else {
            this.transactionDefinition = NO_TRANSACTION;
         }
      }

      return this.transactionDefinition != NO_TRANSACTION ? Optional.of(this.transactionDefinition) : Optional.empty();
   }

   @NonNull
   @Override
   public final Argument<R> getResultArgument() {
      return this.method.getReturnType().asArgument();
   }

   @Override
   public final AnnotationMetadata getAnnotationMetadata() {
      return this.method.getAnnotationMetadata();
   }
}
