package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public interface ResultReader<RS, IDX> {
   default <T> T convertRequired(@NonNull Object value, Class<T> type) {
      return (T)this.getConversionService()
         .convert(value, type)
         .orElseThrow(
            () -> new DataAccessException(
                  "Cannot convert type ["
                     + value.getClass()
                     + "] with value ["
                     + value
                     + "] to target type: "
                     + type
                     + ". Consider defining a TypeConverter bean to handle this case."
               )
         );
   }

   default <T> T convertRequired(@NonNull Object value, Argument<T> type) {
      return (T)this.getConversionService()
         .convert(value, type)
         .orElseThrow(
            () -> new DataAccessException(
                  "Cannot convert type ["
                     + value.getClass()
                     + "] with value ["
                     + value
                     + "] to target type: "
                     + type
                     + ". Consider defining a TypeConverter bean to handle this case."
               )
         );
   }

   @Nullable
   <T> T getRequiredValue(RS resultSet, IDX name, Class<T> type) throws DataAccessException;

   boolean next(RS resultSet);

   @Nullable
   default Object readDynamic(@NonNull RS resultSet, @NonNull IDX index, @NonNull DataType dataType) {
      switch(dataType) {
         case STRING:
         case JSON:
            return this.readString(resultSet, index);
         case UUID:
            return this.readUUID(resultSet, index);
         case LONG:
            return this.readLong(resultSet, index);
         case INTEGER:
            return this.readInt(resultSet, index);
         case BOOLEAN:
            return this.readBoolean(resultSet, index);
         case BYTE:
            return this.readByte(resultSet, index);
         case TIMESTAMP:
            return this.readTimestamp(resultSet, index);
         case DATE:
            return this.readDate(resultSet, index);
         case CHARACTER:
            return this.readChar(resultSet, index);
         case FLOAT:
            return this.readFloat(resultSet, index);
         case SHORT:
            return this.readShort(resultSet, index);
         case DOUBLE:
            return this.readDouble(resultSet, index);
         case BYTE_ARRAY:
            return this.readBytes(resultSet, index);
         case BIGDECIMAL:
            return this.readBigDecimal(resultSet, index);
         case OBJECT:
         default:
            return this.getRequiredValue(resultSet, index, Object.class);
      }
   }

   default long readLong(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Long.TYPE);
   }

   default char readChar(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Character.TYPE);
   }

   default Date readDate(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Date.class);
   }

   default Date readTimestamp(RS resultSet, IDX index) {
      return this.getRequiredValue(resultSet, index, Date.class);
   }

   @Nullable
   default String readString(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, String.class);
   }

   @Nullable
   default UUID readUUID(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, UUID.class);
   }

   default int readInt(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Integer.TYPE);
   }

   default boolean readBoolean(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Boolean.TYPE);
   }

   default float readFloat(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Float.TYPE);
   }

   default byte readByte(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Byte.TYPE);
   }

   default short readShort(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Short.TYPE);
   }

   default double readDouble(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, Double.TYPE);
   }

   default BigDecimal readBigDecimal(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, BigDecimal.class);
   }

   default byte[] readBytes(RS resultSet, IDX name) {
      return this.getRequiredValue(resultSet, name, byte[].class);
   }

   default ConversionService<?> getConversionService() {
      return ConversionService.SHARED;
   }
}
