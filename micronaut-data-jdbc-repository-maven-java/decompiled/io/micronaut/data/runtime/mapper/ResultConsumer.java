package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.exceptions.DataAccessException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.BiConsumer;

@Introspected
@FunctionalInterface
public interface ResultConsumer<T, RS> extends BiConsumer<T, ResultConsumer.Context<RS>> {
   public interface Context<RS> {
      RS getResultSet();

      ResultReader<RS, String> getResultReader();

      @NonNull
      <E> E readEntity(@NonNull String prefix, @NonNull Class<E> type) throws DataAccessException;

      @NonNull
      <E, D> D readDTO(@NonNull String prefix, @NonNull Class<E> rootEntity, @NonNull Class<D> dtoType) throws DataAccessException;

      default long readLong(@NonNull String name) {
         return this.getResultReader().readLong(this.getResultSet(), name);
      }

      default char readChar(@NonNull String name) {
         return this.getResultReader().readChar(this.getResultSet(), name);
      }

      @Nullable
      default Date readDate(@NonNull String name) {
         return this.getResultReader().readDate(this.getResultSet(), name);
      }

      @Nullable
      default Date readTimestamp(@NonNull String name) {
         return this.getResultReader().readTimestamp(this.getResultSet(), name);
      }

      @Nullable
      default String readString(@NonNull String name) {
         return this.getResultReader().readString(this.getResultSet(), name);
      }

      default int readInt(@NonNull String name) {
         return this.getResultReader().readInt(this.getResultSet(), name);
      }

      default boolean readBoolean(@NonNull String name) {
         return this.getResultReader().readBoolean(this.getResultSet(), name);
      }

      default float readFloat(@NonNull String name) {
         return this.getResultReader().readFloat(this.getResultSet(), name);
      }

      default byte readByte(@NonNull String name) {
         return this.getResultReader().readByte(this.getResultSet(), name);
      }

      default short readShort(@NonNull String name) {
         return this.getResultReader().readShort(this.getResultSet(), name);
      }

      default double readDouble(@NonNull String name) {
         return this.getResultReader().readDouble(this.getResultSet(), name);
      }

      @Nullable
      default BigDecimal readBigDecimal(@NonNull String name) {
         return this.getResultReader().readBigDecimal(this.getResultSet(), name);
      }

      default byte[] readBytes(@NonNull String name) {
         return this.getResultReader().readBytes(this.getResultSet(), name);
      }
   }
}
