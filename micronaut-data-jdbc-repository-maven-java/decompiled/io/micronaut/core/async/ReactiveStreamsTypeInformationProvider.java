package io.micronaut.core.async;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.TypeInformationProvider;

public final class ReactiveStreamsTypeInformationProvider implements TypeInformationProvider {
   @Override
   public boolean isSpecifiedSingle(@NonNull AnnotationMetadataProvider annotationMetadataProvider) {
      AnnotationMetadata annotationMetadata = annotationMetadataProvider.getAnnotationMetadata();
      return annotationMetadata.hasStereotype(SingleResult.class) && annotationMetadata.booleanValue(SingleResult.NAME).orElse(true);
   }

   @Override
   public boolean isSingle(@NonNull Class<?> type) {
      return Publishers.isSingle(type);
   }

   @Override
   public boolean isReactive(@NonNull Class<?> type) {
      return Publishers.isConvertibleToPublisher(type);
   }

   @Override
   public boolean isCompletable(@NonNull Class<?> type) {
      return Publishers.isCompletable(type);
   }
}
