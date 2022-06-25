package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;

public interface ApplicationContextConfigurer extends Ordered {
   ApplicationContextConfigurer NO_OP = new ApplicationContextConfigurer() {
   };

   default void configure(@NonNull ApplicationContextBuilder builder) {
   }
}
