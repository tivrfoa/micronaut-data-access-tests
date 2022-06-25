package io.micronaut.http.server.cors;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ImmutableArgumentConversionContext;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.ConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class CorsOriginConverter implements TypeConverter<Map<String, Object>, CorsOriginConfiguration> {
   private static final String ALLOWED_ORIGINS = "allowed-origins";
   private static final String ALLOWED_METHODS = "allowed-methods";
   private static final String ALLOWED_HEADERS = "allowed-headers";
   private static final String EXPOSED_HEADERS = "exposed-headers";
   private static final String ALLOW_CREDENTIALS = "allow-credentials";
   private static final String MAX_AGE = "max-age";
   private static final ArgumentConversionContext<List<HttpMethod>> CONVERSION_CONTEXT_LIST_OF_HTTP_METHOD = ImmutableArgumentConversionContext.of(
      Argument.listOf(HttpMethod.class)
   );

   public Optional<CorsOriginConfiguration> convert(Map<String, Object> object, Class<CorsOriginConfiguration> targetType, ConversionContext context) {
      CorsOriginConfiguration configuration = new CorsOriginConfiguration();
      ConvertibleValues<Object> convertibleValues = new ConvertibleValuesMap<>(object);
      convertibleValues.get("allowed-origins", ConversionContext.LIST_OF_STRING).ifPresent(configuration::setAllowedOrigins);
      convertibleValues.get("allowed-methods", CONVERSION_CONTEXT_LIST_OF_HTTP_METHOD).ifPresent(configuration::setAllowedMethods);
      convertibleValues.get("allowed-headers", ConversionContext.LIST_OF_STRING).ifPresent(configuration::setAllowedHeaders);
      convertibleValues.get("exposed-headers", ConversionContext.LIST_OF_STRING).ifPresent(configuration::setExposedHeaders);
      convertibleValues.get("allow-credentials", ConversionContext.BOOLEAN).ifPresent(configuration::setAllowCredentials);
      convertibleValues.get("max-age", ConversionContext.LONG).ifPresent(configuration::setMaxAge);
      return Optional.of(configuration);
   }
}
