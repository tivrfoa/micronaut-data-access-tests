package org.flywaydb.core.internal.resolver.java;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;

public class JavaMigrationExecutor implements MigrationExecutor {
   private final JavaMigration javaMigration;

   @Override
   public void execute(Context context) throws SQLException {
      DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection());
      DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
      strategy.execute(() -> {
         this.executeOnce(context);
         return true;
      });
   }

   private void executeOnce(final Context context) throws SQLException {
      try {
         this.javaMigration.migrate(new org.flywaydb.core.api.migration.Context() {
            @Override
            public Configuration getConfiguration() {
               return context.getConfiguration();
            }

            @Override
            public Connection getConnection() {
               return context.getConnection();
            }
         });
      } catch (SQLException var3) {
         throw var3;
      } catch (Exception var4) {
         throw new FlywayException("Migration failed !", var4);
      }
   }

   @Override
   public boolean canExecuteInTransaction() {
      return this.javaMigration.canExecuteInTransaction();
   }

   @Override
   public boolean shouldExecute() {
      return true;
   }

   JavaMigrationExecutor(JavaMigration javaMigration) {
      this.javaMigration = javaMigration;
   }
}
