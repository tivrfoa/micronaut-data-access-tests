package io.micronaut.runtime.context.scope.refresh;

import io.micronaut.context.event.ApplicationEvent;
import java.util.Collections;
import java.util.Map;

public class RefreshEvent extends ApplicationEvent {
   static final Map<String, Object> ALL_KEYS = Collections.singletonMap("all", "*");

   public RefreshEvent(Map<String, Object> changes) {
      super(changes);
   }

   public RefreshEvent() {
      super(ALL_KEYS);
   }

   public Map<String, Object> getSource() {
      return (Map<String, Object>)super.getSource();
   }
}
