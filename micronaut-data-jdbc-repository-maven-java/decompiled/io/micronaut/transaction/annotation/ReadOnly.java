package io.micronaut.transaction.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.transaction.TransactionDefinition;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TransactionalAdvice(
   readOnly = true
)
public @interface ReadOnly {
   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "value"
   )
   String value() default "";

   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "value"
   )
   String transactionManager() default "";

   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "propagation"
   )
   TransactionDefinition.Propagation propagation() default TransactionDefinition.Propagation.REQUIRED;

   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "isolation"
   )
   TransactionDefinition.Isolation isolation() default TransactionDefinition.Isolation.DEFAULT;

   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "timeout"
   )
   int timeout() default -1;

   @AliasFor(
      annotation = TransactionalAdvice.class,
      member = "noRollbackFor"
   )
   Class<? extends Throwable>[] noRollbackFor() default {};
}
