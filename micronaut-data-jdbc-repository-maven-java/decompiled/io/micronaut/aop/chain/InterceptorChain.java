package io.micronaut.aop.chain;

import io.micronaut.aop.Adapter;
import io.micronaut.aop.Around;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.InterceptorRegistry;
import io.micronaut.aop.Introduction;
import io.micronaut.aop.InvocationContext;
import io.micronaut.aop.exceptions.UnimplementedAdviceException;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.EnvironmentConfigurable;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.ExecutableMethod;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Internal
public class InterceptorChain<B, R> extends AbstractInterceptorChain<B, R> implements InvocationContext<B, R> {
   protected final B target;
   protected final ExecutableMethod<B, R> executionHandle;

   public InterceptorChain(Interceptor<B, R>[] interceptors, B target, ExecutableMethod<B, R> method, Object... originalParameters) {
      super(interceptors, originalParameters);
      if (LOG.isTraceEnabled()) {
         LOG.trace("Intercepted method [{}] invocation on target: {}", method, target);
      }

      this.target = target;
      this.executionHandle = method;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.executionHandle.getAnnotationMetadata();
   }

   @Override
   public Argument[] getArguments() {
      return this.executionHandle.getArguments();
   }

   @Override
   public R invoke(B instance, Object... arguments) {
      return this.proceed();
   }

   @Override
   public B getTarget() {
      return this.target;
   }

   @Override
   public R proceed() throws RuntimeException {
      if (this.interceptorCount != 0 && this.index != this.interceptorCount) {
         Interceptor<B, R> interceptor = this.interceptors[this.index++];
         if (LOG.isTraceEnabled()) {
            LOG.trace("Proceeded to next interceptor [{}] in chain for method invocation: {}", interceptor, this.executionHandle);
         }

         return interceptor.intercept(this);
      } else {
         try {
            return this.executionHandle.invoke(this.target, this.getParameterValues());
         } catch (AbstractMethodError var3) {
            throw new UnimplementedAdviceException(this.executionHandle);
         }
      }
   }

   @Internal
   public static Interceptor[] resolveAroundInterceptors(
      @Nullable BeanContext beanContext, ExecutableMethod<?, ?> method, List<BeanRegistration<Interceptor<?, ?>>> interceptors
   ) {
      return resolveInterceptors(beanContext, method, interceptors, InterceptorKind.AROUND);
   }

   @Internal
   public static Interceptor[] resolveIntroductionInterceptors(
      @Nullable BeanContext beanContext, ExecutableMethod<?, ?> method, List<BeanRegistration<Interceptor<?, ?>>> interceptors
   ) {
      Interceptor[] introductionInterceptors = resolveInterceptors(beanContext, method, interceptors, InterceptorKind.INTRODUCTION);
      Interceptor[] aroundInterceptors = resolveInterceptors(beanContext, method, interceptors, InterceptorKind.AROUND);
      return ArrayUtils.concat(aroundInterceptors, introductionInterceptors);
   }

   @Internal
   @Deprecated
   public static Interceptor[] resolveAroundInterceptors(@Nullable BeanContext beanContext, ExecutableMethod<?, ?> method, Interceptor... interceptors) {
      instrumentAnnotationMetadata(beanContext, method);
      return resolveInterceptorsInternal(
         method, Around.class, interceptors, beanContext != null ? beanContext.getClassLoader() : InterceptorChain.class.getClassLoader()
      );
   }

   @Internal
   @Deprecated
   public static Interceptor[] resolveIntroductionInterceptors(@Nullable BeanContext beanContext, ExecutableMethod<?, ?> method, Interceptor... interceptors) {
      instrumentAnnotationMetadata(beanContext, method);
      Interceptor[] introductionInterceptors = resolveInterceptorsInternal(
         method, Introduction.class, interceptors, beanContext != null ? beanContext.getClassLoader() : InterceptorChain.class.getClassLoader()
      );
      if (introductionInterceptors.length == 0) {
         if (!method.hasStereotype(Adapter.class)) {
            throw new IllegalStateException(
               "At least one @Introduction method interceptor required, but missing. Check if your @Introduction stereotype annotation is marked with @Retention(RUNTIME) and @Type(..) with the interceptor type. Otherwise do not load @Introduction beans if their interceptor definitions are missing!"
            );
         }

         introductionInterceptors = new Interceptor[]{new AdapterIntroduction(beanContext, method)};
      }

      Interceptor[] aroundInterceptors = resolveAroundInterceptors(beanContext, method, interceptors);
      return ArrayUtils.concat(aroundInterceptors, introductionInterceptors);
   }

   @NonNull
   private static Interceptor[] resolveInterceptors(
      BeanContext beanContext, ExecutableMethod<?, ?> method, List<BeanRegistration<Interceptor<?, ?>>> interceptors, InterceptorKind interceptorKind
   ) {
      return beanContext.<InterceptorRegistry>getBean(InterceptorRegistry.class).resolveInterceptors(method, interceptors, interceptorKind);
   }

   private static void instrumentAnnotationMetadata(BeanContext beanContext, ExecutableMethod<?, ?> method) {
      if (beanContext instanceof ApplicationContext && method instanceof EnvironmentConfigurable) {
         EnvironmentConfigurable m = (EnvironmentConfigurable)method;
         if (m.hasPropertyExpressions()) {
            m.configure(((ApplicationContext)beanContext).getEnvironment());
         }
      }

   }

   private static Interceptor[] resolveInterceptorsInternal(
      ExecutableMethod<?, ?> method, Class<? extends Annotation> annotationType, Interceptor[] interceptors, @NonNull ClassLoader classLoader
   ) {
      List<Class<? extends Annotation>> annotations = method.getAnnotationTypesByStereotype(annotationType, classLoader);
      Set<Class> applicableClasses = new HashSet();

      for(Class<? extends Annotation> aClass : annotations) {
         if ((annotationType != Around.class || aClass.getAnnotation(Around.class) != null || aClass.getAnnotation(Introduction.class) == null)
            && (annotationType != Introduction.class || aClass.getAnnotation(Introduction.class) != null || aClass.getAnnotation(Around.class) == null)) {
            Type typeAnn = (Type)aClass.getAnnotation(Type.class);
            if (typeAnn != null) {
               applicableClasses.addAll(Arrays.asList(typeAnn.value()));
            }
         }
      }

      Interceptor[] interceptorArray = (Interceptor[])Arrays.stream(interceptors)
         .filter(i -> applicableClasses.stream().anyMatch(t -> t.isInstance(i)))
         .toArray(x$0 -> new Interceptor[x$0]);
      OrderUtil.sort((Ordered[])interceptorArray);
      return interceptorArray;
   }
}
