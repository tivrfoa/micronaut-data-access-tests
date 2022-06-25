package io.micronaut.data.jdbc.runtime;

import io.micronaut.core.annotation.NonNull;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallback<R> {
   @NonNull
   R call(@NonNull PreparedStatement statement) throws SQLException;
}
