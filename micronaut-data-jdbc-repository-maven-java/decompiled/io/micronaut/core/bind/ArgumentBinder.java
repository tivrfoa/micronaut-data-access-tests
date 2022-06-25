package io.micronaut.core.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface ArgumentBinder<T, S> {
   ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, S source);

   public interface BindingResult<T> {
      ArgumentBinder.BindingResult EMPTY = Optional::empty;
      ArgumentBinder.BindingResult UNSATISFIED = new ArgumentBinder.BindingResult() {
         @Override
         public Optional getValue() {
            return Optional.empty();
         }

         @Override
         public boolean isSatisfied() {
            return false;
         }
      };

      Optional<T> getValue();

      default List<ConversionError> getConversionErrors() {
         return Collections.emptyList();
      }

      default boolean isSatisfied() {
         return this.getConversionErrors() == Collections.EMPTY_LIST;
      }

      default boolean isPresentAndSatisfied() {
         return this.isSatisfied() && this.getValue().isPresent();
      }

      default T get() {
         return (T)this.getValue().get();
      }
   }
}
