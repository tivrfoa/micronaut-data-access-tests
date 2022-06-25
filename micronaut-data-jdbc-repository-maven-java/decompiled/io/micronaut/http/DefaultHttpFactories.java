package io.micronaut.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.http.simple.SimpleHttpRequestFactory;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class DefaultHttpFactories {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpFactories.class);

   static HttpRequestFactory resolveDefaultRequestFactory() {
      Optional<ServiceDefinition<HttpRequestFactory>> definition = SoftServiceLoader.<HttpRequestFactory>load(HttpRequestFactory.class)
         .firstOr("io.micronaut.http.client.NettyClientHttpRequestFactory", HttpRequestFactory.class.getClassLoader());
      if (definition.isPresent()) {
         ServiceDefinition<HttpRequestFactory> sd = (ServiceDefinition)definition.get();

         try {
            return sd.load();
         } catch (Throwable var3) {
            LOG.warn("Unable to load default request factory for definition [" + definition + "]: " + var3.getMessage(), var3);
         }
      }

      return new SimpleHttpRequestFactory();
   }

   static HttpResponseFactory resolveDefaultResponseFactory() {
      Optional<ServiceDefinition<HttpResponseFactory>> definition = SoftServiceLoader.<HttpResponseFactory>load(HttpResponseFactory.class)
         .firstOr("io.micronaut.http.server.netty.NettyHttpResponseFactory", HttpResponseFactory.class.getClassLoader());
      if (definition.isPresent()) {
         ServiceDefinition<HttpResponseFactory> sd = (ServiceDefinition)definition.get();

         try {
            return sd.load();
         } catch (Throwable var3) {
            LOG.warn("Unable to load default response factory for definition [" + definition + "]: " + var3.getMessage(), var3);
         }
      }

      return new SimpleHttpResponseFactory();
   }
}
