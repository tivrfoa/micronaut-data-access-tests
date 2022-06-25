package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.util.StringUtils;
import java.math.BigDecimal;

public class NumberValueEncoder extends AbstractValueEncoder {
   @Override
   public String getString(BindValue binding) {
      Number x = (Number)(binding.getValue() instanceof BigDecimal
         ? this.getScaled((BigDecimal)binding.getValue(), binding.getScaleOrLength())
         : (Number)binding.getValue());
      switch(binding.getMysqlType()) {
         case NULL:
            return "null";
         case BOOLEAN:
            return String.valueOf(x.longValue() != 0L);
         case BIT:
         case TINYINT:
         case TINYINT_UNSIGNED:
         case SMALLINT:
         case SMALLINT_UNSIGNED:
         case MEDIUMINT:
         case MEDIUMINT_UNSIGNED:
         case INT:
         case INT_UNSIGNED:
         case YEAR:
            return String.valueOf(x.intValue());
         case BIGINT:
         case BIGINT_UNSIGNED:
            return String.valueOf(x.longValue());
         case FLOAT:
         case FLOAT_UNSIGNED:
            return StringUtils.fixDecimalExponent(Float.toString(x.floatValue()));
         case DOUBLE:
         case DOUBLE_UNSIGNED:
            return StringUtils.fixDecimalExponent(Double.toString(x.doubleValue()));
         case DECIMAL:
         case DECIMAL_UNSIGNED:
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
         case BINARY:
         case VARBINARY:
         case TINYBLOB:
         case BLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            return x instanceof BigDecimal ? ((BigDecimal)x).toPlainString() : StringUtils.fixDecimalExponent(x.toString());
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
      Number x = (Number)(binding.getValue() instanceof BigDecimal
         ? this.getScaled((BigDecimal)binding.getValue(), binding.getScaleOrLength())
         : (Number)binding.getValue());
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      switch(binding.getMysqlType()) {
         case BOOLEAN:
         case BIT:
         case TINYINT:
         case TINYINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, x.longValue());
            return;
         case SMALLINT:
         case SMALLINT_UNSIGNED:
         case MEDIUMINT:
         case MEDIUMINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, x.longValue());
            return;
         case INT:
         case INT_UNSIGNED:
         case YEAR:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, x.longValue());
            return;
         case BIGINT:
         case BIGINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, x.longValue());
            return;
         case FLOAT:
         case FLOAT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)Float.floatToIntBits(x.floatValue()));
            return;
         case DOUBLE:
         case DOUBLE_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, Double.doubleToLongBits(x.doubleValue()));
            return;
         case DECIMAL:
         case DECIMAL_UNSIGNED:
         case CHAR:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
         case BINARY:
         case VARBINARY:
         case TINYBLOB:
         case BLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            intoPacket.writeBytes(
               NativeConstants.StringSelfDataType.STRING_LENENC,
               StringUtils.getBytes(x instanceof BigDecimal ? ((BigDecimal)x).toPlainString() : x.toString(), (String)this.charEncoding.getValue())
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
      this.encodeAsBinary(msg, binding);
   }
}
