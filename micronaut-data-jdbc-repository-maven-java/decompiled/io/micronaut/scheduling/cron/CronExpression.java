package io.micronaut.scheduling.cron;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CronExpression {
   private static final int CRON_EXPRESSION_LENGTH_WITH_SEC = 6;
   private static final int CRON_EXPRESSION_LENGTH_WITHOUT_SEC = 5;
   private static final int FOUR = 4;
   private final String expr;
   private final CronExpression.SimpleField secondField;
   private final CronExpression.SimpleField minuteField;
   private final CronExpression.SimpleField hourField;
   private final CronExpression.DayOfWeekField dayOfWeekField;
   private final CronExpression.SimpleField monthField;
   private final CronExpression.DayOfMonthField dayOfMonthField;

   private CronExpression(final String expr) {
      if (expr == null) {
         throw new IllegalArgumentException("expr is null");
      } else {
         this.expr = expr;
         String[] parts = expr.split("\\s+");
         if (parts.length >= 5 && parts.length <= 6) {
            boolean withSeconds = parts.length == 6;
            int ix = withSeconds ? 1 : 0;
            this.secondField = new CronExpression.SimpleField(CronExpression.CronFieldType.SECOND, withSeconds ? parts[0] : "0");
            this.minuteField = new CronExpression.SimpleField(CronExpression.CronFieldType.MINUTE, parts[ix++]);
            this.hourField = new CronExpression.SimpleField(CronExpression.CronFieldType.HOUR, parts[ix++]);
            this.dayOfMonthField = new CronExpression.DayOfMonthField(parts[ix++]);
            this.monthField = new CronExpression.SimpleField(CronExpression.CronFieldType.MONTH, parts[ix++]);
            this.dayOfWeekField = new CronExpression.DayOfWeekField(parts[ix++]);
         } else {
            throw new IllegalArgumentException(String.format("Invalid cron expression [%s], expected 5 or 6 fields, got %s", expr, parts.length));
         }
      }
   }

   public static CronExpression create(final String expr) {
      return new CronExpression(expr);
   }

   public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime) {
      return this.nextTimeAfter(afterTime, afterTime.plusYears(4L));
   }

   public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime, long durationInMillis) {
      return this.nextTimeAfter(afterTime, afterTime.plus(Duration.ofMillis(durationInMillis)));
   }

   public ZonedDateTime nextTimeAfter(ZonedDateTime afterTime, ZonedDateTime dateTimeBarrier) {
      ZonedDateTime nextTime = ZonedDateTime.from(afterTime).withNano(0).plusSeconds(1L).withNano(0);

      while(true) {
         while(!this.secondField.matches(nextTime.getSecond())) {
            nextTime = nextTime.plusSeconds(1L).withNano(0);
         }

         if (this.minuteField.matches(nextTime.getMinute())) {
            if (this.hourField.matches(nextTime.getHour())) {
               if (this.dayOfMonthField.matches(nextTime.toLocalDate())) {
                  if (this.monthField.matches(nextTime.getMonth().getValue())) {
                     if (this.dayOfWeekField.matches(nextTime.toLocalDate())) {
                        return nextTime;
                     }

                     nextTime = nextTime.plusDays(1L).withHour(0).withMinute(0).withSecond(0).withNano(0);
                     checkIfDateTimeBarrierIsReached(nextTime, dateTimeBarrier);
                  } else {
                     nextTime = nextTime.plusMonths(1L).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                     checkIfDateTimeBarrierIsReached(nextTime, dateTimeBarrier);
                  }
               } else {
                  nextTime = nextTime.plusDays(1L).withHour(0).withMinute(0).withSecond(0).withNano(0);
                  checkIfDateTimeBarrierIsReached(nextTime, dateTimeBarrier);
               }
            } else {
               nextTime = nextTime.plusHours(1L).withMinute(0).withSecond(0).withNano(0);
            }
         } else {
            nextTime = nextTime.plusMinutes(1L).withSecond(0).withNano(0);
         }
      }
   }

   private static void checkIfDateTimeBarrierIsReached(ZonedDateTime nextTime, ZonedDateTime dateTimeBarrier) {
      if (nextTime.isAfter(dateTimeBarrier)) {
         throw new IllegalArgumentException("No next execution time could be determined that is before the limit of " + dateTimeBarrier);
      }
   }

   public String getExpression() {
      return this.expr;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "<" + this.expr + ">";
   }

   abstract static class BasicField {
      private static final Pattern CRON_FIELD_REGEXP = Pattern.compile(
         "(?:                                             # start of group 1\n   (?:(?<all>\\*)|(?<ignore>\\?)|(?<last>L))  # global flag (L, ?, *)\n | (?<start>[0-9]{1,2}|[a-z]{3,3})              # or start number or symbol\n      (?:                                        # start of group 2\n         (?<mod>L|W)                             # modifier (L,W)\n       | -(?<end>[0-9]{1,2}|[a-z]{3,3})        # or end nummer or symbol (in range)\n      )?                                         # end of group 2\n)                                              # end of group 1\n(?:(?<incmod>/|\\#)(?<inc>[0-9]{1,7}))?        # increment and increment modifier (/ or \\#)\n",
         6
      );
      private static final int PART_INCREMENT = 999;
      final CronExpression.CronFieldType fieldType;
      final List<CronExpression.FieldPart> parts = new ArrayList();

      private BasicField(CronExpression.CronFieldType fieldType, String fieldExpr) {
         this.fieldType = fieldType;
         this.parse(fieldExpr);
      }

      private void parse(String fieldExpr) {
         String[] rangeParts = fieldExpr.split(",");

         for(String rangePart : rangeParts) {
            Matcher m = CRON_FIELD_REGEXP.matcher(rangePart);
            if (!m.matches()) {
               throw new IllegalArgumentException("Invalid cron field '" + rangePart + "' for field [" + this.fieldType + "]");
            }

            String startNummer = m.group("start");
            String modifier = m.group("mod");
            String sluttNummer = m.group("end");
            String incrementModifier = m.group("incmod");
            String increment = m.group("inc");
            CronExpression.FieldPart part = new CronExpression.FieldPart();
            part.increment = 999;
            if (startNummer != null) {
               part.from = this.mapValue(startNummer);
               part.modifier = modifier;
               if (sluttNummer != null) {
                  part.to = this.mapValue(sluttNummer);
                  part.increment = 1;
               } else if (increment != null) {
                  part.to = this.fieldType.to;
               } else {
                  part.to = part.from;
               }
            } else if (m.group("all") != null) {
               part.from = this.fieldType.from;
               part.to = this.fieldType.to;
               part.increment = 1;
            } else if (m.group("ignore") != null) {
               part.modifier = m.group("ignore");
            } else {
               if (m.group("last") == null) {
                  throw new IllegalArgumentException("Invalid cron part: " + rangePart);
               }

               part.modifier = m.group("last");
            }

            if (increment != null) {
               part.incrementModifier = incrementModifier;
               part.increment = Integer.valueOf(increment);
            }

            this.validateRange(part);
            this.validatePart(part);
            this.parts.add(part);
         }

      }

      protected void validatePart(CronExpression.FieldPart part) {
         if (part.modifier != null) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.modifier));
         } else if (part.incrementModifier != null && !"/".equals(part.incrementModifier)) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.incrementModifier));
         }
      }

      private void validateRange(CronExpression.FieldPart part) {
         if ((part.from == null || part.from >= this.fieldType.from) && (part.to == null || part.to <= this.fieldType.to)) {
            if (part.from != null && part.to != null && part.from > part.to) {
               throw new IllegalArgumentException(
                  String.format(
                     "Invalid interval [%s-%s].  Rolling periods are not supported (ex. 5-1, only 1-5) since this won't give a deterministic result. Must be %s<=_<=%s",
                     part.from,
                     part.to,
                     this.fieldType.from,
                     this.fieldType.to
                  )
               );
            }
         } else {
            throw new IllegalArgumentException(
               String.format("Invalid interval [%s-%s], must be %s<=_<=%s", part.from, part.to, this.fieldType.from, this.fieldType.to)
            );
         }
      }

      protected Integer mapValue(String value) {
         if (this.fieldType.names != null) {
            Integer idx = this.fieldType.names.indexOf(value.toUpperCase(Locale.getDefault()));
            if (idx >= 0) {
               return idx + 1;
            }
         }

         return Integer.valueOf(value);
      }

      protected boolean matches(int val, CronExpression.FieldPart part) {
         return val >= part.from && val <= part.to && (val - part.from) % part.increment == 0;
      }
   }

   static enum CronFieldType {
      SECOND(0, 59, null),
      MINUTE(0, 59, null),
      HOUR(0, 23, null),
      DAY_OF_MONTH(1, 31, null),
      MONTH(1, 12, Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")),
      DAY_OF_WEEK(1, 7, Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"));

      final int from;
      final int to;
      final List<String> names;

      private CronFieldType(int from, int to, List<String> names) {
         this.from = from;
         this.to = to;
         this.names = names;
      }
   }

   static class DayOfMonthField extends CronExpression.BasicField {
      static final int WEEK_DAYS = 5;
      static final int FIRST_DAY = 1;
      static final int ONE_DAY = 1;

      DayOfMonthField(String fieldExpr) {
         super(CronExpression.CronFieldType.DAY_OF_MONTH, fieldExpr);
      }

      boolean matches(LocalDate date) {
         for(CronExpression.FieldPart part : this.parts) {
            if ("L".equals(part.modifier)) {
               YearMonth ym = YearMonth.of(date.getYear(), date.getMonth().getValue());
               return date.getDayOfMonth() == ym.lengthOfMonth() - (part.from == null ? 0 : part.from);
            }

            if ("W".equals(part.modifier)) {
               if (date.getDayOfWeek().getValue() <= 5) {
                  if (date.getDayOfMonth() == part.from) {
                     return true;
                  }

                  if (date.getDayOfWeek().getValue() == 5) {
                     return date.plusDays(1L).getDayOfMonth() == part.from;
                  }

                  if (date.getDayOfWeek().getValue() == 1) {
                     return date.minusDays(1L).getDayOfMonth() == part.from;
                  }
               }
            } else if (this.matches(date.getDayOfMonth(), part)) {
               return true;
            }
         }

         return false;
      }

      @Override
      protected void validatePart(CronExpression.FieldPart part) {
         if (part.modifier != null && Arrays.asList("L", "W", "?").indexOf(part.modifier) == -1) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.modifier));
         } else if (part.incrementModifier != null && !"/".equals(part.incrementModifier)) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.incrementModifier));
         }
      }

      @Override
      protected boolean matches(int val, CronExpression.FieldPart part) {
         return "?".equals(part.modifier) || super.matches(val, part);
      }
   }

   static class DayOfWeekField extends CronExpression.BasicField {
      static final int DAYS_IN_WEEK = 7;

      DayOfWeekField(String fieldExpr) {
         super(CronExpression.CronFieldType.DAY_OF_WEEK, fieldExpr);
      }

      boolean matches(LocalDate date) {
         for(CronExpression.FieldPart part : this.parts) {
            if ("L".equals(part.modifier)) {
               YearMonth ym = YearMonth.of(date.getYear(), date.getMonth().getValue());
               return date.getDayOfWeek() == DayOfWeek.of(part.from) && date.getDayOfMonth() > ym.lengthOfMonth() - 7;
            }

            if ("#".equals(part.incrementModifier)) {
               if (date.getDayOfWeek() == DayOfWeek.of(part.from)) {
                  int num = date.getDayOfMonth() / 7;
                  return part.increment == (date.getDayOfMonth() % 7 == 0 ? num : num + 1);
               }

               return false;
            }

            if (this.matches(date.getDayOfWeek().getValue(), part)) {
               return true;
            }
         }

         return false;
      }

      @Override
      protected Integer mapValue(String value) {
         return "0".equals(value) ? 7 : super.mapValue(value);
      }

      @Override
      protected boolean matches(int val, CronExpression.FieldPart part) {
         return "?".equals(part.modifier) || super.matches(val, part);
      }

      @Override
      protected void validatePart(CronExpression.FieldPart part) {
         if (part.modifier != null && Arrays.asList("L", "?").indexOf(part.modifier) == -1) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.modifier));
         } else if (part.incrementModifier != null && Arrays.asList("/", "#").indexOf(part.incrementModifier) == -1) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.incrementModifier));
         }
      }
   }

   static class FieldPart {
      private Integer from;
      private Integer to;
      private Integer increment;
      private String modifier;
      private String incrementModifier;
   }

   static class SimpleField extends CronExpression.BasicField {
      SimpleField(CronExpression.CronFieldType fieldType, String fieldExpr) {
         super(fieldType, fieldExpr);
      }

      public boolean matches(int val) {
         if (val >= this.fieldType.from && val <= this.fieldType.to) {
            for(CronExpression.FieldPart part : this.parts) {
               if (this.matches(val, part)) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}
