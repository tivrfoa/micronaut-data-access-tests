package io.micronaut.context.event;

import io.micronaut.core.annotation.Indexed;
import java.util.EventListener;

@Indexed(ApplicationEventListener.class)
@FunctionalInterface
public interface ApplicationEventListener<E> extends EventListener {
   void onApplicationEvent(E event);

   default boolean supports(E event) {
      return true;
   }
}
