package io.micronaut.http.client.exceptions;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.Described;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.VndError;
import java.util.Optional;

@Internal
public interface HttpClientErrorDecoder {
   HttpClientErrorDecoder DEFAULT = new HttpClientErrorDecoder() {
   };

   default Optional<String> getMessage(Object error) {
      if (error == null) {
         return Optional.empty();
      } else if (error instanceof JsonError) {
         return Optional.ofNullable(((JsonError)error).getMessage());
      } else {
         return error instanceof Described ? Optional.ofNullable(((Described)error).getDescription()) : Optional.of(error.toString());
      }
   }

   default Argument<?> getErrorType(MediaType mediaType) {
      if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
         return Argument.of(JsonError.class);
      } else {
         return mediaType.equals(MediaType.APPLICATION_VND_ERROR_TYPE) ? Argument.of(VndError.class) : Argument.STRING;
      }
   }
}
