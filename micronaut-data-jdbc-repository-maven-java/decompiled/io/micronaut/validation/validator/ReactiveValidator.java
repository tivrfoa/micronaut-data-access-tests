package io.micronaut.validation.validator;

import io.micronaut.core.annotation.NonNull;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

public interface ReactiveValidator {
   @NonNull
   <T> Publisher<T> validatePublisher(@NonNull Publisher<T> publisher, Class<?>... groups);

   @NonNull
   <T> CompletionStage<T> validateCompletionStage(@NonNull CompletionStage<T> completionStage, Class<?>... groups);
}
