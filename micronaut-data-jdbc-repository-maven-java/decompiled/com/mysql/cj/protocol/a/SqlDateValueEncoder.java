package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public class SqlDateValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE:
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd''", binding.getCalendar()).format((Date)binding.getValue())
               : TimeUtil.getSimpleDateFormat(null, "''yyyy-MM-dd''", this.serverSession.getDefaultTimeZone()).format((Date)binding.getValue());
         case DATETIME:
         case TIMESTAMP:
            Timestamp ts = new Timestamp(((java.util.Date)binding.getValue()).getTime());
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd HH:mm:ss''", binding.getCalendar()).format(ts)
               : TimeUtil.getSimpleDateFormat(
                     null,
                     "''yyyy-MM-dd HH:mm:ss''",
                     binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                        ? this.serverSession.getSessionTimeZone()
                        : this.serverSession.getDefaultTimeZone()
                  )
                  .format(ts);
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            return String.valueOf(cal.get(1));
         default:
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
               this.exceptionInterceptor
            );
      }
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      Calendar calendar = binding.getCalendar();
      switch(binding.getMysqlType()) {
         case DATE:
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime((java.util.Date)binding.getValue());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            this.writeDate(msg, InternalDate.from(calendar));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            String val = binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("yyyy-MM-dd", binding.getCalendar()).format((Date)binding.getValue())
               : TimeUtil.getSimpleDateFormat(null, "yyyy-MM-dd", this.serverSession.getDefaultTimeZone()).format((Date)binding.getValue());
            intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(val, (String)this.charEncoding.getValue()));
            return;
         case DATETIME:
         case TIMESTAMP:
            if (calendar == null) {
               calendar = Calendar.getInstance(
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.propertySet.getBooleanProperty(PropertyKey.preserveInstants).getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone(),
                  Locale.US
               );
            }

            Timestamp ts = new Timestamp(((java.util.Date)binding.getValue()).getTime());
            calendar.setTime(ts);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, ts.getNanos()));
            return;
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)cal.get(1));
            return;
         default:
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
               this.exceptionInterceptor
            );
      }
   }

   @Override
   public void encodeAsQueryAttribute(Message msg, BindValue binding) {
      Calendar calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
      calendar.setTime((java.util.Date)binding.getValue());
      calendar.set(11, 0);
      calendar.set(12, 0);
      calendar.set(13, 0);
      this.writeDate(msg, InternalDate.from(calendar));
   }
}
