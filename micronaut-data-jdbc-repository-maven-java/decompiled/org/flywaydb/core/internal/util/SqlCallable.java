package org.flywaydb.core.internal.util;

import java.sql.SQLException;

public interface SqlCallable<V> {
   V call() throws SQLException;
}
