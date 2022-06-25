package io.micronaut.web.router;

import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteMatchUtils {
   private static final Logger LOG = LoggerFactory.getLogger(RouteMatchUtils.class);

   public static Optional<RouteMatch> findRouteMatch(HttpRequest<?> request) {
      Optional<RouteMatch> routeMatchAttribute = request.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch.class);
      if (routeMatchAttribute.isPresent()) {
         return routeMatchAttribute;
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Route match attribute for request ({}) not found", request.getPath());
         }

         return Optional.empty();
      }
   }
}
