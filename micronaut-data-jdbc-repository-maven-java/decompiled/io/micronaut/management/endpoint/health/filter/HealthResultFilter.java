package io.micronaut.management.endpoint.health.filter;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthResult;
import org.reactivestreams.Publisher;

@Filter({"${endpoints.all.path:/}${endpoints.health.id:health}", "${endpoints.all.path:/}${endpoints.health.id:health}/liveness", "${endpoints.all.path:/}${endpoints.health.id:health}/readiness"})
@Requires(
   beans = {HealthEndpoint.class}
)
public class HealthResultFilter extends OncePerRequestHttpServerFilter {
   public static final String DEFAULT_MAPPING = "${endpoints.all.path:/}${endpoints.health.id:health}";
   public static final String LIVENESS_PROBE_MAPPING = "${endpoints.all.path:/}${endpoints.health.id:health}/liveness";
   public static final String READINESS_PROBE_MAPPING = "${endpoints.all.path:/}${endpoints.health.id:health}/readiness";
   private final HealthEndpoint healthEndpoint;

   protected HealthResultFilter(HealthEndpoint healthEndpoint) {
      this.healthEndpoint = healthEndpoint;
   }

   @Override
   protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
      return Publishers.map(chain.proceed(request), response -> {
         Object body = response.body();
         if (body instanceof HealthResult) {
            HealthResult healthResult = (HealthResult)body;
            HealthStatus status = healthResult.getStatus();
            HttpStatus httpStatus = (HttpStatus)this.healthEndpoint.getStatusConfiguration().getHttpMapping().get(status.getName());
            if (httpStatus != null) {
               response.status(httpStatus);
            } else {
               boolean operational = status.getOperational().orElse(true);
               if (!operational) {
                  response.status(HttpStatus.SERVICE_UNAVAILABLE);
               }
            }
         }

         return response;
      });
   }
}
