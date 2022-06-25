package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.TransactionDefinition;
import java.util.Optional;

public interface StoredDataOperation<R> extends AnnotationMetadataProvider {
   @NonNull
   default Optional<TransactionDefinition> getTransactionDefinition() {
      return Optional.empty();
   }

   @NonNull
   Argument<R> getResultArgument();
}
