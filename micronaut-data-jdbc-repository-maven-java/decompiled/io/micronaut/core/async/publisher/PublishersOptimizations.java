package io.micronaut.core.async.publisher;

import io.micronaut.core.annotation.Internal;
import java.util.List;

@Internal
public final class PublishersOptimizations {
   private final List<Class<?>> reactiveTypes;
   private final List<Class<?>> singleTypes;
   private final List<Class<?>> completableTypes;

   public PublishersOptimizations(List<Class<?>> reactiveTypes, List<Class<?>> singleTypes, List<Class<?>> completableTypes) {
      this.reactiveTypes = reactiveTypes;
      this.singleTypes = singleTypes;
      this.completableTypes = completableTypes;
   }

   List<Class<?>> getReactiveTypes() {
      return this.reactiveTypes;
   }

   List<Class<?>> getSingleTypes() {
      return this.singleTypes;
   }

   List<Class<?>> getCompletableTypes() {
      return this.completableTypes;
   }
}
