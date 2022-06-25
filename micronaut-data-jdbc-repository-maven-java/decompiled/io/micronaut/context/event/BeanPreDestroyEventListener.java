package io.micronaut.context.event;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import java.util.EventListener;

@Indexed(BeanPreDestroyEventListener.class)
@FunctionalInterface
public interface BeanPreDestroyEventListener<T> extends EventListener {
   @NonNull
   T onPreDestroy(@NonNull BeanPreDestroyEvent<T> event);
}
