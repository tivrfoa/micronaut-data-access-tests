package io.micronaut.transaction.annotation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Internal;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.interceptor.TransactionalInterceptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Around
@Type({TransactionalInterceptor.class})
@Internal
public @interface TransactionalAdvice {
   @AliasFor(
      member = "transactionManager"
   )
   String value() default "";

   @AliasFor(
      member = "value"
   )
   String transactionManager() default "";

   TransactionDefinition.Propagation propagation() default TransactionDefinition.Propagation.REQUIRED;

   TransactionDefinition.Isolation isolation() default TransactionDefinition.Isolation.DEFAULT;

   int timeout() default -1;

   boolean readOnly() default false;

   Class<? extends Throwable>[] noRollbackFor() default {};
}
