package org.flywaydb.core.api.callback;

public interface Warning {
   int getCode();

   String getState();

   String getMessage();

   boolean isHandled();

   void setHandled(boolean var1);
}
