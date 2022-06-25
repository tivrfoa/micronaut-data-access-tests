package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;

@Internal
public enum PredicateBinaryOp {
   EQUALS,
   NOT_EQUALS,
   EQUALS_IGNORE_CASE,
   NOT_EQUALS_IGNORE_CASE,
   GREATER_THAN,
   GREATER_THAN_OR_EQUALS,
   LESS_THAN,
   LESS_THAN_OR_EQUALS,
   RLIKE,
   ILIKE,
   LIKE,
   REGEX,
   CONTAINS,
   STARTS_WITH,
   ENDS_WITH,
   STARTS_WITH_IGNORE_CASE,
   ENDS_WITH_IGNORE_CASE;

   @Nullable
   public PredicateBinaryOp negate() {
      switch(this) {
         case EQUALS:
            return NOT_EQUALS;
         case NOT_EQUALS:
            return EQUALS;
         default:
            return null;
      }
   }
}
