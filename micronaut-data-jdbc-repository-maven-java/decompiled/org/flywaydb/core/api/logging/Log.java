package org.flywaydb.core.api.logging;

public interface Log {
   boolean isDebugEnabled();

   void debug(String var1);

   void info(String var1);

   void warn(String var1);

   void error(String var1);

   void error(String var1, Exception var2);

   void notice(String var1);
}
