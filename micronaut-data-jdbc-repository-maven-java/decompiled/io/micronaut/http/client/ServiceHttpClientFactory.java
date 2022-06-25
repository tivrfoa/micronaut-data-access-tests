package io.micronaut.http.client;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.discovery.StaticServiceInstanceList;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.scheduling.TaskScheduler;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import reactor.core.publisher.Flux;

@Factory
@Internal
public class ServiceHttpClientFactory {
   private final TaskScheduler taskScheduler;
   private final BeanProvider<HttpClientRegistry<?>> clientFactory;

   public ServiceHttpClientFactory(TaskScheduler taskScheduler, BeanProvider<HttpClientRegistry<?>> clientFactory) {
      this.taskScheduler = taskScheduler;
      this.clientFactory = clientFactory;
   }

   @EachBean(ServiceHttpClientConfiguration.class)
   @Requires(
      condition = ServiceHttpClientCondition.class
   )
   StaticServiceInstanceList serviceInstanceList(ServiceHttpClientConfiguration configuration) {
      List<URI> originalURLs = configuration.getUrls();
      Collection<URI> loadBalancedURIs = new ConcurrentLinkedQueue(originalURLs);
      return new StaticServiceInstanceList(configuration.getServiceId(), loadBalancedURIs, (String)configuration.getPath().orElse(null));
   }

   @EachBean(ServiceHttpClientConfiguration.class)
   @Requires(
      condition = ServiceHttpClientCondition.class
   )
   ApplicationEventListener<ServerStartupEvent> healthCheckStarter(
      @Parameter ServiceHttpClientConfiguration configuration, @Parameter StaticServiceInstanceList instanceList
   ) {
      if (configuration.isHealthCheck()) {
         return event -> {
            List<URI> originalURLs = configuration.getUrls();
            Collection<URI> loadBalancedURIs = instanceList.getLoadBalancedURIs();
            HttpClient httpClient = this.clientFactory
               .get()
               .getClient(configuration.getHttpVersion(), configuration.getServiceId(), (String)configuration.getPath().orElse(null));
            Duration initialDelay = configuration.getHealthCheckInterval();
            Duration delay = configuration.getHealthCheckInterval();
            this.taskScheduler.scheduleWithFixedDelay(initialDelay, delay, () -> Flux.fromIterable(originalURLs).flatMap(originalURI -> {
                  URI healthCheckURI = originalURI.resolve(configuration.getHealthCheckUri());
                  return Flux.from(httpClient.exchange(HttpRequest.GET(healthCheckURI))).onErrorResume(throwable -> {
                     if (throwable instanceof HttpClientResponseException) {
                        HttpClientResponseException responseException = (HttpClientResponseException)throwable;
                        return Flux.just(responseException.getResponse());
                     } else {
                        return Flux.just(HttpResponse.serverError());
                     }
                  }).map(response -> Collections.singletonMap(originalURI, response.getStatus()));
               }).subscribe(uriToStatusMap -> {
                  Entry<URI, HttpStatus> entry = (Entry)uriToStatusMap.entrySet().iterator().next();
                  URI uri = (URI)entry.getKey();
                  HttpStatus status = (HttpStatus)entry.getValue();
                  if (status.getCode() >= 300) {
                     loadBalancedURIs.remove(uri);
                  } else if (!loadBalancedURIs.contains(uri)) {
                     loadBalancedURIs.add(uri);
                  }

               }));
         };
      } else {
         throw new DisabledBeanException("HTTP Client Health Check not enabled");
      }
   }
}
