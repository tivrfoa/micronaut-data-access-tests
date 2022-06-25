package io.micronaut.data.intercept;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.aop.kotlin.KotlinInterceptedMethod;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.Qualifier;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.exceptions.EmptyResultException;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.operations.PrimaryRepositoryOperations;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.transaction.interceptor.CoroutineTxHelper;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Singleton
@Internal
public final class DataIntroductionAdvice implements MethodInterceptor<Object, Object> {
   private final BeanLocator beanLocator;
   private final Map<RepositoryMethodKey, DataInterceptor> interceptorMap = new ConcurrentHashMap(20);
   private final CoroutineTxHelper coroutineTxHelper;

   @Inject
   public DataIntroductionAdvice(@NonNull BeanLocator beanLocator, @Nullable CoroutineTxHelper coroutineTxHelper) {
      this.beanLocator = beanLocator;
      this.coroutineTxHelper = coroutineTxHelper;
   }

   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      RepositoryMethodKey key = new RepositoryMethodKey(context.getTarget(), context.getExecutableMethod());
      DataInterceptor<Object, Object> dataInterceptor = (DataInterceptor)this.interceptorMap.get(key);
      if (dataInterceptor != null) {
         return this.intercept(context, dataInterceptor, key);
      } else {
         String dataSourceName = (String)context.stringValue(Repository.class).orElse(null);
         Class<?> operationsType = (Class)context.classValue(RepositoryConfiguration.class, "operations").orElse(PrimaryRepositoryOperations.class);
         Class<?> interceptorType = (Class)context.classValue(DataMethod.class, "interceptor").orElse(null);
         if (interceptorType != null && DataInterceptor.class.isAssignableFrom(interceptorType)) {
            DataInterceptor<Object, Object> childInterceptor = this.findInterceptor(dataSourceName, operationsType, interceptorType);
            this.interceptorMap.put(key, childInterceptor);
            return this.intercept(context, childInterceptor, key);
         } else {
            AnnotationValue<DataMethod> declaredAnnotation = context.getDeclaredAnnotation(DataMethod.class);
            if (declaredAnnotation != null) {
               interceptorType = (Class)declaredAnnotation.classValue("interceptor").orElse(null);
               if (interceptorType != null && DataInterceptor.class.isAssignableFrom(interceptorType)) {
                  DataInterceptor<Object, Object> childInterceptor = this.findInterceptor(dataSourceName, operationsType, interceptorType);
                  this.interceptorMap.put(key, childInterceptor);
                  return this.intercept(context, childInterceptor, key);
               }
            }

            String interceptorName = (String)context.getAnnotationMetadata().stringValue(DataMethod.class, "interceptor").orElse(null);
            if (interceptorName != null) {
               throw new IllegalStateException(
                  "Micronaut Data Interceptor ["
                     + interceptorName
                     + "] is not on the classpath but required by the method: "
                     + context.getExecutableMethod().toString()
               );
            } else {
               throw new IllegalStateException(
                  "Micronaut Data method is missing compilation time query information. Ensure that the Micronaut Data annotation processors are declared in your build and try again with a clean re-build."
               );
            }
         }
      }
   }

   private Object intercept(MethodInvocationContext<Object, Object> context, DataInterceptor<Object, Object> dataInterceptor, RepositoryMethodKey key) {
      InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

      try {
         switch(interceptedMethod.resultType()) {
            case PUBLISHER:
               return interceptedMethod.handleResult(dataInterceptor.intercept(key, context));
            case COMPLETION_STAGE:
               boolean isKotlinSuspended = interceptedMethod instanceof KotlinInterceptedMethod;
               if (isKotlinSuspended) {
                  return this.interceptKotlinSuspend(context, dataInterceptor, key, interceptedMethod);
               }

               return interceptedMethod.handleResult(dataInterceptor.intercept(key, context));
            case SYNCHRONOUS:
               return dataInterceptor.intercept(key, context);
            default:
               return interceptedMethod.unsupported();
         }
      } catch (Exception var6) {
         return interceptedMethod.handleException(var6);
      }
   }

   private Object interceptKotlinSuspend(
      MethodInvocationContext<Object, Object> context,
      DataInterceptor<Object, Object> dataInterceptor,
      RepositoryMethodKey key,
      InterceptedMethod interceptedMethod
   ) {
      TransactionSynchronizationManager.TransactionSynchronizationState state = ((CoroutineTxHelper)Objects.requireNonNull(this.coroutineTxHelper))
         .setupTxState((KotlinInterceptedMethod)interceptedMethod);
      CompletionStage<Object> completionStage;
      if (state == null) {
         completionStage = (CompletionStage)dataInterceptor.intercept(key, context);
      } else {
         completionStage = TransactionSynchronizationManager.withState(state, () -> (CompletionStage)dataInterceptor.intercept(key, context));
      }

      CompletableFuture<Object> completableFuture = new CompletableFuture();
      interceptedMethod.handleResult(completableFuture);
      completionStage.whenComplete((value, throwable) -> TransactionSynchronizationManager.withState(state, () -> {
            if (throwable == null) {
               completableFuture.complete(value);
            } else {
               Throwable finalThrowable = throwable;
               if (throwable instanceof CompletionException) {
                  finalThrowable = throwable.getCause();
               }

               if (finalThrowable instanceof EmptyResultException && context.isSuspend() && context.isNullable()) {
                  completableFuture.complete(null);
               } else {
                  completableFuture.completeExceptionally(finalThrowable);
               }
            }

            return null;
         }));
      return KotlinUtils.COROUTINE_SUSPENDED;
   }

   @NonNull
   private DataInterceptor<Object, Object> findInterceptor(@Nullable String dataSourceName, @NonNull Class<?> operationsType, @NonNull Class<?> interceptorType) {
      if (!RepositoryOperations.class.isAssignableFrom(operationsType)) {
         throw new IllegalArgumentException("Repository type must be an instance of RepositoryOperations!");
      } else {
         RepositoryOperations datastore;
         try {
            if (dataSourceName != null) {
               Qualifier qualifier = Qualifiers.byName(dataSourceName);
               datastore = this.beanLocator.getBean(operationsType, qualifier);
            } else {
               datastore = this.beanLocator.getBean(operationsType);
            }
         } catch (NoSuchBeanException var7) {
            throw new ConfigurationException("No backing RepositoryOperations configured for repository. Check your configuration and try again", var7);
         }

         BeanIntrospection<Object> introspection = (BeanIntrospection)BeanIntrospector.SHARED
            .findIntrospections(
               (Predicate<? super BeanIntrospectionReference<?>>)(ref -> ref.isPresent() && interceptorType.isAssignableFrom(ref.getBeanType()))
            )
            .stream()
            .findFirst()
            .orElseThrow(() -> new DataAccessException("No Data interceptor found for type: " + interceptorType));
         DataInterceptor interceptor;
         if (introspection.getConstructorArguments().length == 0) {
            interceptor = (DataInterceptor)introspection.instantiate();
         } else {
            interceptor = (DataInterceptor)introspection.instantiate(datastore);
         }

         return interceptor;
      }
   }
}
