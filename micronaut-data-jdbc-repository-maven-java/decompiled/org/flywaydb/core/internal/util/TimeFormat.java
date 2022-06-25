package org.flywaydb.core.internal.util;

public class TimeFormat {
   public static String format(long millis) {
      return String.format("%02d:%02d.%03ds", millis / 60000L, millis % 60000L / 1000L, millis % 1000L);
   }

   private TimeFormat() {
   }
}
