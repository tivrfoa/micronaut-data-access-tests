package io.micronaut.discovery;

import io.micronaut.context.annotation.Primary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Primary
@Singleton
public class DefaultCompositeDiscoveryClient extends CompositeDiscoveryClient {
   @Inject
   public DefaultCompositeDiscoveryClient(List<DiscoveryClient> discoveryClients) {
      super((DiscoveryClient[])discoveryClients.toArray(new DiscoveryClient[0]));
   }

   public DefaultCompositeDiscoveryClient(DiscoveryClient... discoveryClients) {
      super(discoveryClients);
   }
}
