package io.micronaut.core.convert.format;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import java.util.Locale;
import java.util.Optional;

public class ReadableBytesTypeConverter implements FormattingTypeConverter<CharSequence, Number, ReadableBytes> {
   private static final int KB_UNIT = 1024;

   @Override
   public Class<ReadableBytes> annotationType() {
      return ReadableBytes.class;
   }

   public Optional<Number> convert(CharSequence object, Class<Number> targetType, ConversionContext context) {
      if (StringUtils.isEmpty(object)) {
         return Optional.empty();
      } else {
         String value = object.toString().toUpperCase(Locale.ENGLISH);

         try {
            if (value.endsWith("KB")) {
               long size = Long.valueOf(value.substring(0, value.length() - 2)) * 1024L;
               return ConversionService.SHARED.convert(size, targetType);
            } else if (value.endsWith("MB")) {
               long size = Long.valueOf(value.substring(0, value.length() - 2)) * 1024L * 1024L;
               return ConversionService.SHARED.convert(size, targetType);
            } else if (value.endsWith("GB")) {
               long size = Long.valueOf(value.substring(0, value.length() - 2)) * 1024L * 1024L * 1024L;
               return ConversionService.SHARED.convert(size, targetType);
            } else {
               Long size = Long.valueOf(value);
               return ConversionService.SHARED.convert(size, targetType);
            }
         } catch (NumberFormatException var7) {
            context.reject(value, var7);
            return Optional.empty();
         }
      }
   }
}
