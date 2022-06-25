package org.flywaydb.core.internal.parser;

public class StatementType {
   public static final StatementType GENERIC = new StatementType();
   public static final StatementType UNKNOWN = new StatementType();

   public boolean treatAsIfLetter(char c) {
      return false;
   }
}
