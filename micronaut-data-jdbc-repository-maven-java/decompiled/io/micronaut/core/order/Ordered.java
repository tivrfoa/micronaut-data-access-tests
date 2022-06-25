package io.micronaut.core.order;

public interface Ordered {
   int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
   int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

   default int getOrder() {
      return 0;
   }
}
