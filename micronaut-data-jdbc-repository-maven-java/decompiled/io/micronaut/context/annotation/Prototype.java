package io.micronaut.context.annotation;

import jakarta.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Scope
@Bean
public @interface Prototype {
   String NAME = Prototype.class.getName();
}
