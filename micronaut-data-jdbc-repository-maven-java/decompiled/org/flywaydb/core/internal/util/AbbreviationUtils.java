package org.flywaydb.core.internal.util;

public class AbbreviationUtils {
   public static String abbreviateDescription(String description) {
      if (description == null) {
         return null;
      } else {
         return description.length() <= 200 ? description : description.substring(0, 197) + "...";
      }
   }

   public static String abbreviateScript(String script) {
      if (script == null) {
         return null;
      } else {
         return script.length() <= 1000 ? script : "..." + script.substring(3, 1000);
      }
   }

   private AbbreviationUtils() {
   }
}
