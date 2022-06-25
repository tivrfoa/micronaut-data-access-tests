package io.micronaut.context.annotation;

import jakarta.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Scope
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Provided {
}
