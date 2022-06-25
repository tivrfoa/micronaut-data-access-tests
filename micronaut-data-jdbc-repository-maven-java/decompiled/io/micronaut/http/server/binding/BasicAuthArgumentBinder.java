package io.micronaut.http.server.binding;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.BasicAuth;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Singleton
public class BasicAuthArgumentBinder implements TypedRequestArgumentBinder<BasicAuth> {
   @Override
   public Argument<BasicAuth> argumentType() {
      return Argument.of(BasicAuth.class);
   }

   public ArgumentBinder.BindingResult<BasicAuth> bind(ArgumentConversionContext<BasicAuth> context, HttpRequest<?> source) {
      String authorization = source.getHeaders().get("Authorization");
      if (authorization != null && authorization.startsWith("Basic")) {
         String base64Credentials = authorization.substring(6);
         byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
         String credentials = new String(credDecoded, StandardCharsets.UTF_8);
         String[] values = credentials.split(":", 2);
         if (values.length == 2) {
            return () -> Optional.of(new BasicAuth(values[0], values[1]));
         }
      }

      return ArgumentBinder.BindingResult.EMPTY;
   }
}
