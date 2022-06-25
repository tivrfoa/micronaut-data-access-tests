package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Internal
public abstract class AbstractBeanIntrospection<T> implements BeanIntrospection<T> {
   protected final AnnotationMetadata annotationMetadata;
   protected final Class<T> beanType;
   protected final Map<String, BeanProperty<T, Object>> beanProperties;
   protected final List<BeanMethod<T, Object>> beanMethods;
   private Map<Class<? extends Annotation>, List<BeanProperty<T, Object>>> indexed;
   private Map<AbstractBeanIntrospection<T>.AnnotationValueKey, BeanProperty<T, Object>> indexedValues;

   protected AbstractBeanIntrospection(@NonNull Class<T> beanType, @Nullable AnnotationMetadata annotationMetadata, int propertyCount) {
      this(beanType, annotationMetadata, propertyCount, 0);
   }

   protected AbstractBeanIntrospection(@NonNull Class<T> beanType, @Nullable AnnotationMetadata annotationMetadata, int propertyCount, int methodCount) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      this.beanType = beanType;
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.beanProperties = new LinkedHashMap(propertyCount);
      this.beanMethods = (List<BeanMethod<T, Object>>)(methodCount == 0 ? Collections.emptyList() : new ArrayList(methodCount));
   }

   @Override
   public BeanConstructor<T> getConstructor() {
      return new BeanConstructor<T>() {
         @Override
         public Class<T> getDeclaringBeanType() {
            return AbstractBeanIntrospection.this.getBeanType();
         }

         @Override
         public Argument<?>[] getArguments() {
            return AbstractBeanIntrospection.this.getConstructorArguments();
         }

         @Override
         public T instantiate(Object... parameterValues) {
            return (T)AbstractBeanIntrospection.this.instantiate(parameterValues);
         }

         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return AbstractBeanIntrospection.this.getConstructorAnnotationMetadata();
         }
      };
   }

   protected AnnotationMetadata getConstructorAnnotationMetadata() {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   @NonNull
   @Override
   public Optional<BeanProperty<T, Object>> getIndexedProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String annotationValue) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      return this.indexedValues != null && StringUtils.isNotEmpty(annotationValue)
         ? Optional.ofNullable(this.indexedValues.get(new AbstractBeanIntrospection.AnnotationValueKey(annotationType, annotationValue)))
         : Optional.empty();
   }

   @NonNull
   @Override
   public T instantiate(boolean strictNullable, Object... arguments) throws InstantiationException {
      ArgumentUtils.requireNonNull("arguments", arguments);
      if (arguments.length == 0) {
         return this.instantiate();
      } else {
         Argument<?>[] constructorArguments = this.getConstructorArguments();
         if (constructorArguments.length != arguments.length) {
            throw new InstantiationException("Argument count [" + arguments.length + "] doesn't match required argument count: " + constructorArguments.length);
         } else {
            for(int i = 0; i < constructorArguments.length; ++i) {
               Argument<?> constructorArgument = constructorArguments[i];
               Object specified = arguments[i];
               if (specified == null) {
                  if (!constructorArgument.isDeclaredNullable() && strictNullable) {
                     throw new InstantiationException(
                        "Null argument specified for ["
                           + constructorArgument.getName()
                           + "]. If this argument is allowed to be null annotate it with @Nullable"
                     );
                  }
               } else if (!ReflectionUtils.getWrapperType(constructorArgument.getType()).isInstance(specified)) {
                  throw new InstantiationException("Invalid argument [" + specified + "] specified for argument: " + constructorArgument);
               }
            }

            return this.instantiateInternal(arguments);
         }
      }
   }

   @NonNull
   @Override
   public Optional<BeanProperty<T, Object>> getProperty(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", (T)name);
      return Optional.ofNullable(this.beanProperties.get(name));
   }

   @Override
   public int propertyIndexOf(String name) {
      return new ArrayList(this.beanProperties.keySet()).indexOf(name);
   }

   @NonNull
   @Override
   public Collection<BeanProperty<T, Object>> getIndexedProperties(@NonNull Class<? extends Annotation> annotationType) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      if (this.indexed != null) {
         List<BeanProperty<T, Object>> indexed = (List)this.indexed.get(annotationType);
         if (indexed != null) {
            return Collections.unmodifiableCollection(indexed);
         }
      }

      return Collections.emptyList();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public Collection<BeanProperty<T, Object>> getBeanProperties() {
      return Collections.unmodifiableCollection(this.beanProperties.values());
   }

   @NonNull
   @Override
   public Class<T> getBeanType() {
      return this.beanType;
   }

   @Internal
   protected abstract T instantiateInternal(Object[] arguments);

   @Internal
   protected final void addProperty(@NonNull BeanProperty<T, Object> property) {
      ArgumentUtils.requireNonNull("property", property);
      this.beanProperties.put(property.getName(), property);
   }

   @Internal
   protected final void addMethod(@NonNull BeanMethod<T, Object> method) {
      ArgumentUtils.requireNonNull("method", method);
      this.beanMethods.add(method);
   }

   @NonNull
   @Override
   public Collection<BeanMethod<T, Object>> getBeanMethods() {
      return Collections.unmodifiableCollection(this.beanMethods);
   }

   @Internal
   protected final void indexProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String propertyName) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      if (StringUtils.isNotEmpty(propertyName)) {
         BeanProperty<T, Object> property = (BeanProperty)this.beanProperties.get(propertyName);
         if (property == null) {
            throw new IllegalStateException("Invalid byte code generated during bean introspection. Call addProperty first!");
         }

         if (this.indexed == null) {
            this.indexed = new HashMap(2);
         }

         List<BeanProperty<T, Object>> indexed = (List)this.indexed.computeIfAbsent(annotationType, aClass -> new ArrayList(2));
         indexed.add(property);
      }

   }

   @Internal
   protected final void indexProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String propertyName, @Nullable String annotationValue) {
      this.indexProperty(annotationType, propertyName);
      if (StringUtils.isNotEmpty(annotationValue) && StringUtils.isNotEmpty(propertyName)) {
         if (this.indexedValues == null) {
            this.indexedValues = new HashMap(10);
         }

         BeanProperty<T, Object> property = (BeanProperty)this.beanProperties.get(propertyName);
         this.indexedValues.put(new AbstractBeanIntrospection.AnnotationValueKey(annotationType, annotationValue), property);
      }

   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractBeanIntrospection<?> that = (AbstractBeanIntrospection)o;
         return Objects.equals(this.beanType, that.beanType);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.beanType});
   }

   public String toString() {
      return "BeanIntrospection{type=" + this.beanType + '}';
   }

   private final class AnnotationValueKey {
      @NonNull
      final Class<? extends Annotation> type;
      @NonNull
      final String value;

      AnnotationValueKey(@NonNull Class<? extends Annotation> type, @NonNull String value) {
         this.type = type;
         this.value = value;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractBeanIntrospection<T>.AnnotationValueKey that = (AbstractBeanIntrospection.AnnotationValueKey)o;
            return this.type.equals(that.type) && this.value.equals(that.value);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.type, this.value});
      }
   }
}
