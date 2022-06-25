package io.micronaut.http.bind.binders;

import io.micronaut.http.annotation.Body;

public interface BodyArgumentBinder<T> extends AnnotatedRequestArgumentBinder<Body, T> {
   @Override
   default Class<Body> getAnnotationType() {
      return Body.class;
   }
}
