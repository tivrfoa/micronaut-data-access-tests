package io.micronaut.core.bind;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultExecutableBinder<S> implements ExecutableBinder<S> {
   private final Map<Argument<?>, Object> preBound;

   public DefaultExecutableBinder() {
      this.preBound = Collections.emptyMap();
   }

   public DefaultExecutableBinder(@Nullable Map<Argument<?>, Object> preBound) {
      this.preBound = preBound == null ? Collections.emptyMap() : preBound;
   }

   @Override
   public <T, R> BoundExecutable<T, R> bind(Executable<T, R> target, ArgumentBinderRegistry<S> registry, S source) throws UnsatisfiedArgumentException {
      Argument[] arguments = target.getArguments();
      final Object[] boundArguments = new Object[arguments.length];

      for(int i = 0; i < arguments.length; ++i) {
         Argument<?> argument = arguments[i];
         if (this.preBound.containsKey(argument)) {
            boundArguments[i] = this.preBound.get(argument);
         } else {
            Optional<? extends ArgumentBinder<?, S>> argumentBinder = registry.findArgumentBinder(argument, source);
            if (!argumentBinder.isPresent()) {
               throw new UnsatisfiedArgumentException(argument);
            }

            ArgumentBinder<?, S> binder = (ArgumentBinder)argumentBinder.get();
            ArgumentConversionContext conversionContext = ConversionContext.of(argument);
            ArgumentBinder.BindingResult<?> bindingResult = binder.bind(conversionContext, source);
            if (!bindingResult.isPresentAndSatisfied()) {
               if (!argument.isNullable()) {
                  Optional<ConversionError> lastError = conversionContext.getLastError();
                  if (lastError.isPresent()) {
                     throw new ConversionErrorException(argument, (ConversionError)lastError.get());
                  }

                  throw new UnsatisfiedArgumentException(argument);
               }

               boundArguments[i] = null;
            } else {
               boundArguments[i] = bindingResult.get();
            }
         }
      }

      return new BoundExecutable<T, R>() {
         @Override
         public Executable<T, R> getTarget() {
            return target;
         }

         @Override
         public R invoke(T instance) {
            return target.invoke(instance, this.getBoundArguments());
         }

         @Override
         public Object[] getBoundArguments() {
            return boundArguments;
         }
      };
   }

   @Override
   public <T, R> BoundExecutable<T, R> tryBind(Executable<T, R> target, ArgumentBinderRegistry<S> registry, S source) {
      Argument[] arguments = target.getArguments();
      final Object[] boundArguments = new Object[arguments.length];
      final List<Argument<?>> unbound = new ArrayList(arguments.length);

      for(int i = 0; i < arguments.length; ++i) {
         Argument<?> argument = arguments[i];
         if (this.preBound.containsKey(argument)) {
            boundArguments[i] = this.preBound.get(argument);
         } else {
            Optional<? extends ArgumentBinder<?, S>> argumentBinder = registry.findArgumentBinder(argument, source);
            if (argumentBinder.isPresent()) {
               ArgumentBinder<?, S> binder = (ArgumentBinder)argumentBinder.get();
               ArgumentConversionContext conversionContext = ConversionContext.of(argument);
               ArgumentBinder.BindingResult<?> bindingResult = binder.bind(conversionContext, source);
               if (!bindingResult.isPresentAndSatisfied()) {
                  if (argument.isNullable()) {
                     boundArguments[i] = null;
                  } else {
                     boundArguments[i] = null;
                     unbound.add(argument);
                  }
               } else {
                  boundArguments[i] = bindingResult.get();
               }
            } else {
               boundArguments[i] = null;
               unbound.add(argument);
            }
         }
      }

      return new BoundExecutable<T, R>() {
         @Override
         public List<Argument<?>> getUnboundArguments() {
            return Collections.unmodifiableList(unbound);
         }

         @Override
         public Executable<T, R> getTarget() {
            return target;
         }

         @Override
         public R invoke(T instance) {
            return target.invoke(instance, this.getBoundArguments());
         }

         @Override
         public Object[] getBoundArguments() {
            return boundArguments;
         }
      };
   }
}
