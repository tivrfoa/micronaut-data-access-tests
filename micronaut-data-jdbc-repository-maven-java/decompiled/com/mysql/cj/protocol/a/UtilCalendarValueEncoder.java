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
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UtilCalendarValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      Calendar x = (Calendar)binding.getValue();
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE:
            return binding.getCalendar() != null
               ? TimeUtil.getSimpleDateFormat("''yyyy-MM-dd''", binding.getCalendar()).format(new Date(x.getTimeInMillis()))
               : TimeUtil.getSimpleDateFormat(null, "''yyyy-MM-dd''", this.serverSession.getDefaultTimeZone()).format(new Date(x.getTimeInMillis()));
         case TIMESTAMP:
            Timestamp ts = this.adjustTimestamp(new Timestamp(((Calendar)binding.getValue()).getTimeInMillis()), binding.getField(), binding.keepOrigNanos());
            StringBuffer buf = new StringBuffer();
            if (binding.getCalendar() != null) {
               buf.append(TimeUtil.getSimpleDateFormat("''yyyy-MM-dd HH:mm:ss", binding.getCalendar()).format(x));
            } else {
               buf.append(
                  TimeUtil.getSimpleDateFormat(
                        null,
                        "''yyyy-MM-dd HH:mm:ss",
                        binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                           ? this.serverSession.getSessionTimeZone()
                           : this.serverSession.getDefaultTimeZone()
                     )
                     .format(ts)
               );
            }

            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && ts.getNanos() > 0) {
               buf.append('.');
               buf.append(TimeUtil.formatNanos(ts.getNanos(), 6));
            }

            buf.append('\'');
            return buf.toString();
         case DATETIME: {
         case CHAR: {
         case VARCHAR: {
         case TINYTEXT: {
         case TEXT: {
         case MEDIUMTEXT: {
         case LONGTEXT: {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(x.toInstant(), x.getTimeZone().toZoneId())
               .withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId());
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               zdt.format(
                  zdt.getNano() > 0 && this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()
                     ? TimeUtil.DATETIME_FORMATTER_WITH_MILLIS_NO_OFFSET
                     : TimeUtil.DATETIME_FORMATTER_NO_FRACT_NO_OFFSET
               )
            );
            sb.append("'");
            return sb.toString();
         }
         case YEAR:
            return String.valueOf(x.get(1));
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalTime(
                     ((Calendar)binding.getValue()).toInstant().atZone(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(), binding.getField()
                  )
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
         }
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
      Calendar x = (Calendar)binding.getValue();
      Calendar calendar = binding.getCalendar();
      switch(binding.getMysqlType()) {
         case DATE:
            if (calendar == null) {
               calendar = Calendar.getInstance(this.serverSession.getDefaultTimeZone(), Locale.US);
            }

            calendar.setTime(new Date(x.getTimeInMillis()));
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            this.writeDate(msg, InternalDate.from(calendar));
            return;
         case TIMESTAMP:
         case DATETIME:
            if (calendar == null) {
               calendar = Calendar.getInstance(
                  binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                     ? this.serverSession.getSessionTimeZone()
                     : this.serverSession.getDefaultTimeZone(),
                  Locale.US
               );
            }

            Timestamp ts = this.adjustTimestamp(new Timestamp(((Calendar)binding.getValue()).getTimeInMillis()), binding.getField(), binding.keepOrigNanos());
            calendar.setTime(ts);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, ts.getNanos()));
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            ZonedDateTime zdt = ZonedDateTime.ofInstant(x.toInstant(), x.getTimeZone().toZoneId())
               .withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId());
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC,
               StringUtils.getBytes(
                  zdt.format(
                     zdt.getNano() > 0 && this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()
                        ? TimeUtil.DATETIME_FORMATTER_WITH_MILLIS_NO_OFFSET
                        : TimeUtil.DATETIME_FORMATTER_NO_FRACT_NO_OFFSET
                  ),
                  (String)this.charEncoding.getValue()
               )
            );
            return;
         case YEAR:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)x.get(1));
            return;
         case TIME:
            this.writeTime(
               msg,
               InternalTime.from(
                  this.adjustLocalTime(
                     ((Calendar)binding.getValue()).toInstant().atZone(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(), binding.getField()
                  )
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
      Calendar calendar = (Calendar)binding.getValue();
      InternalTimestamp internalTimestamp = InternalTimestamp.from(calendar, (int)TimeUnit.MILLISECONDS.toNanos((long)calendar.get(14)));
      internalTimestamp.setOffset((int)TimeUnit.MILLISECONDS.toMinutes((long)calendar.getTimeZone().getOffset(calendar.getTimeInMillis())));
      this.writeDateTimeWithOffset(msg, internalTimestamp);
   }
}
