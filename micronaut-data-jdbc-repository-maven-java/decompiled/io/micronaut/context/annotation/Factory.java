package io.micronaut.context.annotation;

import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@DefaultScope(Singleton.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Factory {
}
