package io.micronaut.context.condition;

import io.micronaut.core.annotation.Introspected;
import java.util.function.Predicate;

@FunctionalInterface
@Introspected
public interface Condition extends Predicate<ConditionContext> {
   boolean matches(ConditionContext context);

   default boolean test(ConditionContext condition) {
      return this.matches(condition);
   }
}
