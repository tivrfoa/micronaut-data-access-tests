package io.micronaut.http.cookie;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SameSiteConverter implements TypeConverter<CharSequence, SameSite> {
   private static final Map<CharSequence, SameSite> CONVERSIONS = new ConcurrentHashMap();

   public Optional<SameSite> convert(CharSequence object, Class<SameSite> targetType, ConversionContext context) {
      return Optional.ofNullable(CONVERSIONS.computeIfAbsent(object, charSequence -> {
         if (object == null) {
            return null;
         } else {
            try {
               return SameSite.valueOf(StringUtils.capitalize(object.toString().toLowerCase()));
            } catch (IllegalArgumentException var3x) {
               return null;
            }
         }
      }));
   }
}
