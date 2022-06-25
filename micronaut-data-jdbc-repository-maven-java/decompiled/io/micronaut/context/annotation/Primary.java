package io.micronaut.context.annotation;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
   String NAME = Primary.class.getName();
   String SIMPLE_NAME = Primary.class.getSimpleName();
}
