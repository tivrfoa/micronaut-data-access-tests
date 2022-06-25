package io.micronaut.http.server.netty.jackson;

import com.fasterxml.jackson.annotation.JsonView;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.json.JsonConfiguration;
import io.micronaut.web.router.RouteInfo;
import jakarta.inject.Named;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Requirements({@Requires(
   beans = {JsonConfiguration.class}
), @Requires(
   classes = {JsonView.class}
), @Requires(
   property = "jackson.json-view.enabled"
)})
@Filter({"/**"})
public class JsonViewServerFilter implements HttpServerFilter {
   public static final String PROPERTY_JSON_VIEW_ENABLED = "jackson.json-view.enabled";
   private final JsonViewCodecResolver codecFactory;
   private final ExecutorService executorService;

   public JsonViewServerFilter(JsonViewCodecResolver jsonViewCodecResolver, @Named("io") ExecutorService executorService) {
      this.codecFactory = jsonViewCodecResolver;
      this.executorService = executorService;
   }

   @Override
   public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
      RouteInfo<?> routeInfo = (RouteInfo)request.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class).orElse(null);
      Publisher<MutableHttpResponse<?>> responsePublisher = chain.proceed(request);
      if (routeInfo != null) {
         Optional<Class<?>> viewClass = routeInfo.findAnnotation(JsonView.class).flatMap(AnnotationValue::classValue);
         if (viewClass.isPresent()) {
            return Flux.from(responsePublisher)
               .switchMap(
                  response -> {
                     Optional<?> optionalBody = response.getBody();
                     if (optionalBody.isPresent()) {
                        Object body = optionalBody.get();
                        MediaTypeCodec codec = this.codecFactory.resolveJsonViewCodec((Class<?>)viewClass.get());
                        if (!Publishers.isConvertibleToPublisher(body)) {
                           return Mono.fromCallable(() -> {
                              byte[] encoded = codec.encode(routeInfo.getBodyType(), body);
                              response.body(encoded);
                              return response;
                           }).subscribeOn(Schedulers.fromExecutorService(this.executorService));
                        }
      
                        Publisher<?> pub = Publishers.convertPublisher(body, Publisher.class);
                        response.body(
                           Flux.from(pub).map(o -> codec.encode(routeInfo.getBodyType(), o)).subscribeOn(Schedulers.fromExecutorService(this.executorService))
                        );
                     }
      
                     return Flux.just(response);
                  }
               );
         }
      }

      return responsePublisher;
   }

   @Override
   public int getOrder() {
      return ServerFilterPhase.RENDERING.order();
   }
}
