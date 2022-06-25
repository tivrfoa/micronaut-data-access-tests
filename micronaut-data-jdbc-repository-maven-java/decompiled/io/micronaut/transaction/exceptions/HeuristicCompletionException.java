package io.micronaut.transaction.exceptions;

import io.micronaut.core.annotation.NonNull;
import java.util.Locale;

public class HeuristicCompletionException extends TransactionException {
   private final HeuristicCompletionException.State outcomeState;

   public HeuristicCompletionException(@NonNull HeuristicCompletionException.State outcomeState, Throwable cause) {
      super("Heuristic completion: outcome state is " + outcomeState.toString(), cause);
      this.outcomeState = outcomeState;
   }

   @NonNull
   public HeuristicCompletionException.State getOutcomeState() {
      return this.outcomeState;
   }

   public static enum State {
      UNKNOWN,
      COMMITTED,
      ROLLED_BACK,
      MIXED;

      private final String str = this.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');

      public String toString() {
         return this.str;
      }
   }
}
