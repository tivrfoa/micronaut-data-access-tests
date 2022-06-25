package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Collection;
import java.util.Optional;

public interface BeanWrapper<T> extends AnnotationMetadataProvider {
   @NonNull
   BeanIntrospection<T> getIntrospection();

   @NonNull
   T getBean();

   @NonNull
   default String[] getPropertyNames() {
      return this.getIntrospection().getPropertyNames();
   }

   @NonNull
   default Collection<BeanProperty<T, Object>> getBeanProperties() {
      return this.getIntrospection().getBeanProperties();
   }

   @NonNull
   default <P> P getRequiredProperty(@NonNull String name, @NonNull Class<P> type) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("type", (T)type);
      Argument<P> argument = Argument.of(type);
      return this.getRequiredProperty(name, argument);
   }

   default <P> P getRequiredProperty(@NonNull String name, @NonNull Argument<P> argument) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("argument", argument);
      ArgumentConversionContext<P> context = ConversionContext.of(argument);
      return this.getRequiredProperty(name, context);
   }

   @NonNull
   default <P> P getRequiredProperty(@NonNull String name, @NonNull ArgumentConversionContext<P> context) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("type", context);
      return (P)this.getIntrospection()
         .getProperty(name)
         .map(
            prop -> {
               Optional<P> converted = prop.get(this.getBean(), context);
               return converted.orElseThrow(
                  () -> {
                     ConversionError conversionError = (ConversionError)context.getLastError()
                        .orElseGet(
                           () -> new ConversionError() {
                                 @Override
                                 public Exception getCause() {
                                    return new IntrospectionException(
                                       "Property of type [" + prop.getType() + "] cannot be converted to type: " + context.getArgument().getType()
                                    );
                                 }
               
                                 @Override
                                 public Optional<Object> getOriginalValue() {
                                    return Optional.ofNullable(prop.get(BeanWrapper.this.getBean()));
                                 }
                              }
                        );
                     return new ConversionErrorException(context.getArgument(), conversionError);
                  }
               );
            }
         )
         .orElseThrow(() -> new IntrospectionException("No property found for name: " + name));
   }

   @NonNull
   default <P> Optional<P> getProperty(@NonNull String name, @NonNull Class<P> type) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("type", (T)type);
      Argument<P> argument = Argument.of(type);
      return this.getProperty(name, argument);
   }

   default <P> Optional<P> getProperty(@NonNull String name, Argument<P> type) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("type", type);
      ArgumentConversionContext<P> context = ConversionContext.of(type);
      return this.getProperty(name, context);
   }

   default <P> Optional<P> getProperty(@NonNull String name, ArgumentConversionContext<P> context) {
      ArgumentUtils.requireNonNull("name", (T)name);
      ArgumentUtils.requireNonNull("context", context);
      return this.getIntrospection().getProperty(name).flatMap(prop -> prop.get(this.getBean(), context));
   }

   default BeanWrapper<T> setProperty(@NonNull String name, @Nullable Object value) {
      ArgumentUtils.requireNonNull("name", (T)name);
      this.getIntrospection().getProperty(name).ifPresent(prop -> prop.convertAndSet(this.getBean(), value));
      return this;
   }

   @NonNull
   static <T2> BeanWrapper<T2> getWrapper(@NonNull T2 bean) {
      ArgumentUtils.requireNonNull("bean", bean);
      Class<T2> aClass = bean.getClass();
      BeanIntrospection<T2> introspection = BeanIntrospection.getIntrospection(aClass);
      return new DefaultBeanWrapper<>(bean, introspection);
   }

   @NonNull
   static <T2> Optional<BeanWrapper<T2>> findWrapper(@NonNull T2 bean) {
      ArgumentUtils.requireNonNull("bean", bean);
      Class<T2> aClass = bean.getClass();
      return BeanIntrospector.SHARED.findIntrospection(aClass).map(i -> new DefaultBeanWrapper<>(bean, i));
   }

   @NonNull
   static <T2> Optional<BeanWrapper<T2>> findWrapper(Class<T2> type, @NonNull T2 bean) {
      ArgumentUtils.requireNonNull("type", (T)type);
      ArgumentUtils.requireNonNull("bean", bean);
      return BeanIntrospector.SHARED.findIntrospection(type).map(i -> new DefaultBeanWrapper<>(bean, i));
   }

   @NonNull
   static <T2> BeanWrapper<T2> getWrapper(Class<T2> type, @NonNull T2 bean) {
      ArgumentUtils.requireNonNull("type", (T)type);
      ArgumentUtils.requireNonNull("bean", bean);
      BeanIntrospection<T2> introspection = BeanIntrospection.getIntrospection(type);
      return new DefaultBeanWrapper<>(bean, introspection);
   }
}
