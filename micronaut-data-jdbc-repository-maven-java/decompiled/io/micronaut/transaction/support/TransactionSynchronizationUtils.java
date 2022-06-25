package io.micronaut.transaction.support;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class TransactionSynchronizationUtils {
   private static final Logger LOG = LoggerFactory.getLogger(TransactionSynchronizationUtils.class);

   public static boolean sameResourceFactory(ResourceTransactionManager tm, Object resourceFactory) {
      return unwrapResourceIfNecessary(tm.getResourceFactory()).equals(unwrapResourceIfNecessary(resourceFactory));
   }

   static Object unwrapResourceIfNecessary(Object resource) {
      Objects.requireNonNull(resource, "Resource must not be null");
      Object resourceRef = resource;
      if (resource instanceof InterceptedProxy) {
         resourceRef = ((InterceptedProxy)resource).interceptedTarget();
      }

      return resourceRef;
   }

   public static void triggerFlush(SynchronousTransactionState state) {
      for(TransactionSynchronization synchronization : state.getSynchronizations()) {
         synchronization.flush();
      }

   }

   public static void triggerBeforeCommit(SynchronousTransactionState state, boolean readOnly) {
      for(TransactionSynchronization synchronization : state.getSynchronizations()) {
         synchronization.beforeCommit(readOnly);
      }

   }

   public static void triggerBeforeCompletion(SynchronousTransactionState state) {
      for(TransactionSynchronization synchronization : state.getSynchronizations()) {
         try {
            synchronization.beforeCompletion();
         } catch (Throwable var4) {
            LOG.error("TransactionSynchronization.beforeCompletion threw exception", var4);
         }
      }

   }

   public static void triggerAfterCommit(SynchronousTransactionState state) {
      invokeAfterCommit(state.getSynchronizations());
   }

   public static void invokeAfterCommit(@Nullable List<TransactionSynchronization> synchronizations) {
      if (synchronizations != null) {
         for(TransactionSynchronization synchronization : synchronizations) {
            synchronization.afterCommit();
         }
      }

   }

   public static void triggerAfterCompletion(SynchronousTransactionState state, @NonNull TransactionSynchronization.Status completionStatus) {
      List<TransactionSynchronization> synchronizations = state.getSynchronizations();
      invokeAfterCompletion(synchronizations, completionStatus);
   }

   public static void invokeAfterCompletion(
      @Nullable List<TransactionSynchronization> synchronizations, @NonNull TransactionSynchronization.Status completionStatus
   ) {
      if (synchronizations != null) {
         for(TransactionSynchronization synchronization : synchronizations) {
            try {
               synchronization.afterCompletion(completionStatus);
            } catch (Throwable var5) {
               LOG.error("TransactionSynchronization.afterCompletion threw exception", var5);
            }
         }
      }

   }
}
