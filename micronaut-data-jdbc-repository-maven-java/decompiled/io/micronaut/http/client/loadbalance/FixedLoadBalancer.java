package io.micronaut.http.client.loadbalance;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.http.client.LoadBalancer;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import org.reactivestreams.Publisher;

public class FixedLoadBalancer implements LoadBalancer {
   private final Publisher<ServiceInstance> publisher;
   private final URI uri;

   @Deprecated
   public FixedLoadBalancer(URL url) {
      this(toUriUnchecked(url));
   }

   public FixedLoadBalancer(URI uri) {
      this.uri = uri;
      this.publisher = Publishers.just(ServiceInstance.of(uri.getHost(), uri));
   }

   @Override
   public Publisher<ServiceInstance> select(@Nullable Object discriminator) {
      return this.publisher;
   }

   @Deprecated
   public URL getUrl() {
      try {
         return this.uri.toURL();
      } catch (MalformedURLException var2) {
         throw new UncheckedIOException(var2);
      }
   }

   public URI getUri() {
      return this.uri;
   }

   @Override
   public Optional<String> getContextPath() {
      return Optional.ofNullable(this.getUri().getPath());
   }

   private static URI toUriUnchecked(URL url) {
      try {
         return url.toURI();
      } catch (URISyntaxException var2) {
         throw new IllegalArgumentException("Illegal URI", var2);
      }
   }
}
