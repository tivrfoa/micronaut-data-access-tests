package org.flywaydb.core.internal.parser;

import java.util.List;

class Statement {
   private final int pos;
   private final int line;
   private final int col;
   private final StatementType statementType;
   private final String sql;
   private final List<Token> tokens;

   Statement(int pos, int line, int col, StatementType statementType, String sql, List<Token> tokens) {
      this.pos = pos;
      this.line = line;
      this.col = col;
      this.statementType = statementType;
      this.sql = sql;
      this.tokens = tokens;
   }

   public int getPos() {
      return this.pos;
   }

   public int getLine() {
      return this.line;
   }

   public int getCol() {
      return this.col;
   }

   public StatementType getStatementType() {
      return this.statementType;
   }

   public String getSql() {
      return this.sql;
   }

   public List<Token> getTokens() {
      return this.tokens;
   }
}
