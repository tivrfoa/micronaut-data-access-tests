package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTime;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SqlTimestampValueEncoder extends AbstractValueEncoder {
   private SimpleDateFormat tsdf = null;

   @Override
   public String getString(BindValue binding) {
      Timestamp x = this.adjustTimestamp((Timestamp)((Timestamp)binding.getValue()).clone(), binding.getField(), binding.keepOrigNanos());
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE:
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd''", binding.getCalendar()).format(new Date(((java.util.Date)binding.getValue()).getTime()))
               : TimeUtil.getSimpleDateFormat(null, "''yyyy-MM-dd''", this.serverSession.getDefaultTimeZone())
                  .format(new Date(((java.util.Date)binding.getValue()).getTime()));
         case DATETIME:
         case TIMESTAMP:
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            StringBuffer buf = new StringBuffer();
            if (binding.getCalendar() != null) {
               buf.append(TimeUtil.getSimpleDateFormat("''yyyy-MM-dd HH:mm:ss", binding.getCalendar()).format(x));
            } else {
               this.tsdf = TimeUtil.getSimpleDateFormat(
                  this.tsdf,
                  "''yyyy-MM-dd HH:mm:ss",
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone()
               );
               buf.append(this.tsdf.format(x));
            }

            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
               buf.append('.');
               buf.append(TimeUtil.formatNanos(x.getNanos(), 6));
            }

            buf.append('\'');
            return buf.toString();
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            return String.valueOf(cal.get(1));
         case TIME:
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalTime(((Timestamp)binding.getValue()).toLocalDateTime().toLocalTime(), binding.getField())
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
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
      Timestamp x = this.adjustTimestamp((Timestamp)((Timestamp)binding.getValue()).clone(), binding.getField(), binding.keepOrigNanos());
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
         case DATETIME:
         case TIMESTAMP:
            if (calendar == null) {
               calendar = Calendar.getInstance(
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone(),
                  Locale.US
               );
            }

            calendar.setTime(x);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, x.getNanos()));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            StringBuffer buf = new StringBuffer();
            if (binding.getCalendar() != null) {
               buf.append(TimeUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss", binding.getCalendar()).format(x));
            } else {
               this.tsdf = TimeUtil.getSimpleDateFormat(
                  this.tsdf,
                  "yyyy-MM-dd HH:mm:ss",
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone()
               );
               buf.append(this.tsdf.format(x));
            }

            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
               buf.append('.');
               buf.append(TimeUtil.formatNanos(x.getNanos(), 6));
            }

            intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(buf.toString(), (String)this.charEncoding.getValue()));
            return;
         case YEAR:
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date)binding.getValue());
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)cal.get(1));
            return;
         case TIME:
            Time t = this.adjustTime(new Time(x.getTime()));
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime(t);
            this.writeTime(
               msg,
               InternalTime.from(
                  calendar, this.adjustTimestamp((Timestamp)((Timestamp)binding.getValue()).clone(), binding.getField(), binding.keepOrigNanos()).getNanos()
               )
            );
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
      Timestamp x = (Timestamp)binding.getValue();
      Calendar cal = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
      cal.setTime(x);
      InternalTimestamp internalTimestamp = InternalTimestamp.from(cal, x.getNanos());
      internalTimestamp.setOffset((int)TimeUnit.MILLISECONDS.toMinutes((long)cal.getTimeZone().getOffset(cal.getTimeInMillis())));
      this.writeDateTimeWithOffset(msg, internalTimestamp);
   }
}
