package io.micronaut.transaction.interceptor;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.annotation.TransactionalEventListener;
import io.micronaut.transaction.support.TransactionSynchronization;
import io.micronaut.transaction.support.TransactionSynchronizationAdapter;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TransactionalEventInterceptor implements MethodInterceptor<Object, Object> {
   private static final Logger LOG = LoggerFactory.getLogger(TransactionalEventListener.class);

   @Override
   public int getOrder() {
      return InterceptPhase.TRANSACTION.getPosition() - 10;
   }

   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      final TransactionalEventListener.TransactionPhase phase = (TransactionalEventListener.TransactionPhase)context.enumValue(
            TransactionalEventListener.class, TransactionalEventListener.TransactionPhase.class
         )
         .orElse(TransactionalEventListener.TransactionPhase.AFTER_COMMIT);
      if (TransactionSynchronizationManager.isSynchronizationActive() && TransactionSynchronizationManager.isActualTransactionActive()) {
         TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
               if (phase == TransactionalEventListener.TransactionPhase.BEFORE_COMMIT) {
                  context.proceed();
               }

            }

            @Override
            public void afterCompletion(@NonNull TransactionSynchronization.Status status) {
               switch(status) {
                  case ROLLED_BACK:
                     if (phase == TransactionalEventListener.TransactionPhase.AFTER_ROLLBACK) {
                        context.proceed();
                     }
                     break;
                  case COMMITTED:
                     if (phase == TransactionalEventListener.TransactionPhase.AFTER_COMMIT) {
                        context.proceed();
                     }
                     break;
                  default:
                     if (phase == TransactionalEventListener.TransactionPhase.AFTER_COMPLETION) {
                        context.proceed();
                     }
               }

            }
         });
      } else if (LOG.isDebugEnabled()) {
         LOG.debug("No active transaction, skipping event {}", context.getParameterValues()[0]);
      }

      return null;
   }
}
