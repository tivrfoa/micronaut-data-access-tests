package com.mysql.cj;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class NativeQueryBindings implements QueryBindings {
   private Session session;
   private BindValue[] bindValues;
   private int numberOfExecutions = 0;
   private boolean isLoadDataQuery = false;
   private ColumnDefinition columnDefinition;
   private AtomicBoolean sendTypesToServer = new AtomicBoolean(false);
   private Function<Session, BindValue> bindValueConstructor;
   private boolean longParameterSwitchDetected = false;
   static Map<Class<?>, MysqlType> DEFAULT_MYSQL_TYPES = new HashMap();

   public NativeQueryBindings(int parameterCount, Session sess, Function<Session, BindValue> bindValueConstructor) {
      this.session = sess;
      this.bindValueConstructor = bindValueConstructor;
      this.bindValues = new BindValue[parameterCount];

      for(int i = 0; i < parameterCount; ++i) {
         this.bindValues[i] = (BindValue)bindValueConstructor.apply(this.session);
      }

   }

   @Override
   public QueryBindings clone() {
      NativeQueryBindings newBindings = new NativeQueryBindings(this.bindValues.length, this.session, this.bindValueConstructor);
      BindValue[] bvs = new BindValue[this.bindValues.length];

      for(int i = 0; i < this.bindValues.length; ++i) {
         bvs[i] = this.bindValues[i].clone();
      }

      newBindings.setBindValues(bvs);
      newBindings.isLoadDataQuery = this.isLoadDataQuery;
      newBindings.sendTypesToServer.set(this.sendTypesToServer.get());
      newBindings.setLongParameterSwitchDetected(this.isLongParameterSwitchDetected());
      return newBindings;
   }

   @Override
   public void setColumnDefinition(ColumnDefinition colDef) {
      this.columnDefinition = colDef;
   }

   @Override
   public BindValue[] getBindValues() {
      return this.bindValues;
   }

   @Override
   public void setBindValues(BindValue[] bindValues) {
      this.bindValues = bindValues;
   }

   @Override
   public boolean clearBindValues() {
      boolean hadLongData = false;
      if (this.bindValues != null) {
         for(int i = 0; i < this.bindValues.length; ++i) {
            if (this.bindValues[i] != null && this.bindValues[i].isStream()) {
               hadLongData = true;
            }

            this.bindValues[i].reset();
         }
      }

      return hadLongData;
   }

   @Override
   public void checkParameterSet(int columnIndex) {
      if (!this.bindValues[columnIndex].isSet()) {
         throw ExceptionFactory.createException(
            Messages.getString("PreparedStatement.40") + (columnIndex + 1), "07001", 0, true, null, this.session.getExceptionInterceptor()
         );
      }
   }

   @Override
   public void checkAllParametersSet() {
      for(int i = 0; i < this.bindValues.length; ++i) {
         this.checkParameterSet(i);
      }

   }

   @Override
   public int getNumberOfExecutions() {
      return this.numberOfExecutions;
   }

   @Override
   public void setNumberOfExecutions(int numberOfExecutions) {
      this.numberOfExecutions = numberOfExecutions;
   }

   @Override
   public boolean isLongParameterSwitchDetected() {
      return this.longParameterSwitchDetected;
   }

   @Override
   public void setLongParameterSwitchDetected(boolean longParameterSwitchDetected) {
      this.longParameterSwitchDetected = longParameterSwitchDetected;
   }

   @Override
   public AtomicBoolean getSendTypesToServer() {
      return this.sendTypesToServer;
   }

   @Override
   public BindValue getBinding(int parameterIndex, boolean forLongData) {
      if (this.bindValues[parameterIndex] != null && this.bindValues[parameterIndex].isStream() && !forLongData) {
         this.longParameterSwitchDetected = true;
      }

      return this.bindValues[parameterIndex];
   }

   @Override
   public void setFromBindValue(int parameterIndex, BindValue bv) {
      BindValue binding = this.getBinding(parameterIndex, false);
      binding.setBinding(bv.getValue(), bv.getMysqlType(), this.numberOfExecutions, this.sendTypesToServer);
      binding.setKeepOrigNanos(bv.keepOrigNanos());
      binding.setCalendar(bv.getCalendar());
      binding.setEscapeBytesIfNeeded(bv.escapeBytesIfNeeded());
      binding.setIsNational(bv.isNational());
      binding.setField(bv.getField());
      binding.setScaleOrLength(bv.getScaleOrLength());
   }

   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, int length) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, true);
         binding.setBinding(x, MysqlType.TEXT, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength((long)length);
      }
   }

   @Override
   public void setBigDecimal(int parameterIndex, BigDecimal x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         this.getBinding(parameterIndex, false).setBinding(x, MysqlType.DECIMAL, this.numberOfExecutions, this.sendTypesToServer);
      }
   }

   @Override
   public void setBigInteger(int parameterIndex, BigInteger x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         this.getBinding(parameterIndex, false).setBinding(x, MysqlType.BIGINT_UNSIGNED, this.numberOfExecutions, this.sendTypesToServer);
      }
   }

   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, int length) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, true);
         binding.setBinding(x, MysqlType.BLOB, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength((long)length);
      }
   }

   @Override
   public void setBlob(int parameterIndex, Blob x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.BLOB, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength(-1L);
      }
   }

   @Override
   public void setBoolean(int parameterIndex, boolean x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.BOOLEAN, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setByte(int parameterIndex, byte x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.TINYINT, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setBytes(int parameterIndex, byte[] x, boolean escapeIfNeeded) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.BINARY, this.numberOfExecutions, this.sendTypesToServer);
         binding.setEscapeBytesIfNeeded(escapeIfNeeded);
      }
   }

   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, int length) {
      if (reader == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, true);
         binding.setBinding(reader, MysqlType.TEXT, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength((long)length);
      }
   }

   @Override
   public void setClob(int parameterIndex, Clob x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.TEXT, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength(-1L);
      }
   }

   @Override
   public void setDate(int parameterIndex, Date x, Calendar cal) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.DATE, this.numberOfExecutions, this.sendTypesToServer);
         binding.setCalendar(cal == null ? null : (Calendar)cal.clone());
      }
   }

   @Override
   public void setDouble(int parameterIndex, double x) {
      if (this.session.getPropertySet().getBooleanProperty(PropertyKey.allowNanAndInf).getValue()
         || x != Double.POSITIVE_INFINITY && x != Double.NEGATIVE_INFINITY && !Double.isNaN(x)) {
         this.getBinding(parameterIndex, false).setBinding(x, MysqlType.DOUBLE, this.numberOfExecutions, this.sendTypesToServer);
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("PreparedStatement.64", new Object[]{x}), this.session.getExceptionInterceptor()
         );
      }
   }

   @Override
   public void setFloat(int parameterIndex, float x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.FLOAT, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setInt(int parameterIndex, int x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.INT, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setLong(int parameterIndex, long x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.BIGINT, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setNCharacterStream(int parameterIndex, Reader reader, long length) {
      if (reader == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, true);
         binding.setBinding(reader, MysqlType.TEXT, this.numberOfExecutions, this.sendTypesToServer);
         binding.setScaleOrLength(length);
         binding.setIsNational(true);
      }
   }

   @Override
   public void setNClob(int parameterIndex, NClob value) {
      if (value == null) {
         this.setNull(parameterIndex);
      } else {
         try {
            this.setNCharacterStream(parameterIndex, value.getCharacterStream(), value.length());
         } catch (Throwable var4) {
            throw ExceptionFactory.createException(var4.getMessage(), var4, this.session.getExceptionInterceptor());
         }
      }
   }

   @Override
   public void setNString(int parameterIndex, String x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.VARCHAR, this.numberOfExecutions, this.sendTypesToServer);
         binding.setIsNational(true);
      }
   }

   @Override
   public synchronized void setNull(int parameterIndex) {
      BindValue binding = this.getBinding(parameterIndex, false);
      binding.setBinding(null, MysqlType.NULL, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public boolean isNull(int parameterIndex) {
      return this.bindValues[parameterIndex].isNull();
   }

   @Override
   public void setShort(int parameterIndex, short x) {
      this.getBinding(parameterIndex, false).setBinding(x, MysqlType.SMALLINT, this.numberOfExecutions, this.sendTypesToServer);
   }

   @Override
   public void setString(int parameterIndex, String x) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         this.getBinding(parameterIndex, false).setBinding(x, MysqlType.VARCHAR, this.numberOfExecutions, this.sendTypesToServer);
      }
   }

   @Override
   public void setTime(int parameterIndex, Time x, Calendar cal) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         BindValue binding = this.getBinding(parameterIndex, false);
         binding.setBinding(x, MysqlType.TIME, this.numberOfExecutions, this.sendTypesToServer);
         binding.setCalendar(cal == null ? null : (Calendar)cal.clone());
      }
   }

   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar targetCalendar, Field field, MysqlType targetMysqlType) {
      if (x == null) {
         this.setNull(parameterIndex);
      } else {
         if (field == null
            && this.columnDefinition != null
            && parameterIndex <= this.columnDefinition.getFields().length
            && parameterIndex >= 0
            && this.columnDefinition.getFields()[parameterIndex].getDecimals() > 0) {
            field = this.columnDefinition.getFields()[parameterIndex];
         }

         BindValue binding = this.getBinding(parameterIndex, false);
         if (field == null) {
            binding.setField(field);
         }

         binding.setBinding(x, targetMysqlType, this.numberOfExecutions, this.sendTypesToServer);
         binding.setCalendar(targetCalendar == null ? null : (Calendar)targetCalendar.clone());
      }
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj) {
      if (parameterObj == null) {
         this.setNull(parameterIndex);
      } else {
         MysqlType defaultMysqlType = (MysqlType)DEFAULT_MYSQL_TYPES.get(parameterObj.getClass());
         if (defaultMysqlType == null) {
            Optional<MysqlType> mysqlType = DEFAULT_MYSQL_TYPES.entrySet()
               .stream()
               .filter(m -> ((Class)m.getKey()).isAssignableFrom(parameterObj.getClass()))
               .map(m -> (MysqlType)m.getValue())
               .findFirst();
            if (mysqlType.isPresent()) {
               defaultMysqlType = (MysqlType)mysqlType.get();
            }
         }

         this.setObject(parameterIndex, parameterObj, defaultMysqlType, -1);
      }
   }

   @Override
   public void setObject(int parameterIndex, Object parameterObj, MysqlType targetMysqlType, int scaleOrLength) {
      if (parameterObj == null) {
         this.setNull(parameterIndex);
      } else {
         try {
            if (targetMysqlType != null
               && targetMysqlType != MysqlType.UNKNOWN
               && (
                  !(parameterObj instanceof java.util.Date)
                     || this.session.getPropertySet().getBooleanProperty(PropertyKey.treatUtilDateAsTimestamp).getValue()
               )) {
               BindValue binding = this.getBinding(parameterIndex, false);
               if (this.columnDefinition != null && parameterIndex <= this.columnDefinition.getFields().length && parameterIndex >= 0) {
                  binding.setField(this.columnDefinition.getFields()[parameterIndex]);
               }

               binding.setBinding(parameterObj, targetMysqlType, this.numberOfExecutions, this.sendTypesToServer);
               binding.setScaleOrLength((long)scaleOrLength);
            } else {
               this.setSerializableObject(parameterIndex, parameterObj);
            }
         } catch (Exception var6) {
            throw ExceptionFactory.createException(
               Messages.getString("PreparedStatement.17")
                  + parameterObj.getClass().toString()
                  + Messages.getString("PreparedStatement.18")
                  + var6.getClass().getName()
                  + Messages.getString("PreparedStatement.19")
                  + var6.getMessage(),
               var6,
               this.session.getExceptionInterceptor()
            );
         }
      }
   }

   protected final void setSerializableObject(int parameterIndex, Object parameterObj) {
      try {
         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
         ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
         objectOut.writeObject(parameterObj);
         objectOut.flush();
         objectOut.close();
         bytesOut.flush();
         bytesOut.close();
         byte[] buf = bytesOut.toByteArray();
         ByteArrayInputStream bytesIn = new ByteArrayInputStream(buf);
         this.setBinaryStream(parameterIndex, bytesIn, buf.length);
         this.bindValues[parameterIndex].setMysqlType(MysqlType.BINARY);
      } catch (Exception var7) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("PreparedStatement.54") + var7.getClass().getName(), var7, this.session.getExceptionInterceptor()
         );
      }
   }

   @Override
   public byte[] getBytesRepresentation(int parameterIndex) {
      byte[] parameterVal = this.bindValues[parameterIndex].getByteValue();
      return parameterVal == null ? null : (this.bindValues[parameterIndex].isStream() ? parameterVal : StringUtils.unquoteBytes(parameterVal));
   }

   static {
      DEFAULT_MYSQL_TYPES.put(BigDecimal.class, MysqlType.DECIMAL);
      DEFAULT_MYSQL_TYPES.put(BigInteger.class, MysqlType.BIGINT);
      DEFAULT_MYSQL_TYPES.put(Blob.class, MysqlType.BLOB);
      DEFAULT_MYSQL_TYPES.put(Boolean.class, MysqlType.BOOLEAN);
      DEFAULT_MYSQL_TYPES.put(Byte.class, MysqlType.TINYINT);
      DEFAULT_MYSQL_TYPES.put(byte[].class, MysqlType.BINARY);
      DEFAULT_MYSQL_TYPES.put(Calendar.class, MysqlType.TIMESTAMP);
      DEFAULT_MYSQL_TYPES.put(Clob.class, MysqlType.TEXT);
      DEFAULT_MYSQL_TYPES.put(Date.class, MysqlType.DATE);
      DEFAULT_MYSQL_TYPES.put(java.util.Date.class, MysqlType.TIMESTAMP);
      DEFAULT_MYSQL_TYPES.put(Double.class, MysqlType.DOUBLE);
      DEFAULT_MYSQL_TYPES.put(Duration.class, MysqlType.TIME);
      DEFAULT_MYSQL_TYPES.put(Float.class, MysqlType.FLOAT);
      DEFAULT_MYSQL_TYPES.put(InputStream.class, MysqlType.BLOB);
      DEFAULT_MYSQL_TYPES.put(Instant.class, MysqlType.TIMESTAMP);
      DEFAULT_MYSQL_TYPES.put(Integer.class, MysqlType.INT);
      DEFAULT_MYSQL_TYPES.put(LocalDate.class, MysqlType.DATE);
      DEFAULT_MYSQL_TYPES.put(LocalDateTime.class, MysqlType.DATETIME);
      DEFAULT_MYSQL_TYPES.put(LocalTime.class, MysqlType.TIME);
      DEFAULT_MYSQL_TYPES.put(Long.class, MysqlType.BIGINT);
      DEFAULT_MYSQL_TYPES.put(OffsetDateTime.class, MysqlType.TIMESTAMP);
      DEFAULT_MYSQL_TYPES.put(OffsetTime.class, MysqlType.TIME);
      DEFAULT_MYSQL_TYPES.put(Reader.class, MysqlType.TEXT);
      DEFAULT_MYSQL_TYPES.put(Short.class, MysqlType.SMALLINT);
      DEFAULT_MYSQL_TYPES.put(String.class, MysqlType.VARCHAR);
      DEFAULT_MYSQL_TYPES.put(Time.class, MysqlType.TIME);
      DEFAULT_MYSQL_TYPES.put(Timestamp.class, MysqlType.TIMESTAMP);
      DEFAULT_MYSQL_TYPES.put(ZonedDateTime.class, MysqlType.TIMESTAMP);
   }
}
