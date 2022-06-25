package org.flywaydb.core.internal.callback;

import java.util.List;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Statement;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.database.base.Connection;

public class SimpleContext implements Context {
   private final Configuration configuration;
   private final Connection connection;
   private final MigrationInfo migrationInfo;
   private final Statement statement;
   private final OperationResult operationResult;

   public SimpleContext(Configuration configuration) {
      this.configuration = configuration;
      this.connection = null;
      this.migrationInfo = null;
      this.statement = null;
      this.operationResult = null;
   }

   public SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo, OperationResult operationResult) {
      this.configuration = configuration;
      this.connection = connection;
      this.migrationInfo = migrationInfo;
      this.statement = null;
      this.operationResult = operationResult;
   }

   public SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo, String sql, List<Warning> warnings, List<Error> errors) {
      this.configuration = configuration;
      this.connection = connection;
      this.migrationInfo = migrationInfo;
      this.statement = new SimpleContext.SimpleStatement(sql, warnings, errors);
      this.operationResult = null;
   }

   @Override
   public java.sql.Connection getConnection() {
      return this.connection == null ? null : this.connection.getJdbcConnection();
   }

   @Override
   public Configuration getConfiguration() {
      return this.configuration;
   }

   @Override
   public MigrationInfo getMigrationInfo() {
      return this.migrationInfo;
   }

   @Override
   public Statement getStatement() {
      return this.statement;
   }

   @Override
   public OperationResult getOperationResult() {
      return this.operationResult;
   }

   private static class SimpleStatement implements Statement {
      private final String sql;
      private final List<Warning> warnings;
      private final List<Error> errors;

      private SimpleStatement(String sql, List<Warning> warnings, List<Error> errors) {
         this.sql = sql;
         this.warnings = warnings;
         this.errors = errors;
      }

      @Override
      public String getSql() {
         return this.sql;
      }

      @Override
      public List<Warning> getWarnings() {
         return this.warnings;
      }

      @Override
      public List<Error> getErrors() {
         return this.errors;
      }
   }
}
