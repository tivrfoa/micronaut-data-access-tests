package io.micronaut.core.attr;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.util.StringUtils;
import java.util.Optional;

public interface MutableAttributeHolder extends AttributeHolder {
   @NonNull
   MutableConvertibleValues<Object> getAttributes();

   @NonNull
   default MutableAttributeHolder setAttribute(@NonNull CharSequence name, @Nullable Object value) {
      if (StringUtils.isNotEmpty(name)) {
         if (value == null) {
            this.getAttributes().remove(name.toString());
         } else {
            this.getAttributes().put(name.toString(), value);
         }
      }

      return this;
   }

   @NonNull
   default <T> Optional<T> removeAttribute(@NonNull CharSequence name, @NonNull Class<T> type) {
      if (StringUtils.isNotEmpty(name)) {
         String key = name.toString();
         Optional<T> value = this.getAttribute(key, type);
         value.ifPresent(o -> this.getAttributes().remove(key));
         return value;
      } else {
         return Optional.empty();
      }
   }
}
