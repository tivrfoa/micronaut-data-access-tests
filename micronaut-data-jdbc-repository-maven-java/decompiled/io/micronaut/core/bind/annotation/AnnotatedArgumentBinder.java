package io.micronaut.core.bind.annotation;

import io.micronaut.core.bind.ArgumentBinder;
import java.lang.annotation.Annotation;

public interface AnnotatedArgumentBinder<A extends Annotation, T, S> extends ArgumentBinder<T, S> {
   Class<A> getAnnotationType();
}
