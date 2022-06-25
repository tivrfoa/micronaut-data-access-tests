package io.micronaut.retry.annotation;

import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Secondary;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Secondary
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Executable
public @interface Fallback {
   Class<? extends Throwable>[] includes() default {};

   Class<? extends Throwable>[] excludes() default {};
}
