package io.micronaut.aop.chain;

import io.micronaut.aop.Adapter;
import io.micronaut.aop.ConstructorInterceptor;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.InterceptorRegistry;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.EnvironmentConfigurable;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.naming.Described;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.inject.ExecutableMethod;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultInterceptorRegistry implements InterceptorRegistry {
   protected static final Logger LOG = LoggerFactory.getLogger(InterceptorChain.class);
   private static final MethodInterceptor<?, ?>[] ZERO_METHOD_INTERCEPTORS = new MethodInterceptor[0];
   private final BeanContext beanContext;

   public DefaultInterceptorRegistry(BeanContext beanContext) {
      this.beanContext = beanContext;
   }

   @NonNull
   @Override
   public <T> Interceptor<T, ?>[] resolveInterceptors(
      @NonNull Executable<T, ?> method, @NonNull Collection<BeanRegistration<Interceptor<T, ?>>> interceptors, @NonNull InterceptorKind interceptorKind
   ) {
      AnnotationMetadata annotationMetadata = method.getAnnotationMetadata();
      if (interceptors.isEmpty()) {
         return this.resolveToNone((ExecutableMethod<?, ?>)method, interceptorKind, annotationMetadata);
      } else {
         instrumentAnnotationMetadata(this.beanContext, method);
         Collection<AnnotationValue<?>> applicableBindings = AbstractInterceptorChain.resolveInterceptorValues(annotationMetadata, interceptorKind);
         if (applicableBindings.isEmpty()) {
            return this.resolveToNone((ExecutableMethod<?, ?>)method, interceptorKind, annotationMetadata);
         } else {
            Interceptor[] resolvedInterceptors = (Interceptor[])this.interceptorStream(
                  method.getDeclaringType(), interceptors, interceptorKind, applicableBindings
               )
               .filter(bean -> bean instanceof MethodInterceptor || !(bean instanceof ConstructorInterceptor))
               .toArray(x$0 -> new Interceptor[x$0]);
            if (LOG.isTraceEnabled()) {
               LOG.trace(
                  "Resolved {} {} interceptors out of a possible {} for method: {} - {}",
                  resolvedInterceptors.length,
                  interceptorKind,
                  interceptors.size(),
                  method.getDeclaringType(),
                  method instanceof Described ? ((Described)method).getDescription(true) : method.toString()
               );

               for(int i = 0; i < resolvedInterceptors.length; ++i) {
                  Interceptor<?, ?> resolvedInterceptor = resolvedInterceptors[i];
                  LOG.trace("Interceptor {} - {}", i, resolvedInterceptor);
               }
            }

            return resolvedInterceptors;
         }
      }
   }

   private Interceptor[] resolveToNone(ExecutableMethod<?, ?> method, InterceptorKind interceptorKind, AnnotationMetadata annotationMetadata) {
      if (interceptorKind == InterceptorKind.INTRODUCTION) {
         if (annotationMetadata.hasStereotype(Adapter.class)) {
            return new MethodInterceptor[]{new AdapterIntroduction(this.beanContext, method)};
         } else {
            throw new IllegalStateException(
               "At least one @Introduction method interceptor required, but missing for method: "
                  + method.getDescription(true)
                  + ". Check if your @Introduction stereotype annotation is marked with @Retention(RUNTIME) and @InterceptorBean(..) with the interceptor type. Otherwise do not load @Introduction beans if their interceptor definitions are missing!"
            );
         }
      } else {
         return ZERO_METHOD_INTERCEPTORS;
      }
   }

   private <T, R> Stream<? extends Interceptor<T, R>> interceptorStream(
      Class<?> declaringType,
      Collection<BeanRegistration<Interceptor<T, R>>> interceptors,
      InterceptorKind interceptorKind,
      Collection<AnnotationValue<?>> applicableBindings
   ) {
      return interceptors.stream()
         .filter(beanRegistration -> {
            List<Argument<?>> typeArgs = beanRegistration.getBeanDefinition().getTypeArguments(ConstructorInterceptor.class);
            if (typeArgs.isEmpty()) {
               return true;
            } else {
               Class<?> applicableType = ((Argument)typeArgs.iterator().next()).getType();
               return applicableType.isAssignableFrom(declaringType);
            }
         })
         .filter(
            beanRegistration -> {
               Collection<AnnotationValue<?>> interceptorValues = AbstractInterceptorChain.resolveInterceptorValues(
                  beanRegistration.getBeanDefinition().getAnnotationMetadata(), interceptorKind
               );
               if (interceptorValues.isEmpty()) {
                  for(AnnotationValue<?> applicableValue : applicableBindings) {
                     if (this.isApplicableByType(beanRegistration, applicableValue)) {
                        return true;
                     }
                  }
      
                  return false;
               } else if (interceptorValues.size() == 1) {
                  AnnotationValue<?> interceptorBinding = (AnnotationValue)interceptorValues.iterator().next();
                  AnnotationValue<Annotation> memberBinding = (AnnotationValue)interceptorBinding.getAnnotation("bindMembers").orElse(null);
                  String annotationName = (String)interceptorBinding.stringValue().orElse(null);
                  if (annotationName != null) {
                     for(AnnotationValue<?> applicableBinding : applicableBindings) {
                        if (this.isApplicableByType(beanRegistration, applicableBinding)) {
                           return true;
                        }
      
                        if (annotationName.equals(applicableBinding.stringValue().orElse(null))) {
                           if (memberBinding == null) {
                              return true;
                           }
      
                           AnnotationValue<Annotation> otherMembers = (AnnotationValue)applicableBinding.getAnnotation("bindMembers").orElse(null);
                           if (memberBinding.equals(otherMembers)) {
                              return true;
                           }
                        }
                     }
                  }
      
                  return false;
               } else {
                  boolean isApplicationByBinding = true;
      
                  for(AnnotationValue<?> annotationValue : applicableBindings) {
                     AnnotationValue<Annotation> memberBinding = (AnnotationValue)annotationValue.getAnnotation("bindMembers").orElse(null);
                     String annotationName = (String)annotationValue.stringValue().orElse(null);
                     if (annotationName != null) {
                        boolean interceptorApplicable = true;
      
                        for(AnnotationValue<?> applicableValue : interceptorValues) {
                           if (this.isApplicableByType(beanRegistration, applicableValue)) {
                              return true;
                           }
      
                           if (annotationName.equals(applicableValue.stringValue().orElse(null))) {
                              if (memberBinding == null) {
                                 interceptorApplicable = true;
                                 break;
                              }
      
                              AnnotationValue<Annotation> otherMembers = (AnnotationValue)applicableValue.getAnnotation("bindMembers").orElse(null);
                              interceptorApplicable = memberBinding.equals(otherMembers);
                              if (interceptorApplicable) {
                                 break;
                              }
                           } else {
                              interceptorApplicable = false;
                           }
                        }
      
                        isApplicationByBinding = interceptorApplicable;
                        if (!interceptorApplicable) {
                           break;
                        }
                     }
                  }
      
                  return isApplicationByBinding;
               }
            }
         )
         .sorted(OrderUtil.COMPARATOR)
         .map(BeanRegistration::getBean);
   }

   private <T, R> boolean isApplicableByType(BeanRegistration<Interceptor<T, R>> beanRegistration, AnnotationValue<?> applicableValue) {
      return applicableValue.classValue("interceptorType").map(t -> t.isInstance(beanRegistration.getBean())).orElse(false);
   }

   @NonNull
   @Override
   public <T> Interceptor<T, T>[] resolveConstructorInterceptors(
      @NonNull BeanConstructor<T> constructor, @NonNull Collection<BeanRegistration<Interceptor<T, T>>> interceptors
   ) {
      instrumentAnnotationMetadata(this.beanContext, constructor);
      Collection<AnnotationValue<?>> applicableBindings = AbstractInterceptorChain.resolveInterceptorValues(
         constructor.getAnnotationMetadata(), InterceptorKind.AROUND_CONSTRUCT
      );
      Interceptor[] resolvedInterceptors = (Interceptor[])this.interceptorStream(
            constructor.getDeclaringBeanType(), interceptors, InterceptorKind.AROUND_CONSTRUCT, applicableBindings
         )
         .filter(bean -> bean instanceof ConstructorInterceptor || !(bean instanceof MethodInterceptor))
         .toArray(x$0 -> new Interceptor[x$0]);
      if (LOG.isTraceEnabled()) {
         LOG.trace(
            "Resolved {} {} interceptors out of a possible {} for constructor: {} - {}",
            resolvedInterceptors.length,
            InterceptorKind.AROUND_CONSTRUCT,
            interceptors.size(),
            constructor.getDeclaringBeanType(),
            constructor.getDescription(true)
         );

         for(int i = 0; i < resolvedInterceptors.length; ++i) {
            Interceptor<?, ?> resolvedInterceptor = resolvedInterceptors[i];
            LOG.trace("Interceptor {} - {}", i, resolvedInterceptor);
         }
      }

      return resolvedInterceptors;
   }

   private static void instrumentAnnotationMetadata(BeanContext beanContext, Object method) {
      if (beanContext instanceof ApplicationContext && method instanceof EnvironmentConfigurable) {
         EnvironmentConfigurable m = (EnvironmentConfigurable)method;
         if (m.hasPropertyExpressions()) {
            m.configure(((ApplicationContext)beanContext).getEnvironment());
         }
      }

   }
}
