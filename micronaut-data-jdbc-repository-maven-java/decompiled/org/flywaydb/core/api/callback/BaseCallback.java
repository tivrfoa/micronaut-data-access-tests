package org.flywaydb.core.api.callback;

public abstract class BaseCallback implements Callback {
   @Override
   public boolean supports(Event event, Context context) {
      return true;
   }

   @Override
   public boolean canHandleInTransaction(Event event, Context context) {
      return true;
   }

   @Override
   public String getCallbackName() {
      String name = this.getClass().getSimpleName();
      if (name.contains("__")) {
         name = name.split("__")[1];
      }

      return name;
   }
}
