package io.micronaut.aop.chain;

import io.micronaut.aop.ConstructorInvocationContext;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.InterceptorRegistry;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Internal
public final class ConstructorInterceptorChain<T> extends AbstractInterceptorChain<T, T> implements ConstructorInvocationContext<T> {
   private final BeanConstructor<T> beanConstructor;
   private Object[] internalParameters = ArrayUtils.EMPTY_OBJECT_ARRAY;

   private ConstructorInterceptorChain(@NonNull BeanConstructor<T> beanConstructor, @NonNull Interceptor<T, T>[] interceptors, Object... originalParameters) {
      super(interceptors, originalParameters);
      this.beanConstructor = (BeanConstructor)Objects.requireNonNull(beanConstructor, "Bean constructor cannot be null");
   }

   private ConstructorInterceptorChain(
      @NonNull BeanDefinition<T> beanDefinition,
      @NonNull BeanConstructor<T> beanConstructor,
      @NonNull Interceptor<T, T>[] interceptors,
      int additionalInterceptorParametersCount,
      Object... originalParameters
   ) {
      this(beanConstructor, interceptors, resolveConcreteSubset(beanDefinition, originalParameters, additionalInterceptorParametersCount));
      this.internalParameters = resolveInterceptorArguments(beanDefinition, originalParameters, additionalInterceptorParametersCount);
   }

   @NonNull
   @Override
   public InterceptorKind getKind() {
      return InterceptorKind.AROUND_CONSTRUCT;
   }

   @Override
   public T getTarget() {
      throw new UnsupportedOperationException("The target cannot be retrieved for Constructor interception");
   }

   @Override
   public T proceed() throws RuntimeException {
      if (this.interceptorCount != 0 && this.index != this.interceptorCount) {
         Interceptor<T, T> interceptor = this.interceptors[this.index++];
         if (LOG.isTraceEnabled()) {
            LOG.trace("Proceeded to next interceptor [{}] in chain for constructor invocation: {}", interceptor, this.beanConstructor);
         }

         return interceptor.intercept(this);
      } else {
         Object[] finalParameters;
         if (ArrayUtils.isNotEmpty(this.internalParameters)) {
            finalParameters = ArrayUtils.concat(this.getParameterValues(), this.internalParameters);
         } else {
            finalParameters = this.getParameterValues();
         }

         return this.beanConstructor.instantiate(finalParameters);
      }
   }

   @NonNull
   @Override
   public Argument<?>[] getArguments() {
      return this.beanConstructor.getArguments();
   }

   @Override
   public T invoke(T instance, Object... arguments) {
      throw new UnsupportedOperationException("Existing instances cannot be invoked with Constructor injection");
   }

   @NonNull
   @Override
   public BeanConstructor<T> getConstructor() {
      return this.beanConstructor;
   }

   @Internal
   @NonNull
   @Deprecated
   public static <T1> T1 instantiate(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext beanContext,
      @Nullable List<BeanRegistration<Interceptor<T1, T1>>> interceptors,
      @NonNull BeanDefinition<T1> definition,
      @NonNull BeanConstructor<T1> constructor,
      @NonNull Object... parameters
   ) {
      int micronaut3additionalProxyConstructorParametersCount = 3;
      return instantiate(resolutionContext, beanContext, interceptors, definition, constructor, micronaut3additionalProxyConstructorParametersCount, parameters);
   }

   @Internal
   @NonNull
   public static <T1> T1 instantiate(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext beanContext,
      @Nullable List<BeanRegistration<Interceptor<T1, T1>>> interceptors,
      @NonNull BeanDefinition<T1> definition,
      @NonNull BeanConstructor<T1> constructor,
      int additionalProxyConstructorParametersCount,
      @NonNull Object... parameters
   ) {
      if (interceptors == null) {
         AnnotationMetadataHierarchy hierarchy = new AnnotationMetadataHierarchy(definition.getAnnotationMetadata(), constructor.getAnnotationMetadata());
         Collection<AnnotationValue<?>> annotationValues = resolveInterceptorValues(hierarchy, InterceptorKind.AROUND_CONSTRUCT);
         Collection<BeanRegistration<Interceptor<?, ?>>> resolved = ((DefaultBeanContext)beanContext)
            .getBeanRegistrations(resolutionContext, Interceptor.ARGUMENT, Qualifiers.byInterceptorBindingValues(annotationValues));
         interceptors = new ArrayList(resolved);
      }

      InterceptorRegistry interceptorRegistry = beanContext.getBean(InterceptorRegistry.ARGUMENT);
      Interceptor<T1, T1>[] resolvedInterceptors = interceptorRegistry.resolveConstructorInterceptors(constructor, interceptors);
      return (T1)Objects.requireNonNull(
         new ConstructorInterceptorChain<>(definition, constructor, resolvedInterceptors, additionalProxyConstructorParametersCount, parameters).proceed(),
         "Constructor interceptor chain illegally returned null for constructor: " + constructor.getDescription()
      );
   }

   private static Object[] resolveConcreteSubset(BeanDefinition<?> beanDefinition, Object[] originalParameters, int additionalProxyConstructorParametersCount) {
      if (beanDefinition instanceof AdvisedBeanType) {
         if (originalParameters.length < additionalProxyConstructorParametersCount) {
            throw new IllegalStateException("Invalid intercepted bean constructor. This should never happen. Report an issue to the project maintainers.");
         } else {
            return Arrays.copyOfRange(originalParameters, 0, originalParameters.length - additionalProxyConstructorParametersCount);
         }
      } else {
         return originalParameters;
      }
   }

   private static Object[] resolveInterceptorArguments(
      BeanDefinition<?> beanDefinition, Object[] originalParameters, int additionalProxyConstructorParametersCount
   ) {
      if (beanDefinition instanceof AdvisedBeanType) {
         if (originalParameters.length < additionalProxyConstructorParametersCount) {
            throw new IllegalStateException("Invalid intercepted bean constructor. This should never happen. Report an issue to the project maintainers.");
         } else {
            return Arrays.copyOfRange(originalParameters, originalParameters.length - additionalProxyConstructorParametersCount, originalParameters.length);
         }
      } else {
         return originalParameters;
      }
   }
}
