package io.micronaut.data.runtime.convert;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;

public interface RuntimePersistentPropertyConversionContext extends ArgumentConversionContext<Object> {
   RuntimePersistentProperty<?> getRuntimePersistentProperty();

   @Override
   default Argument<Object> getArgument() {
      return this.getRuntimePersistentProperty().getArgument();
   }
}
