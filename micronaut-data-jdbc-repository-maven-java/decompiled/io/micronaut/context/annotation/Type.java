package io.micronaut.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {
   String NAME = Type.class.getName();

   Class<?>[] value();
}
