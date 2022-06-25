package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.ValueResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
public interface BeanResolutionContext extends ValueResolver<CharSequence>, AutoCloseable {
   default void close() {
   }

   @NonNull
   <T> T getBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> Collection<T> getBeansOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> Stream<T> streamOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> Optional<T> findBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> T inject(@Nullable BeanDefinition<?> beanDefinition, @NonNull T instance);

   @NonNull
   <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   void destroyInjectScopedBeans();

   BeanResolutionContext copy();

   BeanContext getContext();

   BeanDefinition getRootDefinition();

   BeanResolutionContext.Path getPath();

   Object setAttribute(CharSequence key, Object value);

   Object getAttribute(CharSequence key);

   Object removeAttribute(CharSequence key);

   <T> void addInFlightBean(BeanIdentifier beanIdentifier, BeanRegistration<T> beanRegistration);

   void removeInFlightBean(BeanIdentifier beanIdentifier);

   @Nullable
   <T> BeanRegistration<T> getInFlightBean(BeanIdentifier beanIdentifier);

   @Nullable
   Qualifier<?> getCurrentQualifier();

   void setCurrentQualifier(@Nullable Qualifier<?> qualifier);

   <T> void addDependentBean(BeanRegistration<T> beanRegistration);

   @NonNull
   default List<BeanRegistration<?>> getAndResetDependentBeans() {
      return Collections.emptyList();
   }

   @Nullable
   default List<BeanRegistration<?>> popDependentBeans() {
      return null;
   }

   default void pushDependentBeans(@Nullable List<BeanRegistration<?>> dependentBeans) {
   }

   default void markDependentAsFactory() {
   }

   @Nullable
   default BeanRegistration<?> getAndResetDependentFactoryBean() {
      return null;
   }

   public interface Path extends Deque<BeanResolutionContext.Segment<?>>, AutoCloseable {
      BeanResolutionContext.Path pushBeanCreate(BeanDefinition<?> declaringType, Argument<?> beanType);

      BeanResolutionContext.Path pushConstructorResolve(
         BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments, boolean requiresReflection
      );

      BeanResolutionContext.Path pushConstructorResolve(BeanDefinition declaringType, Argument argument);

      BeanResolutionContext.Path pushMethodArgumentResolve(BeanDefinition declaringType, MethodInjectionPoint methodInjectionPoint, Argument argument);

      BeanResolutionContext.Path pushMethodArgumentResolve(
         BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments, boolean requiresReflection
      );

      BeanResolutionContext.Path pushFieldResolve(BeanDefinition declaringType, FieldInjectionPoint fieldInjectionPoint);

      BeanResolutionContext.Path pushFieldResolve(BeanDefinition declaringType, Argument fieldAsArgument, boolean requiresReflection);

      BeanResolutionContext.Path pushAnnotationResolve(BeanDefinition beanDefinition, Argument annotationMemberBeanAsArgument);

      String toCircularString();

      Optional<BeanResolutionContext.Segment<?>> currentSegment();

      default void close() {
         this.pop();
      }
   }

   public interface Segment<T> {
      BeanDefinition<T> getDeclaringType();

      InjectionPoint<T> getInjectionPoint();

      String getName();

      Argument getArgument();
   }
}
