package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface DelegatingBeanDefinition<T> extends BeanDefinition<T> {
   BeanDefinition<T> getTarget();

   @Override
   default boolean requiresMethodProcessing() {
      return this.getTarget().requiresMethodProcessing();
   }

   @Override
   default Optional<Class<? extends Annotation>> getScope() {
      return this.getTarget().getScope();
   }

   @Override
   default Optional<String> getScopeName() {
      return this.getTarget().getScopeName();
   }

   @Override
   default AnnotationMetadata getAnnotationMetadata() {
      return this.getTarget().getAnnotationMetadata();
   }

   @Override
   default <R> ExecutableMethod<T, R> getRequiredMethod(String name, Class<?>... argumentTypes) {
      return this.getTarget().getRequiredMethod(name, argumentTypes);
   }

   @Override
   default boolean isAbstract() {
      return this.getTarget().isAbstract();
   }

   @Override
   default boolean isSingleton() {
      return this.getTarget().isSingleton();
   }

   @Override
   default boolean isProvided() {
      return this.getTarget().isProvided();
   }

   @Override
   default boolean isIterable() {
      return this.getTarget().isIterable();
   }

   @Override
   default Class<T> getBeanType() {
      return this.getTarget().getBeanType();
   }

   @Override
   default ConstructorInjectionPoint<T> getConstructor() {
      return this.getTarget().getConstructor();
   }

   @Override
   default Collection<Class<?>> getRequiredComponents() {
      return this.getTarget().getRequiredComponents();
   }

   @Override
   default Collection<MethodInjectionPoint<T, ?>> getInjectedMethods() {
      return this.getTarget().getInjectedMethods();
   }

   @Override
   default Collection<FieldInjectionPoint<T, ?>> getInjectedFields() {
      return this.getTarget().getInjectedFields();
   }

   @Override
   default Collection<MethodInjectionPoint<T, ?>> getPostConstructMethods() {
      return this.getTarget().getPostConstructMethods();
   }

   @Override
   default Collection<MethodInjectionPoint<T, ?>> getPreDestroyMethods() {
      return this.getTarget().getPreDestroyMethods();
   }

   @NonNull
   @Override
   default String getName() {
      return this.getTarget().getName();
   }

   @Override
   default <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>... argumentTypes) {
      return this.getTarget().findMethod(name, argumentTypes);
   }

   @Override
   default <R> Stream<ExecutableMethod<T, R>> findPossibleMethods(String name) {
      return this.getTarget().findPossibleMethods(name);
   }

   @Override
   default T inject(BeanContext context, T bean) {
      return this.getTarget().inject(context, bean);
   }

   @Override
   default T inject(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
      return this.getTarget().inject(resolutionContext, context, bean);
   }

   @Override
   default Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      return this.getTarget().getExecutableMethods();
   }

   @Override
   default boolean isPrimary() {
      return this.getTarget().isPrimary();
   }

   @Override
   default boolean isEnabled(BeanContext context) {
      return this.getTarget().isEnabled(context);
   }

   @Override
   default boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext) {
      return this.getTarget().isEnabled(context, resolutionContext);
   }

   @Override
   default Optional<Class<?>> getDeclaringType() {
      return this.getTarget().getDeclaringType();
   }

   @NonNull
   @Override
   default List<Argument<?>> getTypeArguments(String type) {
      return this.getTarget().getTypeArguments(type);
   }
}
