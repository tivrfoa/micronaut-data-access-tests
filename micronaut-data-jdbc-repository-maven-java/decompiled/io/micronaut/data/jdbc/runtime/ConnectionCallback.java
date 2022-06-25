package io.micronaut.data.jdbc.runtime;

import io.micronaut.core.annotation.NonNull;
import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallback<R> {
   @NonNull
   R call(@NonNull Connection connection) throws SQLException;
}
