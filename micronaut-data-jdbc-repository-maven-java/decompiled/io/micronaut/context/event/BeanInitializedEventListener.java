package io.micronaut.context.event;

import io.micronaut.core.annotation.Indexed;
import java.util.EventListener;

@Indexed(BeanInitializedEventListener.class)
@FunctionalInterface
public interface BeanInitializedEventListener<T> extends EventListener {
   T onInitialized(BeanInitializingEvent<T> event);
}
