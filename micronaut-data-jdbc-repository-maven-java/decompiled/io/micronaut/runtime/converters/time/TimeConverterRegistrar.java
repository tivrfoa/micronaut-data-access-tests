package io.micronaut.runtime.converters.time;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.convert.format.Format;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Requires(
   notEnv = {"android"}
)
@BootstrapContextCompatible
@TypeHint(
   value = {Duration.class, TemporalAmount.class, Instant.class, LocalDate.class, LocalDateTime.class, MonthDay.class, OffsetDateTime.class, OffsetTime.class, Period.class, Year.class, YearMonth.class, ZonedDateTime.class, ZoneId.class, ZoneOffset.class},
   accessType = {TypeHint.AccessType.ALL_PUBLIC}
)
public class TimeConverterRegistrar implements TypeConverterRegistrar {
   private static final Pattern DURATION_MATCHER = Pattern.compile("^(-?\\d+)([unsmhd])(s?)$");
   private static final int MILLIS = 3;

   @Override
   public void register(ConversionService<?> conversionService) {
      BiFunction<CharSequence, ConversionContext, Optional<Duration>> durationConverter = (object, context) -> {
         String value = object.toString().trim();
         if (value.startsWith("P")) {
            try {
               return Optional.of(Duration.parse(value));
            } catch (DateTimeParseException var11) {
               context.reject(value, var11);
               return Optional.empty();
            }
         } else {
            Matcher matcher = DURATION_MATCHER.matcher(value);
            if (matcher.find()) {
               String amount = matcher.group(1);
               String g2 = matcher.group(2);
               char type = g2.charAt(0);

               try {
                  switch(type) {
                     case 'd':
                        return Optional.of(Duration.ofDays((long)Integer.parseInt(amount)));
                     case 'h':
                        return Optional.of(Duration.ofHours((long)Integer.parseInt(amount)));
                     case 'm':
                        String ms = matcher.group(3);
                        if (StringUtils.hasText(ms)) {
                           return Optional.of(Duration.ofMillis((long)Integer.parseInt(amount)));
                        }

                        return Optional.of(Duration.ofMinutes((long)Integer.parseInt(amount)));
                     case 's':
                        return Optional.of(Duration.ofSeconds((long)Integer.parseInt(amount)));
                     default:
                        String seq = g2 + matcher.group(3);
                        byte var10 = -1;
                        switch(seq.hashCode()) {
                           case 3525:
                              if (seq.equals("ns")) {
                                 var10 = 0;
                              }
                           default:
                              switch(var10) {
                                 case 0:
                                    return Optional.of(Duration.ofNanos((long)Integer.parseInt(amount)));
                                 default:
                                    context.reject(
                                       value,
                                       new DateTimeParseException(
                                          "Unparseable date format ("
                                             + value
                                             + "). Should either be a ISO-8601 duration or a round number followed by the unit type",
                                          value,
                                          0
                                       )
                                    );
                                    return Optional.empty();
                              }
                        }
                  }
               } catch (NumberFormatException var12) {
                  context.reject(value, var12);
               }
            }

            return Optional.empty();
         }
      };
      conversionService.addConverter(
         CharSequence.class, Duration.class, (TypeConverter)((object, targetType, context) -> (Optional)durationConverter.apply(object, context))
      );
      conversionService.addConverter(
         CharSequence.class,
         TemporalAmount.class,
         (TypeConverter)((object, targetType, context) -> ((Optional)durationConverter.apply(object, context)).map(TemporalAmount.class::cast))
      );
      conversionService.addConverter(CharSequence.class, LocalDateTime.class, (TypeConverter)((object, targetType, context) -> {
         try {
            DateTimeFormatter formatter = this.resolveFormatter(context);
            LocalDateTime result = LocalDateTime.parse(object, formatter);
            return Optional.of(result);
         } catch (DateTimeParseException var6) {
            context.reject(object, var6);
            return Optional.empty();
         }
      }));
      TypeConverter<TemporalAccessor, CharSequence> temporalConverter = (object, targetType, context) -> {
         try {
            DateTimeFormatter formatter = this.resolveFormatter(context);
            return Optional.of(formatter.format(object));
         } catch (DateTimeParseException var5) {
            context.reject(object, var5);
            return Optional.empty();
         }
      };
      conversionService.addConverter(TemporalAccessor.class, CharSequence.class, temporalConverter);
      conversionService.addConverter(CharSequence.class, LocalDate.class, (TypeConverter)((object, targetType, context) -> {
         try {
            DateTimeFormatter formatter = this.resolveFormatter(context);
            LocalDate result = LocalDate.parse(object, formatter);
            return Optional.of(result);
         } catch (DateTimeParseException var6) {
            context.reject(object, var6);
            return Optional.empty();
         }
      }));
      conversionService.addConverter(CharSequence.class, ZonedDateTime.class, (TypeConverter)((object, targetType, context) -> {
         try {
            DateTimeFormatter formatter = this.resolveFormatter(context);
            ZonedDateTime result = ZonedDateTime.parse(object, formatter);
            return Optional.of(result);
         } catch (DateTimeParseException var6) {
            context.reject(object, var6);
            return Optional.empty();
         }
      }));
      conversionService.addConverter(CharSequence.class, OffsetDateTime.class, (TypeConverter)((object, targetType, context) -> {
         try {
            DateTimeFormatter formatter = this.resolveFormatter(context);
            OffsetDateTime result = OffsetDateTime.parse(object, formatter);
            return Optional.of(result);
         } catch (DateTimeParseException var6) {
            context.reject(object, var6);
            return Optional.empty();
         }
      }));
   }

   private DateTimeFormatter resolveFormatter(ConversionContext context) {
      Optional<String> format = context.getAnnotationMetadata().stringValue(Format.class);
      return (DateTimeFormatter)format.map(pattern -> DateTimeFormatter.ofPattern(pattern, context.getLocale())).orElse(DateTimeFormatter.RFC_1123_DATE_TIME);
   }
}
