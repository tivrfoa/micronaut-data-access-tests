package io.micronaut.context.condition;

public class TrueCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      return true;
   }
}
