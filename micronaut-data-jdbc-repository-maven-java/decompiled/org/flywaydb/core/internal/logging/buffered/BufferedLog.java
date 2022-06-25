package org.flywaydb.core.internal.logging.buffered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.logging.Log;

public class BufferedLog implements Log {
   public final List<BufferedLog.BufferedLogMessage> bufferedLogMessages = Collections.synchronizedList(new ArrayList());

   @Override
   public boolean isDebugEnabled() {
      return true;
   }

   @Override
   public void debug(String message) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.DEBUG));
   }

   @Override
   public void info(String message) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.INFO));
   }

   @Override
   public void warn(String message) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.WARN));
   }

   @Override
   public void error(String message) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.ERROR));
   }

   @Override
   public void error(String message, Exception e) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.ERROR, e));
   }

   @Override
   public void notice(String message) {
      this.bufferedLogMessages.add(new BufferedLog.BufferedLogMessage(message, BufferedLog.Level.NOTICE));
   }

   public void flush(Log target) {
      synchronized(this.bufferedLogMessages) {
         for(BufferedLog.BufferedLogMessage message : this.bufferedLogMessages) {
            switch(message.level) {
               case DEBUG:
                  target.debug(message.message);
                  break;
               case INFO:
                  target.info(message.message);
                  break;
               case WARN:
                  target.warn(message.message);
                  break;
               case NOTICE:
                  target.notice(message.message);
                  break;
               case ERROR:
                  if (message.e == null) {
                     target.error(message.message);
                  } else {
                     target.error(message.message, message.e);
                  }
            }
         }

         this.bufferedLogMessages.clear();
      }
   }

   public static class BufferedLogMessage {
      public final String message;
      public final BufferedLog.Level level;
      public final Exception e;

      public BufferedLogMessage(String message, BufferedLog.Level level) {
         this(message, level, null);
      }

      public BufferedLogMessage(String message, BufferedLog.Level level, Exception e) {
         this.message = message;
         this.level = level;
         this.e = e;
      }
   }

   public static enum Level {
      DEBUG,
      INFO,
      WARN,
      ERROR,
      NOTICE;
   }
}
