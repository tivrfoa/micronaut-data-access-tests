package io.micronaut.http.client.bind;

import io.micronaut.core.annotation.NonNull;
import java.lang.annotation.Annotation;

public interface AnnotatedClientArgumentRequestBinder<A extends Annotation> extends ClientArgumentRequestBinder<Object> {
   @NonNull
   Class<A> getAnnotationType();
}
