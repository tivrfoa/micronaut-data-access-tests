package io.micronaut.validation.validator.extractors;

import io.micronaut.core.annotation.NonNull;
import java.util.Optional;
import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

public interface ValueExtractorRegistry {
   @NonNull
   <T> Optional<ValueExtractor<T>> findValueExtractor(@NonNull Class<T> targetType);

   @NonNull
   <T> Optional<ValueExtractor<T>> findUnwrapValueExtractor(@NonNull Class<T> targetType);

   @NonNull
   default <T> ValueExtractor<T> getValueExtractor(@NonNull Class<T> targetType) {
      return (ValueExtractor<T>)this.findValueExtractor(targetType)
         .orElseThrow(() -> new ValidationException("No value extractor for target type [" + targetType + "]"));
   }
}
