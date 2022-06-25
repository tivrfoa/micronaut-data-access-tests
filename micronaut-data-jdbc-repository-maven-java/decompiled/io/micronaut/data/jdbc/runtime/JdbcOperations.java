package io.micronaut.data.jdbc.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.exceptions.DataAccessException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.stream.Stream;
import javax.sql.DataSource;

public interface JdbcOperations {
   @NonNull
   DataSource getDataSource();

   @NonNull
   Connection getConnection();

   @NonNull
   <R> R execute(@NonNull ConnectionCallback<R> callback);

   @NonNull
   <R> R prepareStatement(@NonNull String sql, @NonNull PreparedStatementCallback<R> callback);

   @NonNull
   <T> Stream<T> entityStream(@NonNull ResultSet resultSet, @Nullable String prefix, @NonNull Class<T> rootEntity);

   @NonNull
   <T> Stream<T> entityStream(@NonNull ResultSet resultSet, @NonNull Class<T> rootEntity);

   @NonNull
   <E> E readEntity(@NonNull String prefix, @NonNull ResultSet resultSet, @NonNull Class<E> type) throws DataAccessException;

   @NonNull
   default <E> E readEntity(@NonNull ResultSet resultSet, @NonNull Class<E> type) throws DataAccessException {
      return this.readEntity("", resultSet, type);
   }

   @NonNull
   <E, D> D readDTO(@NonNull String prefix, @NonNull ResultSet resultSet, @NonNull Class<E> rootEntity, @NonNull Class<D> dtoType) throws DataAccessException;

   @NonNull
   default <E, D> D readDTO(@NonNull ResultSet resultSet, @NonNull Class<E> rootEntity, @NonNull Class<D> dtoType) throws DataAccessException {
      return this.readDTO("", resultSet, rootEntity, dtoType);
   }
}
