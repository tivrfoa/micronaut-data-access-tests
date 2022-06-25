package io.micronaut.core.convert;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.type.Argument;
import java.util.Map;

public interface ArgumentConversionContext<T> extends ConversionContext, AnnotationMetadataProvider {
   Argument<T> getArgument();

   @Override
   default Argument[] getTypeParameters() {
      return this.getArgument().getTypeParameters();
   }

   @Override
   default Map<String, Argument<?>> getTypeVariables() {
      return this.getArgument().getTypeVariables();
   }

   @Override
   default AnnotationMetadata getAnnotationMetadata() {
      return this.getArgument().getAnnotationMetadata();
   }

   default ArgumentConversionContext<T> with(AnnotationMetadata annotationMetadata) {
      return new DefaultArgumentConversionContext(this.getArgument(), this.getLocale(), this.getCharset()) {
         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return annotationMetadata;
         }
      };
   }
}
