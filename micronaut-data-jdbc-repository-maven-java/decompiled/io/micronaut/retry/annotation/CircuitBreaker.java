package io.micronaut.retry.annotation;

import io.micronaut.context.annotation.AliasFor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.constraints.Digits;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retryable
public @interface CircuitBreaker {
   int MAX_RETRY_ATTEMPTS = 4;

   @AliasFor(
      annotation = Retryable.class,
      member = "includes"
   )
   Class<? extends Throwable>[] includes() default {};

   @AliasFor(
      annotation = Retryable.class,
      member = "excludes"
   )
   Class<? extends Throwable>[] excludes() default {};

   @Digits(
      integer = 4,
      fraction = 0
   )
   @AliasFor(
      annotation = Retryable.class,
      member = "attempts"
   )
   String attempts() default "3";

   @AliasFor(
      annotation = Retryable.class,
      member = "delay"
   )
   String delay() default "500ms";

   @Digits(
      integer = 2,
      fraction = 2
   )
   @AliasFor(
      annotation = Retryable.class,
      member = "multiplier"
   )
   String multiplier() default "0";

   @AliasFor(
      annotation = Retryable.class,
      member = "maxDelay"
   )
   String maxDelay() default "5s";

   String reset() default "20s";

   @AliasFor(
      annotation = Retryable.class,
      member = "predicate"
   )
   Class<? extends RetryPredicate> predicate() default DefaultRetryPredicate.class;
}
