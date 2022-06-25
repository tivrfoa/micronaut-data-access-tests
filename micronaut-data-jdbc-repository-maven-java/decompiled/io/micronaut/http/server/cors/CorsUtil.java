package io.micronaut.http.server.cors;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import java.util.Optional;

class CorsUtil {
   static boolean isPreflightRequest(HttpRequest request) {
      HttpHeaders headers = request.getHeaders();
      Optional<String> origin = headers.getOrigin();
      return origin.isPresent() && headers.contains("Access-Control-Request-Method") && HttpMethod.OPTIONS == request.getMethod();
   }
}
