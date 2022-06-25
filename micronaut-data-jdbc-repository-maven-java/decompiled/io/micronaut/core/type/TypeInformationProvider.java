package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import java.util.Optional;

public interface TypeInformationProvider {
   default boolean isSpecifiedSingle(@NonNull AnnotationMetadataProvider annotationMetadataProvider) {
      return false;
   }

   default boolean isSingle(@NonNull Class<?> type) {
      return false;
   }

   default boolean isReactive(@NonNull Class<?> type) {
      return false;
   }

   default boolean isCompletable(@NonNull Class<?> type) {
      return false;
   }

   default boolean isWrapperType(Class<?> type) {
      return type == Optional.class;
   }
}
