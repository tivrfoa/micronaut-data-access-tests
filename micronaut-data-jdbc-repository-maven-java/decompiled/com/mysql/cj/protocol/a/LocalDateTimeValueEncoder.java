package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTime;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.time.LocalDateTime;

public class LocalDateTimeValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case DATE: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(((LocalDateTime)binding.getValue()).format(TimeUtil.DATE_FORMATTER));
            sb.append("'");
            return sb.toString();
         }
         case TIME: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalDateTime(
                     LocalDateTime.of(((LocalDateTime)binding.getValue()).toLocalDate(), ((LocalDateTime)binding.getValue()).toLocalTime()), binding.getField()
                  )
                  .toLocalTime()
                  .format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
         }
         case DATETIME: {
         case TIMESTAMP: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               this.adjustLocalDateTime(
                     LocalDateTime.of(((LocalDateTime)binding.getValue()).toLocalDate(), ((LocalDateTime)binding.getValue()).toLocalTime()), binding.getField()
                  )
                  .format(TimeUtil.DATETIME_FORMATTER_WITH_OPTIONAL_MICROS)
            );
            sb.append("'");
            return sb.toString();
         }
         case YEAR:
            return String.valueOf(((LocalDateTime)binding.getValue()).getYear());
         case CHAR: {
         case VARCHAR: {
         case TINYTEXT: {
         case TEXT: {
         case MEDIUMTEXT: {
         case LONGTEXT: {
            StringBuilder sb = new StringBuilder("'");
            sb.append(
               ((LocalDateTime)binding.getValue())
                  .format(
                     this.sendFractionalSeconds.getValue() && ((LocalDateTime)binding.getValue()).getNano() > 0
                        ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_NO_OFFSET
                        : TimeUtil.DATETIME_FORMATTER_NO_FRACT_NO_OFFSET
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
      LocalDateTime ldt = (LocalDateTime)binding.getValue();
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      switch(binding.getMysqlType()) {
         case DATE:
            this.writeDate(
               msg, InternalDate.from(this.adjustLocalDateTime(LocalDateTime.of(ldt.toLocalDate(), ldt.toLocalTime()), binding.getField()).toLocalDate())
            );
            return;
         case TIME:
            this.writeTime(msg, InternalTime.from(this.adjustLocalDateTime(LocalDateTime.of(ldt.toLocalDate(), ldt.toLocalTime()), binding.getField())));
            return;
         case DATETIME:
         case TIMESTAMP:
            this.writeDateTime(
               msg, InternalTimestamp.from(this.adjustLocalDateTime(LocalDateTime.of(ldt.toLocalDate(), ldt.toLocalTime()), binding.getField()))
            );
            return;
         case YEAR:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)ldt.getYear());
            break;
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC,
               StringUtils.getBytes(
                  ldt.format(
                     this.sendFractionalSeconds.getValue() && ldt.getNano() > 0
                        ? TimeUtil.DATETIME_FORMATTER_WITH_NANOS_NO_OFFSET
                        : TimeUtil.DATETIME_FORMATTER_NO_FRACT_NO_OFFSET
                  ),
                  (String)this.charEncoding.getValue()
               )
            );
            break;
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
      this.writeDateTime(msg, InternalTimestamp.from((LocalDateTime)binding.getValue()));
   }
}
