package io.micronaut.runtime.context.scope.refresh;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.CollectionUtils;
import java.util.Map;
import java.util.Set;

public interface RefreshEventListener extends ApplicationEventListener<RefreshEvent>, Ordered {
   int DEFAULT_POSITION = -2147483448;

   default boolean supports(RefreshEvent event) {
      if (event != null) {
         Map<String, Object> source = event.getSource();
         if (source != null) {
            if (source == RefreshEvent.ALL_KEYS) {
               return true;
            }

            Set<String> keys = source.keySet();
            Set<String> prefixes = this.getObservedConfigurationPrefixes();
            if (CollectionUtils.isNotEmpty(prefixes)) {
               for(String prefix : prefixes) {
                  if (keys.stream().anyMatch(k -> k.startsWith(prefix))) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @NonNull
   Set<String> getObservedConfigurationPrefixes();

   @Override
   default int getOrder() {
      return -2147483448;
   }
}
