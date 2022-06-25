package io.micronaut.retry.annotation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Type;
import io.micronaut.retry.intercept.DefaultRetryInterceptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.constraints.Digits;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Around
@Type({DefaultRetryInterceptor.class})
public @interface Retryable {
   int MAX_INTEGRAL_DIGITS = 4;

   Class<? extends Throwable>[] value() default {};

   @AliasFor(
      member = "value"
   )
   Class<? extends Throwable>[] includes() default {};

   Class<? extends Throwable>[] excludes() default {};

   @Digits(
      integer = 4,
      fraction = 0
   )
   String attempts() default "3";

   String delay() default "1s";

   String maxDelay() default "";

   @Digits(
      integer = 2,
      fraction = 2
   )
   String multiplier() default "1.0";

   Class<? extends RetryPredicate> predicate() default DefaultRetryPredicate.class;

   Class<? extends Throwable> capturedException() default RuntimeException.class;
}
