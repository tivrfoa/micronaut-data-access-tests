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
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;

public class ZonedDateTimeValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               ((ZonedDateTime)binding.getValue())
                  .withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId())
                  .toLocalDate()
                  .format(TimeUtil.DATE_FORMATTER)
            );
            sb.append("'");
            return sb.toString();
         }
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalTime(
                     ((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(),
                     binding.getField()
                  )
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
         }
         case DATETIME:
         case TIMESTAMP:
            Timestamp x = this.adjustTimestamp(
               Timestamp.valueOf(((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalDateTime()),
               binding.getField(),
               binding.keepOrigNanos()
            );
            StringBuffer buf = new StringBuffer();
            buf.append(
               TimeUtil.getSimpleDateFormat(
                     null,
                     "''yyyy-MM-dd HH:mm:ss",
                     binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                        ? this.serverSession.getSessionTimeZone()
                        : this.serverSession.getDefaultTimeZone()
                  )
                  .format(x)
            );
            if (this.serverSession.getCapabilities().serverSupportsFracSecs() && x.getNanos() > 0) {
               buf.append('.');
               buf.append(TimeUtil.formatNanos(x.getNanos(), 6));
            }

            buf.append('\'');
            return buf.toString();
         case YEAR:
            return String.valueOf(((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).getYear());
         case CHAR: {
         case VARCHAR: {
         case TINYTEXT: {
         case TEXT: {
         case MEDIUMTEXT: {
         case LONGTEXT: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               ((ZonedDateTime)binding.getValue())
                  .format(
                     this.sendFractionalSeconds.getValue() && ((ZonedDateTime)binding.getValue()).getNano() > 0
                        ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                        : TimeUtil.DATETIME_FORMATTER_NO_FRACT_WITH_OFFSET
                  )
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
      switch(binding.getMysqlType()) {
         case DATE:
            this.writeDate(
               msg,
               InternalDate.from(((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalDate())
            );
            return;
         case TIME:
            this.writeTime(
               msg,
               InternalTime.from(
                  this.adjustLocalTime(
                     ((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalTime(),
                     binding.getField()
                  )
               )
            );
            return;
         case DATETIME:
         case TIMESTAMP:
            Timestamp ts = this.adjustTimestamp(
               Timestamp.valueOf(((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).toLocalDateTime()),
               binding.getField(),
               binding.keepOrigNanos()
            );
            Calendar calendar = Calendar.getInstance(
               binding.getMysqlType() == MysqlType.TIMESTAMP && this.preserveInstants.getValue()
                  ? this.serverSession.getSessionTimeZone()
                  : this.serverSession.getDefaultTimeZone(),
               Locale.US
            );
            calendar.setTime(ts);
            this.writeDateTime(msg, InternalTimestamp.from(calendar, ts.getNanos()));
            return;
         case YEAR:
            intoPacket.writeInteger(
               NativeConstants.IntegerDataType.INT4,
               (long)((ZonedDateTime)binding.getValue()).withZoneSameInstant(this.serverSession.getDefaultTimeZone().toZoneId()).getYear()
            );
            return;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC,
               StringUtils.getBytes(
                  ((ZonedDateTime)binding.getValue())
                     .format(
                        this.sendFractionalSeconds.getValue() && ((ZonedDateTime)binding.getValue()).getNano() > 0
                           ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_WITH_OFFSET
                           : TimeUtil.DATETIME_FORMATTER_NO_FRACT_WITH_OFFSET
                     ),
                  (String)this.charEncoding.getValue()
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
      this.writeDateTimeWithOffset(msg, InternalTimestamp.from((ZonedDateTime)binding.getValue()));
   }
}
