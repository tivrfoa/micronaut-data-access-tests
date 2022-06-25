package com.fasterxml.jackson.datatype.jsr310.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DurationUnitConverter {
   private static final Map<String, DurationUnitConverter.DurationSerialization> UNITS;
   final DurationUnitConverter.DurationSerialization serialization;

   DurationUnitConverter(DurationUnitConverter.DurationSerialization serialization) {
      this.serialization = serialization;
   }

   public Duration convert(long value) {
      return (Duration)this.serialization.deserializer.apply(value);
   }

   public long convert(Duration duration) {
      return this.serialization.serializer.apply(duration);
   }

   public static String descForAllowed() {
      return "\"" + (String)UNITS.keySet().stream().collect(Collectors.joining("\", \"")) + "\"";
   }

   public static DurationUnitConverter from(String unit) {
      DurationUnitConverter.DurationSerialization def = (DurationUnitConverter.DurationSerialization)UNITS.get(unit);
      return def == null ? null : new DurationUnitConverter(def);
   }

   static {
      Map<String, DurationUnitConverter.DurationSerialization> units = new LinkedHashMap();
      units.put(
         ChronoUnit.NANOS.name(),
         new DurationUnitConverter.DurationSerialization(Duration::toNanos, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.NANOS))
      );
      units.put(
         ChronoUnit.MICROS.name(),
         new DurationUnitConverter.DurationSerialization(d -> d.toNanos() / 1000L, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.MICROS))
      );
      units.put(
         ChronoUnit.MILLIS.name(),
         new DurationUnitConverter.DurationSerialization(Duration::toMillis, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.MILLIS))
      );
      units.put(
         ChronoUnit.SECONDS.name(),
         new DurationUnitConverter.DurationSerialization(Duration::getSeconds, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.SECONDS))
      );
      units.put(
         ChronoUnit.MINUTES.name(),
         new DurationUnitConverter.DurationSerialization(Duration::toMinutes, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.MINUTES))
      );
      units.put(
         ChronoUnit.HOURS.name(),
         new DurationUnitConverter.DurationSerialization(Duration::toHours, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.HOURS))
      );
      units.put(
         ChronoUnit.HALF_DAYS.name(),
         new DurationUnitConverter.DurationSerialization(d -> d.toHours() / 12L, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.HALF_DAYS))
      );
      units.put(
         ChronoUnit.DAYS.name(),
         new DurationUnitConverter.DurationSerialization(Duration::toDays, DurationUnitConverter.DurationSerialization.deserializer(ChronoUnit.DAYS))
      );
      UNITS = units;
   }

   protected static class DurationSerialization {
      final Function<Duration, Long> serializer;
      final Function<Long, Duration> deserializer;

      DurationSerialization(Function<Duration, Long> serializer, Function<Long, Duration> deserializer) {
         this.serializer = serializer;
         this.deserializer = deserializer;
      }

      static Function<Long, Duration> deserializer(TemporalUnit unit) {
         return v -> Duration.of(v, unit);
      }
   }
}
