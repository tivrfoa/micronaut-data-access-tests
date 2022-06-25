package io.micronaut.context.event;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import java.util.EventListener;

@Indexed(BeanCreatedEventListener.class)
@FunctionalInterface
public interface BeanCreatedEventListener<T> extends EventListener {
   T onCreated(@NonNull BeanCreatedEvent<T> event);
}
