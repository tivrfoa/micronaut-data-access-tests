package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.Ordered;
import java.lang.annotation.Annotation;

@Indexed(PropertyAutoPopulator.class)
@Internal
@FunctionalInterface
public interface PropertyAutoPopulator<T extends Annotation> extends Ordered {
   @NonNull
   Object populate(RuntimePersistentProperty<?> property, @Nullable Object previousValue);
}
