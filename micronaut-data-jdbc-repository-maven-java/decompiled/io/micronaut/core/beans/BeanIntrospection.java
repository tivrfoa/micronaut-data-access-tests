package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.naming.Named;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface BeanIntrospection<T> extends AnnotationMetadataDelegate {
   @NonNull
   Collection<BeanProperty<T, Object>> getBeanProperties();

   @NonNull
   Collection<BeanProperty<T, Object>> getIndexedProperties(@NonNull Class<? extends Annotation> annotationType);

   @NonNull
   T instantiate() throws InstantiationException;

   @NonNull
   default T instantiate(Object... arguments) throws InstantiationException {
      return this.instantiate(true, arguments);
   }

   @NonNull
   T instantiate(boolean strictNullable, Object... arguments) throws InstantiationException;

   @NonNull
   Class<T> getBeanType();

   @NonNull
   Optional<BeanProperty<T, Object>> getIndexedProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String annotationValue);

   @NonNull
   default Collection<BeanMethod<T, Object>> getBeanMethods() {
      return Collections.emptyList();
   }

   @NonNull
   default Optional<BeanProperty<T, Object>> getIndexedProperty(@NonNull Class<? extends Annotation> annotationType) {
      return this.getIndexedProperties(annotationType).stream().findFirst();
   }

   @NonNull
   default Argument<?>[] getConstructorArguments() {
      return Argument.ZERO_ARGUMENTS;
   }

   @NonNull
   default Optional<BeanProperty<T, Object>> getProperty(@NonNull String name) {
      return Optional.empty();
   }

   default int propertyIndexOf(@NonNull String name) {
      int index = 0;

      for(BeanProperty<T, Object> property : this.getBeanProperties()) {
         if (property.getName().equals(name)) {
            return index;
         }

         ++index;
      }

      return -1;
   }

   @NonNull
   default <P> BeanProperty<T, P> getRequiredProperty(@NonNull String name, @NonNull Class<P> type) {
      return (BeanProperty<T, P>)this.getProperty(name, type)
         .orElseThrow(() -> new IntrospectionException("No property [" + name + "] of type [" + type + "] present"));
   }

   @NonNull
   default <P> Optional<BeanProperty<T, P>> getProperty(@NonNull String name, @NonNull Class<P> type) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("type", (T)type);
      BeanProperty<T, ?> prop = (BeanProperty)this.getProperty(name).orElse(null);
      return prop != null && type.isAssignableFrom(prop.getType()) ? Optional.of(prop) : Optional.empty();
   }

   @NonNull
   default String[] getPropertyNames() {
      return (String[])this.getBeanProperties().stream().map(Named::getName).toArray(x$0 -> new String[x$0]);
   }

   @NonNull
   default BeanConstructor<T> getConstructor() {
      return new BeanConstructor<T>() {
         @NonNull
         @Override
         public Class<T> getDeclaringBeanType() {
            return BeanIntrospection.this.getBeanType();
         }

         @NonNull
         @Override
         public Argument<?>[] getArguments() {
            return BeanIntrospection.this.getConstructorArguments();
         }

         @NonNull
         @Override
         public T instantiate(Object... parameterValues) {
            return (T)BeanIntrospection.this.instantiate(parameterValues);
         }
      };
   }

   static <T2> BeanIntrospection<T2> getIntrospection(Class<T2> type) {
      return BeanIntrospector.SHARED.getIntrospection(type);
   }
}
