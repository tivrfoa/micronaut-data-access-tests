package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;

@Internal
public enum PredicateUnaryOp {
   IS_NULL,
   IS_NON_NULL,
   IS_TRUE,
   IS_FALSE,
   IS_EMPTY,
   IS_NOT_EMPTY;

   @Nullable
   public PredicateUnaryOp negate() {
      switch(this) {
         case IS_NULL:
            return IS_NON_NULL;
         case IS_NON_NULL:
            return IS_NULL;
         case IS_TRUE:
            return IS_FALSE;
         case IS_FALSE:
            return IS_TRUE;
         case IS_EMPTY:
            return IS_NOT_EMPTY;
         case IS_NOT_EMPTY:
            return IS_EMPTY;
         default:
            return null;
      }
   }
}
