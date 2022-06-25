package io.micronaut.http.util;

import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class OutgoingHttpRequestProcessorImpl implements OutgoingHttpRequestProcessor {
   @Override
   public boolean shouldProcessRequest(OutgointRequestProcessorMatcher matcher, HttpRequest<?> request) {
      Optional<String> serviceId = request.getAttribute(HttpAttributes.SERVICE_ID.toString(), String.class);
      String uri = request.getUri().toString();
      return this.shouldProcessRequest(matcher, (String)serviceId.orElse(null), uri);
   }

   public boolean shouldProcessRequest(OutgointRequestProcessorMatcher matcher, String serviceId, String uri) {
      if (matcher.getServiceIdPattern() != null && serviceId != null && matcher.getServiceIdPattern().matcher(serviceId).matches()) {
         return true;
      } else {
         return matcher.getUriPattern() != null && uri != null && matcher.getUriPattern().matcher(uri).matches();
      }
   }
}
