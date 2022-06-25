package io.micronaut.aop.chain;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.InterceptorRegistry;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.aop.exceptions.UnimplementedAdviceException;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

@Internal
public final class MethodInterceptorChain<T, R> extends InterceptorChain<T, R> implements MethodInvocationContext<T, R> {
   private static final Object[] EMPTY_ARRAY = new Object[0];
   @Nullable
   private final InterceptorKind kind;

   public MethodInterceptorChain(Interceptor<T, R>[] interceptors, T target, ExecutableMethod<T, R> executionHandle) {
      this(interceptors, target, executionHandle, (InterceptorKind)null);
   }

   public MethodInterceptorChain(Interceptor<T, R>[] interceptors, T target, ExecutableMethod<T, R> executionHandle, @Nullable InterceptorKind kind) {
      super(interceptors, target, executionHandle, EMPTY_ARRAY);
      this.kind = kind;
   }

   public MethodInterceptorChain(Interceptor<T, R>[] interceptors, T target, ExecutableMethod<T, R> executionHandle, Object... originalParameters) {
      super(interceptors, target, executionHandle, originalParameters);
      this.kind = null;
   }

   @NonNull
   @Override
   public InterceptorKind getKind() {
      return this.kind != null ? this.kind : (this.target instanceof Introduced ? InterceptorKind.INTRODUCTION : InterceptorKind.AROUND);
   }

   @Override
   public R invoke(T instance, Object... arguments) {
      return (R)new MethodInterceptorChain<>(this.interceptors, instance, this.executionHandle, this.originalParameters).proceed();
   }

   @Override
   public boolean isSuspend() {
      return this.executionHandle.isSuspend();
   }

   @Override
   public boolean isAbstract() {
      return this.executionHandle.isAbstract();
   }

   @Override
   public R proceed() throws RuntimeException {
      if (this.interceptorCount != 0 && this.index != this.interceptorCount) {
         Interceptor<T, R> interceptor = this.interceptors[this.index++];
         if (LOG.isTraceEnabled()) {
            LOG.trace("Proceeded to next interceptor [{}] in chain for method invocation: {}", interceptor, this.executionHandle);
         }

         return (R)(interceptor instanceof MethodInterceptor ? ((MethodInterceptor)interceptor).intercept(this) : interceptor.intercept(this));
      } else if (this.target instanceof Introduced && this.executionHandle.isAbstract()) {
         throw new UnimplementedAdviceException(this.executionHandle);
      } else {
         return this.executionHandle.invoke(this.target, this.getParameterValues());
      }
   }

   @Override
   public String getMethodName() {
      return this.executionHandle.getMethodName();
   }

   @Override
   public Class[] getArgumentTypes() {
      return this.executionHandle.getArgumentTypes();
   }

   @Override
   public Method getTargetMethod() {
      return this.executionHandle.getTargetMethod();
   }

   @Override
   public ReturnType<R> getReturnType() {
      return this.executionHandle.getReturnType();
   }

   @Override
   public Class<T> getDeclaringType() {
      return this.executionHandle.getDeclaringType();
   }

   public String toString() {
      return this.executionHandle.toString();
   }

   @NonNull
   @Override
   public ExecutableMethod<T, R> getExecutableMethod() {
      return this.executionHandle;
   }

   @Internal
   @NonNull
   public static <T1> T1 initialize(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext beanContext,
      @NonNull BeanDefinition<T1> definition,
      @NonNull ExecutableMethod<T1, T1> postConstructMethod,
      @NonNull T1 bean
   ) {
      return doIntercept(resolutionContext, beanContext, definition, postConstructMethod, bean, InterceptorKind.POST_CONSTRUCT);
   }

   @Internal
   @NonNull
   public static <T1> T1 dispose(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext beanContext,
      @NonNull BeanDefinition<T1> definition,
      @NonNull ExecutableMethod<T1, T1> preDestroyMethod,
      @NonNull T1 bean
   ) {
      return doIntercept(resolutionContext, beanContext, definition, preDestroyMethod, bean, InterceptorKind.PRE_DESTROY);
   }

   private static <T1> T1 doIntercept(
      BeanResolutionContext resolutionContext,
      BeanContext beanContext,
      BeanDefinition<T1> definition,
      ExecutableMethod<T1, T1> interceptedMethod,
      T1 bean,
      InterceptorKind kind
   ) {
      AnnotationMetadata annotationMetadata = interceptedMethod.getAnnotationMetadata();
      Collection<AnnotationValue<?>> binding = resolveInterceptorValues(annotationMetadata, kind);
      Collection<BeanRegistration<Interceptor<?, ?>>> resolved = ((DefaultBeanContext)beanContext)
         .getBeanRegistrations(resolutionContext, Interceptor.ARGUMENT, Qualifiers.byInterceptorBindingValues(binding));
      InterceptorRegistry interceptorRegistry = beanContext.getBean(InterceptorRegistry.ARGUMENT);
      Interceptor[] resolvedInterceptors = interceptorRegistry.resolveInterceptors(interceptedMethod, resolved, kind);
      if (ArrayUtils.isNotEmpty(resolvedInterceptors)) {
         MethodInterceptorChain<T1, T1> chain = new MethodInterceptorChain<>(resolvedInterceptors, bean, interceptedMethod, kind);
         return (T1)Objects.requireNonNull(chain.proceed(), kind.name() + " interceptor chain illegal returned null for type: " + definition.getBeanType());
      } else {
         return interceptedMethod.invoke(bean, new Object[0]);
      }
   }
}
