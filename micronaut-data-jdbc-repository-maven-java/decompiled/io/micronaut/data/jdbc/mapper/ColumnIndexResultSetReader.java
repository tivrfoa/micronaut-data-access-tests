package io.micronaut.data.jdbc.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.mapper.ResultReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public final class ColumnIndexResultSetReader implements ResultReader<ResultSet, Integer> {
   private final ConversionService<?> conversionService;

   @Deprecated
   public ColumnIndexResultSetReader() {
      this(null);
   }

   public ColumnIndexResultSetReader(DataConversionService<?> conversionService) {
      this.conversionService = (ConversionService<?>)(conversionService == null ? ConversionService.SHARED : conversionService);
   }

   @Override
   public ConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @Nullable
   public Object readDynamic(@NonNull ResultSet resultSet, @NonNull Integer index, @NonNull DataType dataType) {
      Object val = ResultReader.super.readDynamic(resultSet, index, dataType);

      try {
         return resultSet.wasNull() ? null : val;
      } catch (SQLException var6) {
         throw this.exceptionForColumn(index, var6);
      }
   }

   public Timestamp readTimestamp(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getTimestamp(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public long readLong(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getLong(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public char readChar(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getString(index).charAt(0);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public Date readDate(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getDate(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   @Nullable
   public String readString(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getString(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public int readInt(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getInt(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public boolean readBoolean(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getBoolean(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public float readFloat(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getFloat(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public byte readByte(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getByte(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public short readShort(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getShort(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public double readDouble(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getDouble(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public BigDecimal readBigDecimal(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getBigDecimal(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public byte[] readBytes(ResultSet resultSet, Integer index) {
      try {
         return resultSet.getBytes(index);
      } catch (SQLException var4) {
         throw this.exceptionForColumn(index, var4);
      }
   }

   public <T> T getRequiredValue(ResultSet resultSet, Integer index, Class<T> type) throws DataAccessException {
      try {
         Object o;
         if (Blob.class.isAssignableFrom(type)) {
            o = resultSet.getBlob(index);
         } else if (Clob.class.isAssignableFrom(type)) {
            o = resultSet.getClob(index);
         } else {
            o = resultSet.getObject(index);
         }

         if (o == null) {
            return null;
         } else {
            return (T)(type.isInstance(o) ? o : this.convertRequired(o, type));
         }
      } catch (ConversionErrorException | SQLException var5) {
         throw this.exceptionForColumn(index, var5);
      }
   }

   public boolean next(ResultSet resultSet) {
      try {
         return resultSet.next();
      } catch (SQLException var3) {
         throw new DataAccessException("Error calling next on SQL result set: " + var3.getMessage(), var3);
      }
   }

   private DataAccessException exceptionForColumn(Integer index, Exception e) {
      return new DataAccessException("Error reading object for index [" + index + "] from result set: " + e.getMessage(), e);
   }
}
