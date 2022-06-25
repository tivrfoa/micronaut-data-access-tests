package org.flywaydb.core.api.callback;

public interface Callback {
   boolean supports(Event var1, Context var2);

   boolean canHandleInTransaction(Event var1, Context var2);

   void handle(Event var1, Context var2);

   String getCallbackName();
}
