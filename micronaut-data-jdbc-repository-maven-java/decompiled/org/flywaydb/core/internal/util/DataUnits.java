package org.flywaydb.core.internal.util;

public enum DataUnits {
   BYTE(1L, "B"),
   KILOBYTE(1024L, "kB"),
   MEGABYTE(1048576L, "MB"),
   GIGABYTE(1073741824L, "GB");

   private final long factor;
   private final String suffix;

   public long toBytes(long units) {
      return units * this.factor;
   }

   public long fromBytes(long bytes) {
      return bytes / this.factor;
   }

   public String toHumanReadableString(long bytes) {
      return this.fromBytes(bytes) + " " + this.suffix;
   }

   private DataUnits(long factor, String suffix) {
      this.factor = factor;
      this.suffix = suffix;
   }
}
