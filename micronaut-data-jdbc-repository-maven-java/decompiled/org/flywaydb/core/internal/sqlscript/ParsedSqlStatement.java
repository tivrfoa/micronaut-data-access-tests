package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

public class ParsedSqlStatement implements SqlStatement {
   private final int pos;
   private final int line;
   private final int col;
   private final String sql;
   private final Delimiter delimiter;
   private final boolean canExecuteInTransaction;

   public ParsedSqlStatement(int pos, int line, int col, String sql, Delimiter delimiter, boolean canExecuteInTransaction) {
      this.pos = pos;
      this.line = line;
      this.col = col;
      this.sql = sql;
      this.delimiter = delimiter;
      this.canExecuteInTransaction = canExecuteInTransaction;
   }

   @Override
   public final int getLineNumber() {
      return this.line;
   }

   @Override
   public String getDelimiter() {
      return this.delimiter.toString();
   }

   @Override
   public boolean canExecuteInTransaction() {
      return this.canExecuteInTransaction;
   }

   @Override
   public Results execute(JdbcTemplate jdbcTemplate) {
      return jdbcTemplate.executeStatement(this.sql);
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

   @Override
   public String getSql() {
      return this.sql;
   }
}
