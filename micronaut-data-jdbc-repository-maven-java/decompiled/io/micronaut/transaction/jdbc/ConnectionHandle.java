package io.micronaut.transaction.jdbc;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionHandle {
   Connection getConnection();

   default void releaseConnection(Connection con) {
   }
}
