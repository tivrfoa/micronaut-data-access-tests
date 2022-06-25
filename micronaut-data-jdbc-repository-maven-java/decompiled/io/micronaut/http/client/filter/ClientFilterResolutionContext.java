package io.micronaut.http.client.filter;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ClientFilterResolutionContext implements AnnotationMetadataProvider {
   private final List<String> clientIds;
   private final AnnotationMetadata annotationMetadata;

   public ClientFilterResolutionContext(List<String> clientIds, AnnotationMetadata annotationMetadata) {
      this.clientIds = clientIds;
      this.annotationMetadata = annotationMetadata;
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Nullable
   public List<String> getClientIds() {
      return this.clientIds != null ? this.clientIds : Collections.emptyList();
   }
}
