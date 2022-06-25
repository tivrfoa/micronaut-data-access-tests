package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface BeanProperty<B, T> extends AnnotatedElement, AnnotationMetadataDelegate, ArgumentCoercible<T> {
   @NonNull
   BeanIntrospection<B> getDeclaringBean();

   @Nullable
   T get(@NonNull B bean);

   @NonNull
   default <T2> Optional<T2> get(@NonNull B bean, @NonNull Class<T2> type) {
      ArgumentUtils.requireNonNull("bean", bean);
      ArgumentUtils.requireNonNull("type", (T)type);
      Argument<T2> argument = Argument.of(type);
      return this.get(bean, argument);
   }

   default <T2> Optional<T2> get(@NonNull B bean, @NonNull Argument<T2> argument) {
      ArgumentUtils.requireNonNull("bean", bean);
      ArgumentUtils.requireNonNull("type", argument);
      ArgumentConversionContext<T2> conversionContext = ConversionContext.of(argument);
      return this.get(bean, conversionContext);
   }

   default <T2> Optional<T2> get(@NonNull B bean, @NonNull ArgumentConversionContext<T2> conversionContext) {
      ArgumentUtils.requireNonNull("bean", bean);
      ArgumentUtils.requireNonNull("conversionContext", conversionContext);
      T v = this.get(bean);
      return ConversionService.SHARED.convert(v, conversionContext);
   }

   @Nullable
   default <T2> T2 get(@NonNull B bean, @NonNull Class<T2> type, @Nullable T2 defaultValue) {
      ArgumentUtils.requireNonNull("bean", bean);
      if (type == null) {
         return defaultValue;
      } else {
         T v = this.get(bean);
         return (T2)ConversionService.SHARED.convert(v, type).orElse(defaultValue);
      }
   }

   default boolean hasSetterOrConstructorArgument() {
      BeanIntrospection<B> declaringBean = this.getDeclaringBean();
      return !this.isReadOnly()
         || Arrays.stream(declaringBean.getConstructorArguments()).anyMatch(arg -> declaringBean.getProperty(arg.getName(), arg.getType()).isPresent());
   }

   default B withValue(@NonNull B bean, @Nullable T value) {
      if (this.isReadOnly()) {
         if (value == this.get(bean)) {
            return bean;
         } else {
            BeanIntrospection<B> declaringBean = this.getDeclaringBean();
            Argument<?>[] constructorArguments = declaringBean.getConstructorArguments();
            Object[] values = new Object[constructorArguments.length];
            boolean found = false;

            for(int i = 0; i < constructorArguments.length; ++i) {
               Argument<?> constructorArgument = constructorArguments[i];
               String argumentName = constructorArgument.getName();
               Class<?> argumentType = constructorArgument.getType();
               BeanProperty<B, ?> prop = (BeanProperty)declaringBean.getProperty(argumentName, argumentType).orElse(null);
               if (prop == null) {
                  throw new UnsupportedOperationException(
                     "Cannot create copy of type ["
                        + declaringBean.getBeanType()
                        + "]. Constructor contains argument ["
                        + argumentName
                        + "] that is not a readable property"
                  );
               }

               if (prop == this) {
                  found = true;
                  values[i] = value;
               } else {
                  values[i] = prop.get(bean);
               }
            }

            if (found) {
               B newInstance = declaringBean.instantiate(values);

               for(BeanProperty<B, Object> beanProperty : declaringBean.getBeanProperties()) {
                  if (beanProperty != this && beanProperty.isReadWrite()) {
                     beanProperty.set(newInstance, beanProperty.get(bean));
                  }
               }

               return newInstance;
            } else {
               B newInstance = declaringBean.instantiate(values);

               for(BeanProperty<B, Object> beanProperty : declaringBean.getBeanProperties()) {
                  if (beanProperty == this && beanProperty.isReadWrite()) {
                     found = true;
                     beanProperty.set(newInstance, beanProperty.get(bean));
                  }
               }

               if (!found) {
                  throw new UnsupportedOperationException(
                     "Cannot mutate property ["
                        + this.getName()
                        + "] that is not mutable via a setter method or constructor argument for type: "
                        + declaringBean.getBeanType().getName()
                  );
               } else {
                  return newInstance;
               }
            }
         }
      } else {
         this.set(bean, value);
         return bean;
      }
   }

   default void set(@NonNull B bean, @Nullable T value) {
      if (this.isReadOnly()) {
         throw new UnsupportedOperationException("Cannot write read-only property: " + this.getName());
      } else {
         throw new UnsupportedOperationException("Write method unimplemented for property: " + this.getName());
      }
   }

   default void convertAndSet(@NonNull B bean, @Nullable Object value) {
      ArgumentUtils.requireNonNull("bean", bean);
      if (value != null) {
         Argument<T> argument = this.asArgument();
         ArgumentConversionContext<T> context = ConversionContext.of(argument);
         T converted = (T)ConversionService.SHARED
            .convert(value, context)
            .orElseThrow(
               () -> new ConversionErrorException(
                     argument,
                     (ConversionError)context.getLastError()
                        .orElse((ConversionError)() -> new IllegalArgumentException("Value [" + value + "] cannot be converted to type : " + this.getType()))
                  )
            );
         this.set(bean, converted);
      } else {
         this.set(bean, (T)value);
      }

   }

   @NonNull
   Class<T> getType();

   @NonNull
   @Override
   default Argument<T> asArgument() {
      return Argument.of(this.getType());
   }

   default boolean isReadOnly() {
      return false;
   }

   default boolean isWriteOnly() {
      return false;
   }

   default boolean isReadWrite() {
      return !this.isReadOnly() && !this.isWriteOnly();
   }

   default Class<B> getDeclaringType() {
      return this.getDeclaringBean().getBeanType();
   }
}
