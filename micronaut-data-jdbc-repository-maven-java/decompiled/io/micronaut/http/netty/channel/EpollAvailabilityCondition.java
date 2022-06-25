package io.micronaut.http.netty.channel;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.netty.channel.epoll.Epoll;

@Internal
public class EpollAvailabilityCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      return Epoll.isAvailable();
   }
}
