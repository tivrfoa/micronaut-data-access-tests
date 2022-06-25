package org.flywaydb.core.internal.database.postgresql;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;

public class PostgreSQLCopyParsedStatement extends ParsedSqlStatement {
   private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true);
   private final String copyData;

   public PostgreSQLCopyParsedStatement(int pos, int line, int col, String sql, String copyData) {
      super(pos, line, col, sql, COPY_DELIMITER, true);
      this.copyData = copyData;
   }

   @Override
   public Results execute(JdbcTemplate jdbcTemplate) {
      Object copyManager;
      Method copyManagerCopyInMethod;
      try {
         Connection connection = jdbcTemplate.getConnection();
         ClassLoader classLoader = connection.getClass().getClassLoader();
         Class<?> baseConnectionClass = classLoader.loadClass("org.postgresql.core.BaseConnection");
         Object baseConnection = connection.unwrap(baseConnectionClass);
         Class<?> copyManagerClass = classLoader.loadClass("org.postgresql.copy.CopyManager");
         Constructor<?> copyManagerConstructor = copyManagerClass.getConstructor(baseConnectionClass);
         copyManagerCopyInMethod = copyManagerClass.getMethod("copyIn", String.class, Reader.class);
         copyManager = copyManagerConstructor.newInstance(baseConnection);
      } catch (Exception var12) {
         throw new FlywayException("Unable to find PostgreSQL CopyManager class", var12);
      }

      Results results = new Results();

      try {
         try {
            Long updateCount = (Long)copyManagerCopyInMethod.invoke(copyManager, this.getSql(), new StringReader(this.copyData));
            results.addResult(new Result(updateCount, null, null, this.getSql()));
         } catch (InvocationTargetException | IllegalAccessException var10) {
            throw new SQLException("Unable to execute COPY operation", var10);
         }
      } catch (SQLException var11) {
         jdbcTemplate.extractErrors(results, var11);
      }

      return results;
   }
}
