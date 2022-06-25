package io.micronaut.core.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public interface UnsafeBeanProperty<B, T> extends BeanProperty<B, T> {
   T getUnsafe(@NonNull B bean);

   @NonNull
   B withValueUnsafe(@NonNull B bean, @Nullable T value);

   void setUnsafe(@NonNull B bean, @Nullable T value);
}
