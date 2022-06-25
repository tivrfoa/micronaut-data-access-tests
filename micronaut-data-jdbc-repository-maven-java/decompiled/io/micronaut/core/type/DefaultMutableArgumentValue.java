package io.micronaut.core.type;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;

@Internal
class DefaultMutableArgumentValue<V> extends DefaultArgumentValue<V> implements MutableArgumentValue<V> {
   private V value;

   DefaultMutableArgumentValue(Argument<V> argument, V value) {
      super(argument, value);
      this.value = value;
   }

   @Override
   public void setValue(V value) {
      if (!this.getType().isInstance(value)) {
         this.value = value;
      } else {
         this.value = (V)ConversionService.SHARED
            .convert(value, this.getType())
            .orElseThrow(() -> new IllegalArgumentException("Invalid value [" + value + "] for argument: " + this));
      }

   }

   @Override
   public V getValue() {
      return this.value;
   }
}
