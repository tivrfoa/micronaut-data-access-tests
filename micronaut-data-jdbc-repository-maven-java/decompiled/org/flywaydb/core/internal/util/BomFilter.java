package org.flywaydb.core.internal.util;

public class BomFilter {
   private static final char BOM = '\ufeff';

   public static boolean isBom(char c) {
      return c == '\ufeff';
   }

   public static String FilterBomFromString(String s) {
      if (s.isEmpty()) {
         return s;
      } else {
         return isBom(s.charAt(0)) ? s.substring(1) : s;
      }
   }
}
