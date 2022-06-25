package io.micronaut.context.annotation;

import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Singleton
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Context {
}
