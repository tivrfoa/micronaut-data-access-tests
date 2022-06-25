package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class YearMonthKeyDeserializer extends Jsr310KeyDeserializer {
   public static final YearMonthKeyDeserializer INSTANCE = new YearMonthKeyDeserializer();
   private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-')
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .toFormatter();

   protected YearMonthKeyDeserializer() {
   }

   protected YearMonth deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return YearMonth.parse(key, FORMATTER);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, YearMonth.class, var4, key);
      }
   }
}
