package org.flywaydb.core.internal.sqlscript;

public class Delimiter {
   public static final Delimiter SEMICOLON = new Delimiter(";", false);
   public static final Delimiter GO = new Delimiter("GO", true);
   private final String delimiter;
   private final boolean aloneOnLine;

   public Delimiter(String delimiter, boolean aloneOnLine) {
      this.delimiter = delimiter;
      this.aloneOnLine = aloneOnLine;
   }

   public boolean shouldBeAloneOnLine() {
      return this.aloneOnLine;
   }

   public String getEscape() {
      return null;
   }

   public String toString() {
      return (this.aloneOnLine ? "\n" : "") + this.delimiter;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Delimiter delimiter1 = (Delimiter)o;
         return this.aloneOnLine == delimiter1.aloneOnLine && this.delimiter.equals(delimiter1.delimiter);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.delimiter.hashCode();
      return 31 * result + (this.aloneOnLine ? 1 : 0);
   }

   public String getDelimiter() {
      return this.delimiter;
   }
}
