package io.micronaut.inject.annotation;

import io.micronaut.core.naming.Named;
import java.lang.annotation.Annotation;

public interface NamedAnnotationTransformer extends AnnotationTransformer<Annotation>, Named {
}
