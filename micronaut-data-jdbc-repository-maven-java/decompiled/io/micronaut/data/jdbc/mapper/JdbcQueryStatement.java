package io.micronaut.data.jdbc.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.mapper.QueryStatement;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public class JdbcQueryStatement implements QueryStatement<PreparedStatement, Integer> {
   private final ConversionService<?> conversionService;

   public JdbcQueryStatement() {
      this(null);
   }

   public JdbcQueryStatement(DataConversionService<?> conversionService) {
      this.conversionService = (ConversionService<?>)(conversionService == null ? ConversionService.SHARED : conversionService);
   }

   @Override
   public ConversionService<?> getConversionService() {
      return this.conversionService;
   }

   public QueryStatement<PreparedStatement, Integer> setDynamic(
      @NonNull PreparedStatement statement, @NonNull Integer index, @NonNull DataType dataType, Object value
   ) {
      if (value == null) {
         try {
            switch(dataType) {
               case ENTITY:
                  throw new IllegalStateException("Cannot set null value as ENTITY data type!");
               case LONG:
                  statement.setNull(index, -5);
                  return this;
               case STRING:
               case JSON:
                  statement.setNull(index, 12);
                  return this;
               case DATE:
                  statement.setNull(index, 91);
                  return this;
               case BOOLEAN:
                  statement.setNull(index, 16);
                  return this;
               case INTEGER:
                  statement.setNull(index, 4);
                  return this;
               case TIMESTAMP:
                  statement.setNull(index, 93);
                  return this;
               case OBJECT:
                  statement.setNull(index, 1111);
                  return this;
               case CHARACTER:
                  statement.setNull(index, 1);
                  return this;
               case DOUBLE:
                  statement.setNull(index, 8);
                  return this;
               case BYTE_ARRAY:
                  statement.setNull(index, -2);
                  return this;
               case FLOAT:
                  statement.setNull(index, 6);
                  return this;
               case BIGDECIMAL:
                  statement.setNull(index, 3);
                  return this;
               case BYTE:
                  statement.setNull(index, -7);
                  return this;
               case SHORT:
                  statement.setNull(index, -6);
                  return this;
               default:
                  if (dataType.isArray()) {
                     statement.setNull(index, 2003);
                  } else {
                     statement.setNull(index, 0);
                  }

                  return this;
            }
         } catch (SQLException var6) {
            throw new DataAccessException("Error setting JDBC null value: " + var6.getMessage(), var6);
         }
      } else {
         return QueryStatement.super.setDynamic(statement, index, dataType, value);
      }
   }

   public QueryStatement<PreparedStatement, Integer> setTimestamp(PreparedStatement statement, Integer name, Instant instant) {
      try {
         if (instant == null) {
            statement.setNull(name, 93);
         } else {
            statement.setTimestamp(name, Timestamp.from(instant));
         }

         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   public QueryStatement<PreparedStatement, Integer> setValue(PreparedStatement statement, Integer index, Object value) throws DataAccessException {
      try {
         if (value instanceof Clob) {
            statement.setClob(index, (Clob)value);
         } else if (value instanceof Blob) {
            statement.setBlob(index, (Blob)value);
         } else if (value instanceof Array) {
            statement.setArray(index, (Array)value);
         } else if (value != null) {
            if (value.getClass().isEnum()) {
               statement.setObject(index, value, 1111);
            } else {
               statement.setObject(index, value);
            }
         }

         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setLong(PreparedStatement statement, Integer name, long value) {
      try {
         statement.setLong(name, value);
         return this;
      } catch (SQLException var6) {
         throw this.newDataAccessException(var6);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setChar(PreparedStatement statement, Integer name, char value) {
      try {
         statement.setString(name, String.valueOf(value));
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setDate(PreparedStatement statement, Integer name, Date date) {
      try {
         if (date == null) {
            statement.setNull(name, 91);
         } else {
            statement.setDate(name, new java.sql.Date(date.getTime()));
         }

         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   public QueryStatement<PreparedStatement, Integer> setString(PreparedStatement statement, Integer name, String string) {
      try {
         if (string == null) {
            statement.setNull(name, 12);
         } else {
            statement.setString(name, string);
         }

         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setInt(PreparedStatement statement, Integer name, int integer) {
      try {
         statement.setInt(name, integer);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setBoolean(PreparedStatement statement, Integer name, boolean bool) {
      try {
         statement.setBoolean(name, bool);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setFloat(PreparedStatement statement, Integer name, float f) {
      try {
         statement.setFloat(name, f);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setByte(PreparedStatement statement, Integer name, byte b) {
      try {
         statement.setByte(name, b);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setShort(PreparedStatement statement, Integer name, short s) {
      try {
         statement.setShort(name, s);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setDouble(PreparedStatement statement, Integer name, double d) {
      try {
         statement.setDouble(name, d);
         return this;
      } catch (SQLException var6) {
         throw this.newDataAccessException(var6);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setBigDecimal(PreparedStatement statement, Integer name, BigDecimal bd) {
      try {
         statement.setBigDecimal(name, bd);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setBytes(PreparedStatement statement, Integer name, byte[] bytes) {
      try {
         statement.setBytes(name, bytes);
         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   @NonNull
   public QueryStatement<PreparedStatement, Integer> setArray(PreparedStatement statement, Integer name, Object array) {
      try {
         if (array == null) {
            statement.setNull(name, 2003);
         } else if (array instanceof Array) {
            statement.setArray(name, (Array)array);
         } else {
            statement.setObject(name, array);
         }

         return this;
      } catch (SQLException var5) {
         throw this.newDataAccessException(var5);
      }
   }

   private DataAccessException newDataAccessException(SQLException e) {
      return new DataAccessException("Unable to set PreparedStatement value: " + e.getMessage(), e);
   }
}
