package io.micronaut.scheduling.annotation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Type;
import io.micronaut.scheduling.async.AsyncInterceptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Executable
@Around
@Type({AsyncInterceptor.class})
public @interface Async {
   String value() default "scheduled";
}
