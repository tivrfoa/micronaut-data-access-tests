package io.micronaut.transaction.interceptor;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.aop.kotlin.KotlinInterceptedMethod;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import io.micronaut.transaction.exceptions.NoTransactionException;
import io.micronaut.transaction.exceptions.TransactionSystemException;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TransactionalInterceptor implements MethodInterceptor<Object, Object> {
   private static final Logger LOG = LoggerFactory.getLogger(TransactionalInterceptor.class);
   private static final ThreadLocal<TransactionalInterceptor.TransactionInfo> TRANSACTION_INFO_HOLDER = new ThreadLocal<TransactionalInterceptor.TransactionInfo>(
      
   ) {
      public String toString() {
         return "Current aspect-driven transaction";
      }
   };
   private final Map<ExecutableMethod, TransactionalInterceptor.TransactionInvocation> transactionInvocationMap = new ConcurrentHashMap(30);
   @NonNull
   private final BeanLocator beanLocator;
   private final CoroutineTxHelper coroutineTxHelper;

   public TransactionalInterceptor(@NonNull BeanLocator beanLocator) {
      this(beanLocator, null);
   }

   @Inject
   public TransactionalInterceptor(@NonNull BeanLocator beanLocator, @Nullable CoroutineTxHelper coroutineTxHelper) {
      this.beanLocator = beanLocator;
      this.coroutineTxHelper = coroutineTxHelper;
   }

   @Override
   public int getOrder() {
      return InterceptPhase.TRANSACTION.getPosition();
   }

   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      InterceptedMethod interceptedMethod = InterceptedMethod.of(context);
      boolean isKotlinSuspended = interceptedMethod instanceof KotlinInterceptedMethod;

      try {
         boolean isReactive = interceptedMethod.resultType() == InterceptedMethod.ResultType.PUBLISHER;
         boolean isAsync = interceptedMethod.resultType() == InterceptedMethod.ResultType.COMPLETION_STAGE;
         TransactionalInterceptor.TransactionInvocation<?> transactionInvocation = (TransactionalInterceptor.TransactionInvocation)this.transactionInvocationMap
            .computeIfAbsent(
               context.getExecutableMethod(),
               executableMethod -> {
                  String qualifier = (String)executableMethod.stringValue(TransactionalAdvice.class).orElse(null);
                  ReactiveTransactionOperations<?> reactiveTransactionOperations = (ReactiveTransactionOperations)this.beanLocator
                     .findBean(ReactiveTransactionOperations.class, qualifier != null ? Qualifiers.byName(qualifier) : null)
                     .orElse(null);
                  if ((isReactive || isAsync) && (!isKotlinSuspended || reactiveTransactionOperations != null)) {
                     if (isReactive && reactiveTransactionOperations == null) {
                        throw new ConfigurationException(
                           "No reactive transaction management has been configured. Ensure you have correctly configured a reactive capable transaction manager"
                        );
                     } else {
                        TransactionAttribute var9x = this.resolveTransactionDefinition(executableMethod);
                        return new TransactionalInterceptor.TransactionInvocation<>(null, reactiveTransactionOperations, var9x);
                     }
                  } else {
                     SynchronousTransactionManager<?> transactionManagerx = this.beanLocator
                        .getBean(SynchronousTransactionManager.class, qualifier != null ? Qualifiers.byName(qualifier) : null);
                     TransactionAttribute transactionAttribute = this.resolveTransactionDefinition(executableMethod);
                     return new TransactionalInterceptor.TransactionInvocation(transactionManagerx, null, transactionAttribute);
                  }
               }
            );
         TransactionAttribute definition = transactionInvocation.definition;
         switch(interceptedMethod.resultType()) {
            case PUBLISHER:
               return interceptedMethod.handleResult(transactionInvocation.reactiveTransactionOperations.withTransaction(definition, status -> {
                  context.setAttribute("io.micronaut.tx.STATUS", status);
                  context.setAttribute("io.micronaut.tx.ATTRIBUTE", definition);
                  return Publishers.convertPublisher(context.proceed(), Publisher.class);
               }));
            case COMPLETION_STAGE:
               if (transactionInvocation.reactiveTransactionOperations != null) {
                  return interceptedMethod.handleResult(interceptedMethod.interceptResult());
               } else {
                  if (isKotlinSuspended) {
                     return this.interceptKotlinSuspended(context, interceptedMethod, transactionInvocation, definition);
                  }

                  throw new ConfigurationException("Async return type doesn't support transactional execution.");
               }
            case SYNCHRONOUS:
               SynchronousTransactionManager<?> transactionManager = transactionInvocation.transactionManager;
               TransactionalInterceptor.TransactionInfo transactionInfo = this.createTransactionIfNecessary(
                  transactionManager, definition, context.getExecutableMethod()
               );

               Object retVal;
               try {
                  retVal = context.proceed();
               } catch (Throwable var16) {
                  this.completeTransactionAfterThrowing(transactionInfo, var16);
                  throw var16;
               } finally {
                  this.cleanupTransactionInfo(transactionInfo);
               }

               this.commitTransactionAfterReturning(transactionInfo);
               return retVal;
            default:
               return interceptedMethod.unsupported();
         }
      } catch (Exception var18) {
         return interceptedMethod.handleException(var18);
      }
   }

   private Object interceptKotlinSuspended(
      MethodInvocationContext<Object, Object> context,
      InterceptedMethod interceptedMethod,
      TransactionalInterceptor.TransactionInvocation<?> transactionInvocation,
      TransactionAttribute definition
   ) {
      KotlinInterceptedMethod kotlinInterceptedMethod = (KotlinInterceptedMethod)interceptedMethod;
      TransactionSynchronizationManager.TransactionSynchronizationState state = ((CoroutineTxHelper)Objects.requireNonNull(this.coroutineTxHelper))
         .setupTxState(kotlinInterceptedMethod);
      return TransactionSynchronizationManager.withState(
         state,
         () -> {
            SynchronousTransactionManager<?> transactionManager = transactionInvocation.transactionManager;
            TransactionalInterceptor.TransactionInfo<Object> transactionInfo = this.createTransactionIfNecessary(
               transactionManager, definition, context.getExecutableMethod()
            );
   
            CompletionStage<?> result;
            try {
               result = interceptedMethod.interceptResultAsCompletionStage();
            } catch (Exception var11) {
               CompletableFuture<?> r = new CompletableFuture();
               r.completeExceptionally(var11);
               result = r;
            }
   
            CompletableFuture<Object> newResult = new CompletableFuture();
            result.whenComplete((o, throwable) -> TransactionSynchronizationManager.withState(state, () -> {
                  if (throwable == null) {
                     this.commitTransactionAfterReturning(transactionInfo);
                     newResult.complete(o);
                  } else {
                     try {
                        this.completeTransactionAfterThrowing(transactionInfo, throwable);
                     } catch (Exception var6x) {
                     }
   
                     newResult.completeExceptionally(throwable);
                  }
   
                  this.cleanupTransactionInfo(transactionInfo);
                  return null;
               }));
            return interceptedMethod.handleResult(newResult);
         }
      );
   }

   @Nullable
   private static TransactionalInterceptor.TransactionInfo currentTransactionInfo() throws NoTransactionException {
      return (TransactionalInterceptor.TransactionInfo)TRANSACTION_INFO_HOLDER.get();
   }

   public static <T> TransactionStatus<T> currentTransactionStatus() throws NoTransactionException {
      TransactionalInterceptor.TransactionInfo info = currentTransactionInfo();
      if (info == null) {
         throw new NoTransactionException("No transaction aspect-managed TransactionStatus in scope");
      } else {
         return info.transactionStatus;
      }
   }

   @Deprecated
   protected TransactionalInterceptor.TransactionInfo createTransactionIfNecessary(
      @NonNull SynchronousTransactionManager<?> tm, @NonNull TransactionAttribute txAttr, final ExecutableMethod<Object, Object> executableMethod
   ) {
      TransactionStatus<?> status = tm.getTransaction(txAttr);
      return this.prepareTransactionInfo(tm, txAttr, executableMethod, status);
   }

   @Deprecated
   protected TransactionalInterceptor.TransactionInfo prepareTransactionInfo(
      @NonNull SynchronousTransactionManager tm,
      @NonNull TransactionAttribute txAttr,
      ExecutableMethod<Object, Object> executableMethod,
      @NonNull TransactionStatus status
   ) {
      TransactionalInterceptor.TransactionInfo txInfo = new TransactionalInterceptor.TransactionInfo(tm, txAttr, executableMethod);
      if (LOG.isTraceEnabled()) {
         LOG.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
      }

      txInfo.newTransactionStatus(status);
      txInfo.bindToThread();
      return txInfo;
   }

   @Deprecated
   protected void commitTransactionAfterReturning(@NonNull TransactionalInterceptor.TransactionInfo txInfo) {
      if (LOG.isTraceEnabled()) {
         LOG.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
      }

      txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
   }

   @Deprecated
   protected void completeTransactionAfterThrowing(@NonNull TransactionalInterceptor.TransactionInfo txInfo, Throwable ex) {
      if (LOG.isTraceEnabled()) {
         LOG.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "] after exception: " + ex);
      }

      if (txInfo.transactionAttribute.rollbackOn(ex)) {
         try {
            txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
         } catch (TransactionSystemException var6) {
            LOG.error("Application exception overridden by rollback exception", ex);
            var6.initApplicationException(ex);
            throw var6;
         } catch (Error | RuntimeException var7) {
            LOG.error("Application exception overridden by rollback exception", ex);
            throw var7;
         }
      } else {
         try {
            txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
         } catch (TransactionSystemException var4) {
            LOG.error("Application exception overridden by commit exception", ex);
            var4.initApplicationException(ex);
            throw var4;
         } catch (Error | RuntimeException var5) {
            LOG.error("Application exception overridden by commit exception", ex);
            throw var5;
         }
      }

   }

   @Deprecated
   protected void cleanupTransactionInfo(@Nullable TransactionalInterceptor.TransactionInfo txInfo) {
      if (txInfo != null) {
         txInfo.restoreThreadLocalStatus();
      }

   }

   @Deprecated
   protected TransactionAttribute resolveTransactionDefinition(ExecutableMethod<Object, Object> executableMethod) {
      AnnotationValue<TransactionalAdvice> annotation = executableMethod.getAnnotation(TransactionalAdvice.class);
      if (annotation == null) {
         throw new IllegalStateException("No declared @Transactional annotation present");
      } else {
         DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
         attribute.setName(executableMethod.getDeclaringType().getSimpleName() + "." + executableMethod.getMethodName());
         attribute.setReadOnly(annotation.isTrue("readOnly"));
         annotation.intValue("timeout").ifPresent(value -> attribute.setTimeout(Duration.ofSeconds((long)value)));
         Class[] noRollbackFors = annotation.classValues("noRollbackFor");
         attribute.setNoRollbackFor(noRollbackFors);
         annotation.enumValue("propagation", TransactionDefinition.Propagation.class).ifPresent(attribute::setPropagationBehavior);
         annotation.enumValue("isolation", TransactionDefinition.Isolation.class).ifPresent(attribute::setIsolationLevel);
         return attribute;
      }
   }

   @Deprecated
   protected static final class TransactionInfo<T> {
      private final SynchronousTransactionManager<T> transactionManager;
      private final TransactionAttribute transactionAttribute;
      private final ExecutableMethod<Object, Object> executableMethod;
      private TransactionStatus<T> transactionStatus;
      private TransactionalInterceptor.TransactionInfo<T> oldTransactionInfo;

      protected TransactionInfo(
         @NonNull SynchronousTransactionManager<T> transactionManager,
         @NonNull TransactionAttribute transactionAttribute,
         @NonNull ExecutableMethod<Object, Object> executableMethod
      ) {
         this.transactionManager = transactionManager;
         this.transactionAttribute = transactionAttribute;
         this.executableMethod = executableMethod;
      }

      @NonNull
      public SynchronousTransactionManager<T> getTransactionManager() {
         return this.transactionManager;
      }

      @NonNull
      public String getJoinpointIdentification() {
         return this.executableMethod.getDeclaringType().getName() + " . " + this.executableMethod.toString();
      }

      public void newTransactionStatus(@NonNull TransactionStatus<T> status) {
         this.transactionStatus = status;
      }

      @NonNull
      public TransactionStatus<T> getTransactionStatus() {
         if (this.transactionStatus == null) {
            throw new IllegalStateException("Transaction status not yet initialized");
         } else {
            return this.transactionStatus;
         }
      }

      public boolean hasTransaction() {
         return true;
      }

      private void bindToThread() {
         this.oldTransactionInfo = (TransactionalInterceptor.TransactionInfo)TransactionalInterceptor.TRANSACTION_INFO_HOLDER.get();
         TransactionalInterceptor.TRANSACTION_INFO_HOLDER.set(this);
      }

      private void restoreThreadLocalStatus() {
         TransactionalInterceptor.TRANSACTION_INFO_HOLDER.set(this.oldTransactionInfo);
      }

      public String toString() {
         return this.transactionAttribute.toString();
      }
   }

   private static final class TransactionInvocation<C> {
      @Nullable
      final SynchronousTransactionManager<C> transactionManager;
      @Nullable
      final ReactiveTransactionOperations<C> reactiveTransactionOperations;
      final TransactionAttribute definition;

      TransactionInvocation(
         SynchronousTransactionManager<C> transactionManager, ReactiveTransactionOperations<C> reactiveTransactionOperations, TransactionAttribute definition
      ) {
         this.transactionManager = transactionManager;
         this.reactiveTransactionOperations = reactiveTransactionOperations;
         this.definition = definition;
      }

      boolean isReactive() {
         return this.reactiveTransactionOperations != null;
      }
   }
}
