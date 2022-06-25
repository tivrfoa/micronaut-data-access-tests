package io.micronaut.jackson.databind;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.json.bind.JsonBeanPropertyBinderExceptionHandler;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
final class DatabindPropertyBinderExceptionHandler implements JsonBeanPropertyBinderExceptionHandler {
   @Override
   public Optional<ConversionErrorException> toConversionError(@Nullable Object object, @NonNull Exception e) {
      if (e instanceof InvalidFormatException) {
         InvalidFormatException ife = (InvalidFormatException)e;
         final Object originalValue = ife.getValue();
         ConversionError conversionError = new ConversionError() {
            @Override
            public Exception getCause() {
               return e;
            }

            @Override
            public Optional<Object> getOriginalValue() {
               return Optional.ofNullable(originalValue);
            }
         };
         Class<?> type = object != null ? object.getClass() : Object.class;
         List<JsonMappingException.Reference> path = ife.getPath();
         String name;
         if (!path.isEmpty()) {
            name = ((JsonMappingException.Reference)path.get(path.size() - 1)).getFieldName();
         } else {
            name = NameUtils.decapitalize(type.getSimpleName());
         }

         return Optional.of(new ConversionErrorException(Argument.of(ife.getTargetType(), name), conversionError));
      } else {
         return Optional.empty();
      }
   }
}
