package io.micronaut.web.router.exceptions;

import io.micronaut.web.router.UriRouteMatch;
import java.util.List;
import java.util.stream.Collectors;

public class DuplicateRouteException extends RoutingException {
   private final String uri;
   private final List<UriRouteMatch<Object, Object>> uriRoutes;

   public DuplicateRouteException(String uri, List<UriRouteMatch<Object, Object>> uriRoutes) {
      super(buildMessage(uri, uriRoutes));
      this.uri = uri;
      this.uriRoutes = uriRoutes;
   }

   public String getUri() {
      return this.uri;
   }

   public List<UriRouteMatch<Object, Object>> getUriRoutes() {
      return this.uriRoutes;
   }

   private static String buildMessage(String uri, List<UriRouteMatch<Object, Object>> uriRoutes) {
      StringBuilder message = new StringBuilder("More than 1 route matched the incoming request. The following routes matched ");
      message.append(uri).append(": ");
      message.append((String)uriRoutes.stream().map(Object::toString).collect(Collectors.joining(", ")));
      return message.toString();
   }
}
