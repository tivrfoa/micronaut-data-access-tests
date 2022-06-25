package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.filter.FilterPatternStyle;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Singleton
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {
   String MATCH_ALL_PATTERN = "/**";

   String[] value() default {};

   FilterPatternStyle patternStyle() default FilterPatternStyle.ANT;

   @AliasFor(
      member = "value"
   )
   String[] patterns() default {};

   HttpMethod[] methods() default {};

   String[] serviceId() default {};
}
