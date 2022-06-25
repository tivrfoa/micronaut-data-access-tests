package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.exceptions.DataAccessException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface BeanIntrospectionMapper<D, R> extends TypeMapper<D, R> {
   @NonNull
   @Override
   default R map(@NonNull D object, @NonNull Class<R> type) throws InstantiationException {
      ArgumentUtils.requireNonNull("resultSet", object);
      ArgumentUtils.requireNonNull("type", type);

      try {
         BeanIntrospection<R> introspection = BeanIntrospection.getIntrospection(type);
         Argument<?>[] arguments = introspection.getConstructorArguments();
         R instance;
         if (ArrayUtils.isEmpty(arguments)) {
            instance = introspection.instantiate();
         } else {
            Object[] args = new Object[arguments.length];

            for(int i = 0; i < arguments.length; ++i) {
               Argument<?> argument = arguments[i];
               Object o = this.read(object, argument);
               if (o == null) {
                  args[i] = o;
               } else if (argument.getType().isInstance(o)) {
                  args[i] = o;
               } else {
                  Object convertFrom;
                  if (Collection.class.isAssignableFrom(argument.getType()) && !(o instanceof Collection)) {
                     convertFrom = Collections.singleton(o);
                  } else {
                     convertFrom = o;
                  }

                  args[i] = this.convert(convertFrom, argument);
               }
            }

            instance = introspection.instantiate(args);
         }

         for(BeanProperty<R, Object> property : introspection.getBeanProperties()) {
            if (!property.isReadOnly()) {
               Object v = this.read(object, property.getName());
               if (v != null) {
                  if (property.getType().isInstance(v)) {
                     property.set(instance, v);
                  } else if (Iterable.class.isAssignableFrom(property.getType())) {
                     Object value = property.get(instance);
                     if (value instanceof Collection) {
                        ((Collection)value).add(v);
                     } else if (value instanceof Iterable) {
                        List list = new ArrayList(CollectionUtils.iterableToList((Iterable)value));
                        list.add(v);
                        property.set(instance, this.convert(list, property.asArgument()));
                     } else {
                        property.set(instance, this.convert(Collections.singleton(v), property.asArgument()));
                     }
                  } else {
                     property.set(instance, this.convert(v, property.asArgument()));
                  }
               }
            }
         }

         return instance;
      } catch (InstantiationException | IntrospectionException var12) {
         throw new DataAccessException("Error instantiating type [" + type.getName() + "] from introspection: " + var12.getMessage(), var12);
      }
   }

   default Object convert(Object value, Argument<?> argument) {
      if (value == null) {
         return null;
      } else {
         ConversionContext acc = ConversionContext.of(argument);
         Optional<?> result = this.getConversionService().convert(value, argument);
         if (!result.isPresent()) {
            Optional<ConversionError> lastError = acc.getLastError();
            if (lastError.isPresent()) {
               throw new ConversionErrorException(argument, (ConversionError)lastError.get());
            } else {
               throw new IllegalArgumentException("Cannot convert object type " + value.getClass() + " to required type: " + argument.getType());
            }
         } else {
            return result.get();
         }
      }
   }
}
