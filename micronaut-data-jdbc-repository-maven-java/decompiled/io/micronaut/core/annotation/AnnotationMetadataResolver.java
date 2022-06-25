package io.micronaut.core.annotation;

public interface AnnotationMetadataResolver {
   AnnotationMetadataResolver DEFAULT = new AnnotationMetadataResolver() {
   };

   @NonNull
   default AnnotationMetadata resolveMetadata(@Nullable Class<?> type) {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   @NonNull
   default AnnotationMetadata resolveMetadata(Object object) {
      return this.resolveMetadata(object != null ? object.getClass() : null);
   }
}
