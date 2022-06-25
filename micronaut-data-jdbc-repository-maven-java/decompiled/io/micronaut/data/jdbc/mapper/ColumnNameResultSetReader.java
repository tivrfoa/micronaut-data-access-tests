package io.micronaut.data.jdbc.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.mapper.ResultReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public final class ColumnNameResultSetReader implements ResultReader<ResultSet, String> {
   private final ConversionService<?> conversionService;

   public ColumnNameResultSetReader() {
      this(null);
   }

   public ColumnNameResultSetReader(DataConversionService<?> conversionService) {
      this.conversionService = (ConversionService<?>)(conversionService == null ? ConversionService.SHARED : conversionService);
   }

   @Override
   public ConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @Nullable
   public Object readDynamic(@NonNull ResultSet resultSet, @NonNull String index, @NonNull DataType dataType) {
      Object val = ResultReader.super.readDynamic(resultSet, index, dataType);

      try {
         return resultSet.wasNull() ? null : val;
      } catch (SQLException var6) {
         throw this.exceptionForColumn(index, var6);
      }
   }

   public boolean next(ResultSet resultSet) {
      try {
         return resultSet.next();
      } catch (SQLException var3) {
         throw new DataAccessException("Error calling next on SQL result set: " + var3.getMessage(), var3);
      }
   }

   @Override
   public <T> T convertRequired(@NonNull Object value, Class<T> type) {
      if (value == null) {
         throw new DataAccessException(
            "Cannot convert type null value to target type: " + type + ". Consider defining a TypeConverter bean to handle this case."
         );
      } else {
         Class wrapperType = ReflectionUtils.getWrapperType(type);
         return (T)(wrapperType.isInstance(value)
            ? value
            : this.conversionService
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
               ));
      }
   }

   public Date readTimestamp(ResultSet resultSet, String index) {
      try {
         return resultSet.getTimestamp(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public long readLong(ResultSet resultSet, String name) {
      try {
         return resultSet.getLong(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public char readChar(ResultSet resultSet, String name) {
      try {
         return resultSet.getString(name).charAt(0);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public Date readDate(ResultSet resultSet, String name) {
      try {
         return resultSet.getDate(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   @Nullable
   public String readString(ResultSet resultSet, String name) {
      try {
         return resultSet.getString(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public int readInt(ResultSet resultSet, String name) {
      try {
         return resultSet.getInt(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public boolean readBoolean(ResultSet resultSet, String name) {
      try {
         return resultSet.getBoolean(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public float readFloat(ResultSet resultSet, String name) {
      try {
         return resultSet.getFloat(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public byte readByte(ResultSet resultSet, String name) {
      try {
         return resultSet.getByte(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public short readShort(ResultSet resultSet, String name) {
      try {
         return resultSet.getShort(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public double readDouble(ResultSet resultSet, String name) {
      try {
         return resultSet.getDouble(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public BigDecimal readBigDecimal(ResultSet resultSet, String name) {
      try {
         return resultSet.getBigDecimal(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public byte[] readBytes(ResultSet resultSet, String name) {
      try {
         return resultSet.getBytes(name);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(name, var4);
      }
   }

   public <T> T getRequiredValue(ResultSet resultSet, String name, Class<T> type) throws DataAccessException {
      try {
         Object o;
         if (Blob.class.isAssignableFrom(type)) {
            o = resultSet.getBlob(name);
         } else if (Clob.class.isAssignableFrom(type)) {
            o = resultSet.getClob(name);
         } else {
            o = resultSet.getObject(name);
         }

         if (o == null) {
            return null;
         } else {
            return (T)(type.isInstance(o) ? o : this.convertRequired(o, type));
         }
      } catch (ConversionErrorException | SQLException var5) {
         throw this.exceptionForColumn(name, var5);
      }
   }

   private DataAccessException exceptionForColumn(String name, Exception e) {
      return new DataAccessException("Error reading object for name [" + name + "] from result set: " + e.getMessage(), e);
   }
}
