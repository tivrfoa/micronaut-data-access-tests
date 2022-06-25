package io.micronaut.context.event;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import java.util.EventListener;

@Indexed(BeanDestroyedEventListener.class)
@FunctionalInterface
public interface BeanDestroyedEventListener<T> extends EventListener {
   void onDestroyed(@NonNull BeanDestroyedEvent<T> event);
}
