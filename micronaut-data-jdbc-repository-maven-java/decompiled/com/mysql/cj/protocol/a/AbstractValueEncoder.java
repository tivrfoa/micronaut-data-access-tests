package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTime;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.ValueEncoder;
import com.mysql.cj.result.Field;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public abstract class AbstractValueEncoder implements ValueEncoder {
   protected PropertySet propertySet;
   protected ServerSession serverSession;
   protected ExceptionInterceptor exceptionInterceptor;
   protected RuntimeProperty<String> charEncoding = null;
   protected RuntimeProperty<Boolean> sendFractionalSeconds;
   protected RuntimeProperty<Boolean> sendFractionalSecondsForTime;
   protected RuntimeProperty<Boolean> preserveInstants;

   @Override
   public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
      this.propertySet = pset;
      this.serverSession = serverSess;
      this.exceptionInterceptor = excInterceptor;
      this.charEncoding = pset.getStringProperty(PropertyKey.characterEncoding);
      this.sendFractionalSeconds = pset.getBooleanProperty(PropertyKey.sendFractionalSeconds);
      this.sendFractionalSecondsForTime = pset.getBooleanProperty(PropertyKey.sendFractionalSecondsForTime);
      this.preserveInstants = pset.getBooleanProperty(PropertyKey.preserveInstants);
   }

   @Override
   public byte[] getBytes(BindValue binding) {
      return StringUtils.getBytes(this.getString(binding), (String)this.charEncoding.getValue());
   }

   @Override
   public void encodeAsText(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      intoPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, this.getBytes(binding));
   }

   @Override
   public void encodeAsQueryAttribute(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      String x = binding.getValue().toString();
      intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(x, (String)this.charEncoding.getValue()));
   }

   protected BigDecimal getScaled(BigDecimal x, long scaleOrLength) {
      if (scaleOrLength < 0L) {
         return x.setScale(x.scale());
      } else {
         BigDecimal scaledBigDecimal;
         try {
            scaledBigDecimal = x.setScale((int)scaleOrLength);
         } catch (ArithmeticException var8) {
            try {
               scaledBigDecimal = x.setScale((int)scaleOrLength, 4);
            } catch (ArithmeticException var7) {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("PreparedStatement.65", new Object[]{scaleOrLength, x.toPlainString()}),
                  this.exceptionInterceptor
               );
            }
         }

         return scaledBigDecimal;
      }
   }

   protected LocalTime adjustLocalTime(LocalTime x, Field f) {
      if (this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()) {
         return TimeUtil.adjustNanosPrecision(x, f == null ? 6 : f.getDecimals(), !this.serverSession.isServerTruncatesFracSecs());
      } else {
         if (x.getNano() > 0) {
            x = x.withNano(0);
         }

         return x;
      }
   }

   protected LocalDateTime adjustLocalDateTime(LocalDateTime x, Field f) {
      if (this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()) {
         return TimeUtil.adjustNanosPrecision(x, f == null ? 6 : f.getDecimals(), !this.serverSession.isServerTruncatesFracSecs());
      } else {
         if (x.getNano() > 0) {
            x = x.withNano(0);
         }

         return x;
      }
   }

   protected Duration adjustDuration(Duration x, Field f) {
      if (this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()) {
         return TimeUtil.adjustNanosPrecision(x, f == null ? 6 : f.getDecimals(), !this.serverSession.isServerTruncatesFracSecs());
      } else {
         if (x.getNano() > 0) {
            x = x.isNegative() ? x.plusSeconds(1L).withNanos(0) : x.withNanos(0);
         }

         return x;
      }
   }

   protected Timestamp adjustTimestamp(Timestamp x, Field f, boolean keepOrigNanos) {
      if (keepOrigNanos) {
         return x;
      } else {
         return this.serverSession.getCapabilities().serverSupportsFracSecs() && this.sendFractionalSeconds.getValue()
            ? TimeUtil.adjustNanosPrecision(x, f == null ? 6 : f.getDecimals(), !this.serverSession.isServerTruncatesFracSecs())
            : TimeUtil.truncateFractionalSeconds(x);
      }
   }

   protected Time adjustTime(Time x) {
      return this.serverSession.getCapabilities().serverSupportsFracSecs()
            && this.sendFractionalSeconds.getValue()
            && this.sendFractionalSecondsForTime.getValue()
         ? x
         : TimeUtil.truncateFractionalSeconds(x);
   }

   protected void writeDate(Message msg, InternalDate d) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      intoPacket.ensureCapacity(5);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 4L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)d.getYear());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)d.getMonth());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)d.getDay());
   }

   protected void writeTime(Message msg, InternalTime time) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      boolean hasFractionalSeconds = time.getNanos() > 0;
      intoPacket.ensureCapacity((hasFractionalSeconds ? 12 : 8) + 1);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, hasFractionalSeconds ? 12L : 8L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, time.isNegative() ? 1L : 0L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, (long)(time.getHours() / 24));
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)(time.getHours() % 24));
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)time.getMinutes());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)time.getSeconds());
      if (hasFractionalSeconds) {
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, TimeUnit.NANOSECONDS.toMicros((long)time.getNanos()));
      }

   }

   protected void writeDateTime(Message msg, InternalTimestamp ts) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      long microseconds = TimeUnit.NANOSECONDS.toMicros((long)ts.getNanos());
      intoPacket.ensureCapacity((microseconds > 0L ? 11 : 7) + 1);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, microseconds > 0L ? 11L : 7L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)ts.getYear());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)ts.getMonth());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)ts.getDay());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)ts.getHours());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)ts.getMinutes());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)ts.getSeconds());
      if (microseconds > 0L) {
         intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, microseconds);
      }

   }

   public void writeDateTimeWithOffset(Message msg, InternalTimestamp timestamp) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      intoPacket.ensureCapacity(14);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 13L);
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)timestamp.getYear());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)timestamp.getMonth());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)timestamp.getDay());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)timestamp.getHours());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)timestamp.getMinutes());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, (long)timestamp.getSeconds());
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, TimeUnit.NANOSECONDS.toMicros((long)timestamp.getNanos()));
      intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)timestamp.getOffset());
   }

   protected byte[] escapeBytesIfNeeded(byte[] x) {
      if (!this.serverSession.isNoBackslashEscapesSet() && !this.serverSession.getCharsetSettings().isMultibyteCharset((String)this.charEncoding.getValue())) {
         ByteArrayOutputStream bOut = new ByteArrayOutputStream(x.length + 9);
         bOut.write(95);
         bOut.write(98);
         bOut.write(105);
         bOut.write(110);
         bOut.write(97);
         bOut.write(114);
         bOut.write(121);
         bOut.write(39);
         StringUtils.escapeBytes(bOut, x);
         bOut.write(39);
         return bOut.toByteArray();
      } else {
         ByteArrayOutputStream bOut = new ByteArrayOutputStream(x.length * 2 + 3);
         bOut.write(120);
         bOut.write(39);
         StringUtils.hexEscapeBlock(x, x.length, (lowBits, highBits) -> {
            bOut.write(lowBits);
            bOut.write(highBits);
         });
         bOut.write(39);
         return bOut.toByteArray();
      }
   }

   @Override
   public long getTextLength(BindValue binding) {
      if (binding.isNull()) {
         return 4L;
      } else {
         return binding.isStream() && binding.getScaleOrLength() != -1L ? binding.getScaleOrLength() * 2L : (long)binding.getByteValue().length;
      }
   }

   @Override
   public long getBinaryLength(BindValue binding) {
      if (binding.isNull()) {
         return 0L;
      } else if (binding.isStream() && binding.getScaleOrLength() != -1L) {
         return binding.getScaleOrLength();
      } else {
         int bufferType = binding.getFieldType();
         switch(bufferType) {
            case 0:
            case 15:
            case 246:
            case 254:
               if (binding.getValue() instanceof byte[]) {
                  return (long)((byte[])((byte[])binding.getValue())).length;
               } else {
                  if (binding.getValue() instanceof BigDecimal) {
                     return (long)((BigDecimal)binding.getValue()).toPlainString().length();
                  }

                  return (long)((String)binding.getValue()).length();
               }
            case 1:
               return 1L;
            case 2:
               return 2L;
            case 3:
               return 4L;
            case 4:
               return 4L;
            case 5:
               return 8L;
            case 7:
               return 14L;
            case 8:
               return 8L;
            case 10:
               return 5L;
            case 11:
               return 13L;
            case 12:
               return 12L;
            case 253:
               return (long)(binding.getValue().toString().length() + 9);
            default:
               return 0L;
         }
      }
   }
}
