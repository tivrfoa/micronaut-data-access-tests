package reactor.util;

public interface Logger {
   String getName();

   boolean isTraceEnabled();

   void trace(String var1);

   void trace(String var1, Object... var2);

   void trace(String var1, Throwable var2);

   boolean isDebugEnabled();

   void debug(String var1);

   void debug(String var1, Object... var2);

   void debug(String var1, Throwable var2);

   boolean isInfoEnabled();

   void info(String var1);

   void info(String var1, Object... var2);

   void info(String var1, Throwable var2);

   default void infoOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true));
      } else if (this.isInfoEnabled()) {
         this.info(messageSupplier.get(false));
      }

   }

   default void infoOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true), cause);
      } else if (this.isInfoEnabled()) {
         this.info(messageSupplier.get(false), cause);
      }

   }

   boolean isWarnEnabled();

   void warn(String var1);

   void warn(String var1, Object... var2);

   void warn(String var1, Throwable var2);

   default void warnOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true));
      } else if (this.isWarnEnabled()) {
         this.warn(messageSupplier.get(false));
      }

   }

   default void warnOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true), cause);
      } else if (this.isWarnEnabled()) {
         this.warn(messageSupplier.get(false), cause);
      }

   }

   boolean isErrorEnabled();

   void error(String var1);

   void error(String var1, Object... var2);

   void error(String var1, Throwable var2);

   default void errorOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true));
      } else if (this.isErrorEnabled()) {
         this.error(messageSupplier.get(false));
      }

   }

   default void errorOrDebug(Logger.ChoiceOfMessageSupplier messageSupplier, Throwable cause) {
      if (this.isDebugEnabled()) {
         this.debug(messageSupplier.get(true), cause);
      } else if (this.isErrorEnabled()) {
         this.error(messageSupplier.get(false), cause);
      }

   }

   @FunctionalInterface
   public interface ChoiceOfMessageSupplier {
      String get(boolean var1);
   }
}
