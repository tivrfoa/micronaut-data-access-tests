package io.micronaut.inject.annotation;

import io.micronaut.core.naming.Named;
import java.lang.annotation.Annotation;

public interface NamedAnnotationMapper extends AnnotationMapper<Annotation>, Named {
}
