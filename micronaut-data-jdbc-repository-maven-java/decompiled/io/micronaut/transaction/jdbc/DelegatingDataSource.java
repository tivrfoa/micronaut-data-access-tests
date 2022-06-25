package io.micronaut.transaction.jdbc;

import io.micronaut.core.annotation.NonNull;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DelegatingDataSource implements DataSource {
   @NonNull
   private final DataSource targetDataSource;

   public DelegatingDataSource(@NonNull DataSource targetDataSource) {
      Objects.requireNonNull(targetDataSource, "The target data source cannot be null");
      this.targetDataSource = targetDataSource;
   }

   @NonNull
   public final DataSource getTargetDataSource() {
      return this.targetDataSource;
   }

   public Connection getConnection() throws SQLException {
      return this.getTargetDataSource().getConnection();
   }

   public Connection getConnection(String username, String password) throws SQLException {
      return this.getTargetDataSource().getConnection(username, password);
   }

   public PrintWriter getLogWriter() throws SQLException {
      return this.getTargetDataSource().getLogWriter();
   }

   public void setLogWriter(PrintWriter out) throws SQLException {
      this.getTargetDataSource().setLogWriter(out);
   }

   public int getLoginTimeout() throws SQLException {
      return this.getTargetDataSource().getLoginTimeout();
   }

   public void setLoginTimeout(int seconds) throws SQLException {
      this.getTargetDataSource().setLoginTimeout(seconds);
   }

   public <T> T unwrap(Class<T> iface) throws SQLException {
      return (T)(iface.isInstance(this) ? this : this.getTargetDataSource().unwrap(iface));
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return iface.isInstance(this) || this.getTargetDataSource().isWrapperFor(iface);
   }

   public Logger getParentLogger() {
      return Logger.getLogger("global");
   }

   @NonNull
   public static DataSource unwrapDataSource(@NonNull DataSource dataSource) {
      while(dataSource instanceof DelegatingDataSource) {
         dataSource = ((DelegatingDataSource)dataSource).getTargetDataSource();
      }

      return dataSource;
   }
}
