package io.micronaut.transaction.annotation;

import io.micronaut.aop.Adapter;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.transaction.interceptor.annotation.TransactionalEventAdvice;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Adapter(ApplicationEventListener.class)
@Indexed(ApplicationEventListener.class)
@TransactionalEventAdvice
public @interface TransactionalEventListener {
   TransactionalEventListener.TransactionPhase value() default TransactionalEventListener.TransactionPhase.AFTER_COMMIT;

   public static enum TransactionPhase {
      BEFORE_COMMIT,
      AFTER_COMMIT,
      AFTER_ROLLBACK,
      AFTER_COMPLETION;
   }
}
