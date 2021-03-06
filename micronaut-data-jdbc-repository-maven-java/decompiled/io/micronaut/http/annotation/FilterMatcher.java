package io.micronaut.http.annotation;

import io.micronaut.http.HttpMethod;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterMatcher {
   String NAME = FilterMatcher.class.getName();

   HttpMethod[] methods() default {};
}
