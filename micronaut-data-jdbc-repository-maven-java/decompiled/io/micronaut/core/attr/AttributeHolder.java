package io.micronaut.core.attr;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.StringUtils;
import java.util.Optional;

public interface AttributeHolder {
   @NonNull
   ConvertibleValues<Object> getAttributes();

   @NonNull
   default Optional<Object> getAttribute(CharSequence name) {
      return StringUtils.isNotEmpty(name) ? this.getAttributes().get(name.toString(), Object.class) : Optional.empty();
   }

   @NonNull
   default <T> Optional<T> getAttribute(CharSequence name, Class<T> type) {
      return StringUtils.isNotEmpty(name) ? this.getAttributes().get(name.toString(), type) : Optional.empty();
   }
}
