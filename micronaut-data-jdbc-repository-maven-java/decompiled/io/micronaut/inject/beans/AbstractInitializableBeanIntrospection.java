package io.micronaut.inject.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanMethod;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.UnsafeBeanProperty;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.ExecutableMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractInitializableBeanIntrospection<B> implements BeanIntrospection<B> {
   private final Class<B> beanType;
   private final AnnotationMetadata annotationMetadata;
   private final AnnotationMetadata constructorAnnotationMetadata;
   private final Argument<?>[] constructorArguments;
   private final List<BeanProperty<B, Object>> beanProperties;
   private final List<BeanMethod<B, Object>> beanMethods;
   private BeanConstructor<B> beanConstructor;

   public AbstractInitializableBeanIntrospection(
      Class<B> beanType,
      AnnotationMetadata annotationMetadata,
      AnnotationMetadata constructorAnnotationMetadata,
      Argument<?>[] constructorArguments,
      AbstractInitializableBeanIntrospection.BeanPropertyRef<Object>[] propertiesRefs,
      AbstractInitializableBeanIntrospection.BeanMethodRef<Object>[] methodsRefs
   ) {
      this.beanType = beanType;
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.constructorAnnotationMetadata = constructorAnnotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : constructorAnnotationMetadata;
      this.constructorArguments = constructorArguments == null ? Argument.ZERO_ARGUMENTS : constructorArguments;
      if (propertiesRefs != null) {
         List<BeanProperty<B, Object>> beanProperties = new ArrayList(propertiesRefs.length);

         for(AbstractInitializableBeanIntrospection.BeanPropertyRef beanPropertyRef : propertiesRefs) {
            beanProperties.add(new AbstractInitializableBeanIntrospection.BeanPropertyImpl(beanPropertyRef));
         }

         this.beanProperties = Collections.unmodifiableList(beanProperties);
      } else {
         this.beanProperties = Collections.emptyList();
      }

      if (methodsRefs != null) {
         List<BeanMethod<B, Object>> beanMethods = new ArrayList(methodsRefs.length);

         for(AbstractInitializableBeanIntrospection.BeanMethodRef beanMethodRef : methodsRefs) {
            beanMethods.add(new AbstractInitializableBeanIntrospection.BeanMethodImpl(beanMethodRef));
         }

         this.beanMethods = Collections.unmodifiableList(beanMethods);
      } else {
         this.beanMethods = Collections.emptyList();
      }

   }

   @NonNull
   @Internal
   protected abstract B instantiateInternal(@Nullable Object[] arguments);

   @Internal
   protected BeanProperty<B, Object> getPropertyByIndex(int index) {
      return (BeanProperty<B, Object>)this.beanProperties.get(index);
   }

   @Nullable
   protected <V> V dispatch(int index, @NonNull B target, @Nullable Object[] args) {
      throw this.unknownDispatchAtIndexException(index);
   }

   @Nullable
   protected <V> V dispatchOne(int index, @NonNull Object target, @Nullable Object arg) {
      throw this.unknownDispatchAtIndexException(index);
   }

   protected final RuntimeException unknownDispatchAtIndexException(int index) {
      return new IllegalStateException("Unknown dispatch at index: " + index);
   }

   @Nullable
   public BeanProperty<B, Object> findIndexedProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String annotationValue) {
      return null;
   }

   @NonNull
   @Override
   public Collection<BeanProperty<B, Object>> getIndexedProperties(@NonNull Class<? extends Annotation> annotationType) {
      return Collections.emptyList();
   }

   protected Collection<BeanProperty<B, Object>> getBeanPropertiesIndexedSubset(int[] indexes) {
      return (Collection<BeanProperty<B, Object>>)(indexes.length == 0
         ? Collections.emptyList()
         : new AbstractInitializableBeanIntrospection.IndexedCollections<BeanProperty<B, Object>>(indexes, this.beanProperties));
   }

   @Override
   public B instantiate() throws InstantiationException {
      throw new InstantiationException("No default constructor exists");
   }

   @NonNull
   @Override
   public B instantiate(boolean strictNullable, Object... arguments) throws InstantiationException {
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

   @Override
   public BeanConstructor<B> getConstructor() {
      if (this.beanConstructor == null) {
         this.beanConstructor = new BeanConstructor<B>() {
            @Override
            public Class<B> getDeclaringBeanType() {
               return AbstractInitializableBeanIntrospection.this.beanType;
            }

            @Override
            public Argument<?>[] getArguments() {
               return AbstractInitializableBeanIntrospection.this.constructorArguments;
            }

            @Override
            public B instantiate(Object... parameterValues) {
               return (B)AbstractInitializableBeanIntrospection.this.instantiate(parameterValues);
            }

            @Override
            public AnnotationMetadata getAnnotationMetadata() {
               return AbstractInitializableBeanIntrospection.this.constructorAnnotationMetadata;
            }
         };
      }

      return this.beanConstructor;
   }

   @Override
   public Argument<?>[] getConstructorArguments() {
      return this.constructorArguments;
   }

   @NonNull
   @Override
   public Optional<BeanProperty<B, Object>> getIndexedProperty(@NonNull Class<? extends Annotation> annotationType, @NonNull String annotationValue) {
      return Optional.ofNullable(this.findIndexedProperty(annotationType, annotationValue));
   }

   @NonNull
   @Override
   public Optional<BeanProperty<B, Object>> getProperty(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", name);
      int index = this.propertyIndexOf(name);
      return index == -1 ? Optional.empty() : Optional.of(this.beanProperties.get(index));
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @NonNull
   @Override
   public Collection<BeanProperty<B, Object>> getBeanProperties() {
      return this.beanProperties;
   }

   @NonNull
   @Override
   public Class<B> getBeanType() {
      return this.beanType;
   }

   @NonNull
   @Override
   public Collection<BeanMethod<B, Object>> getBeanMethods() {
      return this.beanMethods;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractInitializableBeanIntrospection<?> that = (AbstractInitializableBeanIntrospection)o;
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

   private final class BeanMethodImpl<P> implements BeanMethod<B, P>, ExecutableMethod<B, P> {
      private final AbstractInitializableBeanIntrospection.BeanMethodRef<P> ref;

      private BeanMethodImpl(AbstractInitializableBeanIntrospection.BeanMethodRef<P> ref) {
         this.ref = ref;
      }

      @NonNull
      @Override
      public BeanIntrospection<B> getDeclaringBean() {
         return AbstractInitializableBeanIntrospection.this;
      }

      @NonNull
      @Override
      public ReturnType<P> getReturnType() {
         return new ReturnType() {
            @Override
            public Class<P> getType() {
               return BeanMethodImpl.this.ref.returnType.getType();
            }

            @NonNull
            @Override
            public Argument<P> asArgument() {
               return BeanMethodImpl.this.ref.returnType;
            }

            @Override
            public Map<String, Argument<?>> getTypeVariables() {
               return BeanMethodImpl.this.ref.returnType.getTypeVariables();
            }

            @NonNull
            @Override
            public AnnotationMetadata getAnnotationMetadata() {
               return BeanMethodImpl.this.ref.returnType.getAnnotationMetadata();
            }
         };
      }

      @NonNull
      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.ref.annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : this.ref.annotationMetadata;
      }

      @NonNull
      @Override
      public String getName() {
         return this.ref.name;
      }

      @Override
      public Argument<?>[] getArguments() {
         return this.ref.arguments == null ? Argument.ZERO_ARGUMENTS : this.ref.arguments;
      }

      @Override
      public P invoke(@NonNull B instance, Object... arguments) {
         return AbstractInitializableBeanIntrospection.this.dispatch(this.ref.methodIndex, instance, arguments);
      }

      @Override
      public Method getTargetMethod() {
         if (ClassUtils.REFLECTION_LOGGER.isWarnEnabled()) {
            ClassUtils.REFLECTION_LOGGER
               .warn(
                  "Using getTargetMethod for method {} on type {} requires the use of reflection. GraalVM configuration necessary",
                  this.getName(),
                  this.getDeclaringType()
               );
         }

         return ReflectionUtils.getRequiredMethod(this.getDeclaringType(), this.getMethodName(), this.getArgumentTypes());
      }

      @Override
      public Class<B> getDeclaringType() {
         return this.getDeclaringBean().getBeanType();
      }

      @Override
      public String getMethodName() {
         return this.getName();
      }
   }

   @Internal
   public static final class BeanMethodRef<P> {
      @NonNull
      final Argument<P> returnType;
      @NonNull
      final String name;
      @Nullable
      final AnnotationMetadata annotationMetadata;
      @Nullable
      final Argument<?>[] arguments;
      final int methodIndex;

      public BeanMethodRef(
         @NonNull Argument<P> returnType,
         @NonNull String name,
         @Nullable AnnotationMetadata annotationMetadata,
         @Nullable Argument<?>[] arguments,
         int methodIndex
      ) {
         this.returnType = returnType;
         this.name = name;
         this.annotationMetadata = annotationMetadata;
         this.arguments = arguments;
         this.methodIndex = methodIndex;
      }
   }

   private final class BeanPropertyImpl<P> implements UnsafeBeanProperty<B, P> {
      private final AbstractInitializableBeanIntrospection.BeanPropertyRef<P> ref;
      private final Class<?> typeOrWrapperType;

      private BeanPropertyImpl(AbstractInitializableBeanIntrospection.BeanPropertyRef<P> ref) {
         this.ref = ref;
         this.typeOrWrapperType = ReflectionUtils.getWrapperType(this.getType());
      }

      @NonNull
      @Override
      public String getName() {
         return this.ref.argument.getName();
      }

      @NonNull
      @Override
      public Class<P> getType() {
         return this.ref.argument.getType();
      }

      @NonNull
      @Override
      public Argument<P> asArgument() {
         return this.ref.argument;
      }

      @NonNull
      @Override
      public BeanIntrospection<B> getDeclaringBean() {
         return AbstractInitializableBeanIntrospection.this;
      }

      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.ref.argument.getAnnotationMetadata();
      }

      @Nullable
      @Override
      public P get(@NonNull B bean) {
         ArgumentUtils.requireNonNull("bean", bean);
         if (!AbstractInitializableBeanIntrospection.this.beanType.isInstance(bean)) {
            throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + AbstractInitializableBeanIntrospection.this.beanType);
         } else if (this.isWriteOnly()) {
            throw new UnsupportedOperationException("Cannot read from a write-only property");
         } else {
            return AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.getMethodIndex, bean, null);
         }
      }

      @Override
      public P getUnsafe(B bean) {
         return AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.getMethodIndex, bean, null);
      }

      @Override
      public void set(@NonNull B bean, @Nullable P value) {
         ArgumentUtils.requireNonNull("bean", bean);
         if (!AbstractInitializableBeanIntrospection.this.beanType.isInstance(bean)) {
            throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + bean);
         } else if (this.isReadOnly()) {
            throw new UnsupportedOperationException("Cannot write a read-only property: " + this.getName());
         } else if (value != null && !this.typeOrWrapperType.isInstance(value)) {
            throw new IllegalArgumentException("Specified value [" + value + "] is not of the correct type: " + this.getType());
         } else {
            AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.setMethodIndex, bean, value);
         }
      }

      @Override
      public void setUnsafe(B bean, P value) {
         AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.setMethodIndex, bean, value);
      }

      @Override
      public B withValue(@NonNull B bean, @Nullable P value) {
         ArgumentUtils.requireNonNull("bean", bean);
         if (!AbstractInitializableBeanIntrospection.this.beanType.isInstance(bean)) {
            throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + AbstractInitializableBeanIntrospection.this.beanType);
         } else {
            return (B)this.withValueUnsafe((P)bean, value);
         }
      }

      @Override
      public B withValueUnsafe(B bean, P value) {
         if (value == this.getUnsafe(bean)) {
            return bean;
         } else if (this.ref.withMethodIndex == -1) {
            if (!this.ref.readyOnly && this.ref.setMethodIndex != -1) {
               AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.setMethodIndex, bean, value);
               return bean;
            } else {
               return UnsafeBeanProperty.super.withValue(bean, value);
            }
         } else {
            return AbstractInitializableBeanIntrospection.this.dispatchOne(this.ref.withMethodIndex, bean, value);
         }
      }

      @Override
      public boolean isReadOnly() {
         return this.ref.readyOnly;
      }

      @Override
      public boolean isWriteOnly() {
         return this.ref.getMethodIndex == -1 && (this.ref.setMethodIndex != -1 || this.ref.withMethodIndex != -1);
      }

      @Override
      public boolean hasSetterOrConstructorArgument() {
         return this.ref.mutable;
      }

      public String toString() {
         return "BeanProperty{beanType="
            + AbstractInitializableBeanIntrospection.this.beanType
            + ", type="
            + this.ref.argument.getType()
            + ", name='"
            + this.ref.argument.getName()
            + '\''
            + '}';
      }
   }

   @Internal
   public static final class BeanPropertyRef<P> {
      @NonNull
      final Argument<P> argument;
      final int getMethodIndex;
      final int setMethodIndex;
      final int withMethodIndex;
      final boolean readyOnly;
      final boolean mutable;

      public BeanPropertyRef(@NonNull Argument<P> argument, int getMethodIndex, int setMethodIndex, int valueMethodIndex, boolean readyOnly, boolean mutable) {
         this.argument = argument;
         this.getMethodIndex = getMethodIndex;
         this.setMethodIndex = setMethodIndex;
         this.withMethodIndex = valueMethodIndex;
         this.readyOnly = readyOnly;
         this.mutable = mutable;
      }
   }

   private static final class IndexedCollections<T> extends AbstractCollection<T> {
      private final int[] indexed;
      private final List<T> list;

      private IndexedCollections(int[] indexed, List<T> list) {
         this.indexed = indexed;
         this.list = list;
      }

      public Iterator<T> iterator() {
         return new Iterator<T>() {
            int i = -1;

            public boolean hasNext() {
               return this.i + 1 < IndexedCollections.this.indexed.length;
            }

            public T next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  int index = IndexedCollections.this.indexed[++this.i];
                  return (T)IndexedCollections.this.list.get(index);
               }
            }
         };
      }

      public int size() {
         return this.indexed.length;
      }
   }
}
