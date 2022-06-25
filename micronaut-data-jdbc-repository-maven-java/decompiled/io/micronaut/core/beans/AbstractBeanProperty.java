package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Objects;

@Internal
public abstract class AbstractBeanProperty<B, P> implements UnsafeBeanProperty<B, P> {
   private final BeanIntrospection<B> introspection;
   private final Class<B> beanType;
   private final Class<P> type;
   private final String name;
   private final AnnotationMetadata annotationMetadata;
   private final Argument[] typeArguments;
   private final Class<?> typeOrWrapperType;

   @Internal
   protected AbstractBeanProperty(
      @NonNull BeanIntrospection<B> introspection,
      @NonNull Class<P> type,
      @NonNull String name,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument[] typeArguments
   ) {
      this.introspection = introspection;
      this.type = type;
      this.beanType = introspection.getBeanType();
      this.name = name;
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.typeArguments = typeArguments;
      this.typeOrWrapperType = ReflectionUtils.getWrapperType(type);
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   @NonNull
   @Override
   public Class<P> getType() {
      return this.type;
   }

   @NonNull
   @Override
   public Argument<P> asArgument() {
      return this.typeArguments != null
         ? Argument.of(this.type, this.name, this.getAnnotationMetadata(), this.typeArguments)
         : Argument.of(this.type, this.name, this.getAnnotationMetadata());
   }

   @NonNull
   @Override
   public final BeanIntrospection<B> getDeclaringBean() {
      return this.introspection;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Nullable
   @Override
   public final P get(@NonNull B bean) {
      ArgumentUtils.requireNonNull("bean", bean);
      if (!this.beanType.isInstance(bean)) {
         throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + this.introspection.getBeanType());
      } else if (this.isWriteOnly()) {
         throw new UnsupportedOperationException("Cannot read from a write-only property");
      } else {
         return this.readInternal(bean);
      }
   }

   @Override
   public final P getUnsafe(B bean) {
      return this.readInternal(bean);
   }

   @Override
   public B withValue(@NonNull B bean, @Nullable P value) {
      ArgumentUtils.requireNonNull("bean", bean);
      if (!this.beanType.isInstance(bean)) {
         throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + this.introspection.getBeanType());
      } else {
         return this.withValueUnsafe(bean, value);
      }
   }

   @Override
   public final B withValueUnsafe(B bean, P value) {
      return (B)(value == this.getUnsafe(bean) ? bean : this.withValueInternal(bean, value));
   }

   @Override
   public final void set(@NonNull B bean, @Nullable P value) {
      ArgumentUtils.requireNonNull("bean", bean);
      if (!this.beanType.isInstance(bean)) {
         throw new IllegalArgumentException("Invalid bean [" + bean + "] for type: " + this.beanType);
      } else if (this.isReadOnly()) {
         throw new UnsupportedOperationException("Cannot write a read-only property: " + this.getName());
      } else if (value != null && !this.typeOrWrapperType.isInstance(value)) {
         throw new IllegalArgumentException("Specified value [" + value + "] is not of the correct type: " + this.getType());
      } else {
         this.writeInternal(bean, value);
      }
   }

   @Override
   public final void setUnsafe(B bean, P value) {
      this.writeInternal(bean, value);
   }

   @Internal
   protected B withValueInternal(B bean, P value) {
      return UnsafeBeanProperty.super.withValue(bean, value);
   }

   @Internal
   protected abstract void writeInternal(@NonNull B bean, @Nullable P value);

   @Internal
   protected abstract P readInternal(@NonNull B bean);

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractBeanProperty<?, ?> that = (AbstractBeanProperty)o;
         return Objects.equals(this.beanType, that.beanType) && Objects.equals(this.type, that.type) && Objects.equals(this.name, that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.beanType, this.type, this.name});
   }

   public String toString() {
      return "BeanProperty{beanType=" + this.beanType + ", type=" + this.type + ", name='" + this.name + '\'' + '}';
   }
}
