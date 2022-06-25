package org.flywaydb.core.api.callback;

public interface Error {
   int getCode();

   String getState();

   String getMessage();

   boolean isHandled();

   void setHandled(boolean var1);
}
