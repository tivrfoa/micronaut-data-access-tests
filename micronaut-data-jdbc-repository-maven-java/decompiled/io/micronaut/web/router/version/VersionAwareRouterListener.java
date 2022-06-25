package io.micronaut.web.router.version;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.filter.FilteredRouter;
import jakarta.inject.Singleton;

@Singleton
@Requirements({@Requires(
   property = "micronaut.router.versioning.enabled",
   value = "true"
), @Requires(
   beans = {RoutesVersioningConfiguration.class}
)})
public class VersionAwareRouterListener implements BeanCreatedEventListener<Router> {
   private final VersionRouteMatchFilter routeVersionFilter;

   public VersionAwareRouterListener(VersionRouteMatchFilter filter) {
      this.routeVersionFilter = filter;
   }

   public Router onCreated(BeanCreatedEvent<Router> event) {
      return new FilteredRouter(event.getBean(), this.routeVersionFilter);
   }
}
