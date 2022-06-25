package io.micronaut.inject.visitor;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.order.OrderUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class BeanElementVisitorLoader {
   @NonNull
   static List<BeanElementVisitor<?>> load() {
      List<BeanElementVisitor<?>> visitors = new ArrayList(10);

      for(ServiceDefinition<BeanElementVisitor> definition : SoftServiceLoader.load(BeanElementVisitor.class)) {
         if (definition.isPresent()) {
            try {
               BeanElementVisitor<?> visitor = definition.load();
               if (visitor.isEnabled()) {
                  visitors.add(visitor);
               }
            } catch (Exception var5) {
            }
         }
      }

      if (visitors.isEmpty()) {
         return Collections.emptyList();
      } else {
         OrderUtil.sort(visitors);
         return Collections.unmodifiableList(visitors);
      }
   }
}
