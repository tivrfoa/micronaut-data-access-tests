package io.micronaut.http.netty.channel;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.netty.channel.kqueue.KQueue;

@Internal
public class KQueueAvailabilityCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      return KQueue.isAvailable();
   }
}
