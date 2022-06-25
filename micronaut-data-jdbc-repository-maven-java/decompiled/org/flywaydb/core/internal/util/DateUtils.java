package org.flywaydb.core.internal.util;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
   public static String formatDateAsIsoString(Date date) {
      return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
   }

   public static String formatTimeAsIsoString(Date date) {
      return date == null ? "" : new SimpleDateFormat("HH:mm:ss").format(date);
   }

   public static Date toDate(int year, int month, int day) {
      return new GregorianCalendar(year, month - 1, day).getTime();
   }

   public static String toDateString(Date date) {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      String year = "" + calendar.get(1);
      String month = StringUtils.trimOrLeftPad("" + (calendar.get(2) + 1), 2, '0');
      String day = StringUtils.trimOrLeftPad("" + calendar.get(5), 2, '0');
      return year + "-" + month + "-" + day;
   }

   public static Date addDaysToDate(Date fromDate, int days) {
      return Date.from(fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays((long)days).atStartOfDay(ZoneId.systemDefault()).toInstant());
   }

   private DateUtils() {
   }
}
