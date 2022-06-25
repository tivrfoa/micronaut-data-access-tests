package io.micronaut.http.bind.binders;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.http.HttpRequest;
import java.lang.annotation.Annotation;

public interface AnnotatedRequestArgumentBinder<A extends Annotation, T> extends RequestArgumentBinder<T>, AnnotatedArgumentBinder<A, T, HttpRequest<?>> {
   static <SA extends Annotation, ST> AnnotatedRequestArgumentBinder of(Class<SA> annotationType, ArgumentBinder<ST, HttpRequest<?>> binder) {
      return new AnnotatedRequestArgumentBinder<SA, ST>() {
         public ArgumentBinder.BindingResult<ST> bind(ArgumentConversionContext<ST> argument, HttpRequest<?> source) {
            return binder.bind(argument, source);
         }

         @Override
         public Class<SA> getAnnotationType() {
            return annotationType;
         }
      };
   }
}
