package com.fasterxml.jackson.datatype.jsr310;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiFunction;

public final class DecimalUtils {
   private static final BigDecimal ONE_BILLION = new BigDecimal(1000000000L);

   private DecimalUtils() {
   }

   public static String toDecimal(long seconds, int nanoseconds) {
      StringBuilder sb = new StringBuilder(20).append(seconds).append('.');
      if ((long)nanoseconds == 0L) {
         if (seconds == 0L) {
            return "0.0";
         }

         sb.append("000000000");
      } else {
         StringBuilder nanoSB = new StringBuilder(9);
         nanoSB.append(nanoseconds);
         int nanosLen = nanoSB.length();
         int prepZeroes = 9 - nanosLen;

         while(prepZeroes > 0) {
            --prepZeroes;
            sb.append('0');
         }

         sb.append(nanoSB);
      }

      return sb.toString();
   }

   public static BigDecimal toBigDecimal(long seconds, int nanoseconds) {
      if ((long)nanoseconds == 0L) {
         return seconds == 0L ? BigDecimal.ZERO.setScale(1) : BigDecimal.valueOf(seconds).setScale(9);
      } else {
         return new BigDecimal(toDecimal(seconds, nanoseconds));
      }
   }

   @Deprecated
   public static int extractNanosecondDecimal(BigDecimal value, long integer) {
      return value.subtract(new BigDecimal(integer)).multiply(ONE_BILLION).intValue();
   }

   public static <T> T extractSecondsAndNanos(BigDecimal seconds, BiFunction<Long, Integer, T> convert) {
      BigDecimal nanoseconds = seconds.scaleByPowerOfTen(9);
      long secondsOnly;
      int nanosOnly;
      if (nanoseconds.precision() - nanoseconds.scale() <= 0) {
         nanosOnly = 0;
         secondsOnly = (long)0;
      } else if (seconds.scale() < -63) {
         nanosOnly = 0;
         secondsOnly = (long)0;
      } else {
         secondsOnly = seconds.longValue();
         nanosOnly = nanoseconds.subtract(new BigDecimal(secondsOnly).scaleByPowerOfTen(9)).intValue();
         if (secondsOnly < 0L && secondsOnly > Instant.MIN.getEpochSecond()) {
            nanosOnly = Math.abs(nanosOnly);
         }
      }

      return (T)convert.apply(secondsOnly, nanosOnly);
   }
}
