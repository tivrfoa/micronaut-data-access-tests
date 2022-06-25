package io.micronaut.transaction.jdbc;

import java.sql.Connection;
import java.util.Objects;

public class SimpleConnectionHandle implements ConnectionHandle {
   private final Connection connection;

   public SimpleConnectionHandle(Connection connection) {
      Objects.requireNonNull(connection, "Connection must not be null");
      this.connection = connection;
   }

   @Override
   public Connection getConnection() {
      return this.connection;
   }

   public String toString() {
      return "SimpleConnectionHandle: " + this.connection;
   }
}
