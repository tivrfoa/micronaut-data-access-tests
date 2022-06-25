package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Singleton;

@Internal
final class NoInjectionBeanDefinition<T> implements BeanDefinition<T>, BeanDefinitionReference<T> {
   private final Class<?> singletonClass;
   private final Map<Class<?>, List<Argument<?>>> typeArguments = new HashMap();
   private final Qualifier<T> qualifier;

   NoInjectionBeanDefinition(Class<?> singletonClass, Qualifier<T> qualifier) {
      this.singletonClass = singletonClass;
      this.qualifier = qualifier;
   }

   @Nullable
   public Qualifier<T> getQualifier() {
      return this.qualifier;
   }

   @Override
   public Qualifier<T> getDeclaredQualifier() {
      return this.getQualifier();
   }

   @Override
   public Optional<Class<? extends Annotation>> getScope() {
      return Optional.of(Singleton.class);
   }

   @Override
   public Optional<String> getScopeName() {
      return Optional.of("javax.inject.Singleton");
   }

   @NonNull
   @Override
   public List<Argument<?>> getTypeArguments(Class<?> type) {
      List<Argument<?>> result = (List)this.typeArguments.get(type);
      if (result == null) {
         Class[] classes = type.isInterface()
            ? GenericTypeUtils.resolveInterfaceTypeArguments(this.singletonClass, type)
            : GenericTypeUtils.resolveSuperTypeGenericArguments(this.singletonClass, type);
         result = (List)Arrays.stream(classes).map(Argument::of).collect(Collectors.toList());
         this.typeArguments.put(type, result);
      }

      return result;
   }

   @Override
   public boolean isSingleton() {
      return true;
   }

   @Override
   public boolean isProvided() {
      return false;
   }

   @Override
   public boolean isIterable() {
      return false;
   }

   @Override
   public boolean isPrimary() {
      return true;
   }

   @Override
   public Class getBeanType() {
      return this.singletonClass;
   }

   @Override
   public Optional<Class<?>> getDeclaringType() {
      return Optional.empty();
   }

   @Override
   public ConstructorInjectionPoint getConstructor() {
      throw new UnsupportedOperationException(
         "Bean of type ["
            + this.getBeanType()
            + "] is a manually registered singleton that was registered with the context via BeanContext.registerBean(..) and cannot be created directly"
      );
   }

   @Override
   public Collection<Class<?>> getRequiredComponents() {
      return Collections.emptyList();
   }

   @Override
   public Collection<MethodInjectionPoint<T, ?>> getInjectedMethods() {
      return Collections.emptyList();
   }

   @Override
   public Collection<FieldInjectionPoint<T, ?>> getInjectedFields() {
      return Collections.emptyList();
   }

   @Override
   public Collection<MethodInjectionPoint<T, ?>> getPostConstructMethods() {
      return Collections.emptyList();
   }

   @Override
   public Collection<MethodInjectionPoint<T, ?>> getPreDestroyMethods() {
      return Collections.emptyList();
   }

   @NonNull
   @Override
   public String getName() {
      return this.singletonClass.getName();
   }

   @Override
   public boolean isEnabled(BeanContext beanContext) {
      return true;
   }

   @Override
   public boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext) {
      return true;
   }

   @Override
   public <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>[] argumentTypes) {
      return Optional.empty();
   }

   @Override
   public T inject(BeanContext context, T bean) {
      return bean;
   }

   @Override
   public T inject(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
      return bean;
   }

   @Override
   public Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      return Collections.emptyList();
   }

   @Override
   public Stream<ExecutableMethod<T, ?>> findPossibleMethods(String name) {
      return Stream.empty();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NoInjectionBeanDefinition<?> that = (NoInjectionBeanDefinition)o;
         return this.singletonClass.equals(that.singletonClass) && Objects.equals(this.qualifier, that.qualifier);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.singletonClass.hashCode();
   }

   @Override
   public String getBeanDefinitionName() {
      return this.singletonClass.getName();
   }

   @Override
   public BeanDefinition<T> load() {
      return this;
   }

   @Override
   public BeanDefinition<T> load(BeanContext context) {
      return this;
   }

   @Override
   public boolean isContextScope() {
      return false;
   }

   @Override
   public boolean isPresent() {
      return true;
   }
}
