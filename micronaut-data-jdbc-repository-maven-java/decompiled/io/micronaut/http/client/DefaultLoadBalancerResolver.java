package io.micronaut.http.client;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.ServiceInstanceList;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;
import io.micronaut.http.client.loadbalance.ServiceInstanceListLoadBalancerFactory;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@BootstrapContextCompatible
public class DefaultLoadBalancerResolver implements LoadBalancerResolver {
   private final Map<String, ServiceInstanceList> serviceInstanceLists;
   private final BeanContext beanContext;

   public DefaultLoadBalancerResolver(BeanContext beanContext, ServiceInstanceList... serviceInstanceLists) {
      this(beanContext, Arrays.asList(serviceInstanceLists));
   }

   @Inject
   public DefaultLoadBalancerResolver(BeanContext beanContext, List<ServiceInstanceList> serviceInstanceLists) {
      this.beanContext = beanContext;
      if (CollectionUtils.isNotEmpty(serviceInstanceLists)) {
         this.serviceInstanceLists = new HashMap(serviceInstanceLists.size());

         for(ServiceInstanceList provider : serviceInstanceLists) {
            this.serviceInstanceLists.put(provider.getID(), provider);
         }
      } else {
         this.serviceInstanceLists = Collections.emptyMap();
      }

   }

   @Override
   public Optional<? extends LoadBalancer> resolve(String... serviceReferences) {
      if (!ArrayUtils.isEmpty(serviceReferences) && !StringUtils.isEmpty(serviceReferences[0])) {
         String reference = serviceReferences[0];
         if (!reference.startsWith("/")) {
            if (reference.indexOf(47) > -1) {
               try {
                  URI uri = new URI(reference);
                  return Optional.of(LoadBalancer.fixed(uri));
               } catch (URISyntaxException var5) {
                  return Optional.empty();
               }
            } else {
               reference = NameUtils.hyphenate(reference);
               return this.resolveLoadBalancerForServiceID(reference);
            }
         } else if (this.beanContext.containsBean(EmbeddedServer.class)) {
            EmbeddedServer embeddedServer = this.beanContext.getBean(EmbeddedServer.class);
            URI uri = embeddedServer.getURI();
            return Optional.of(LoadBalancer.fixed(uri));
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   protected Optional<? extends LoadBalancer> resolveLoadBalancerForServiceID(String serviceID) {
      if (this.serviceInstanceLists.containsKey(serviceID)) {
         ServiceInstanceList serviceInstanceList = (ServiceInstanceList)this.serviceInstanceLists.get(serviceID);
         LoadBalancer loadBalancer = this.beanContext
            .<ServiceInstanceListLoadBalancerFactory>getBean(ServiceInstanceListLoadBalancerFactory.class)
            .create(serviceInstanceList);
         return Optional.ofNullable(loadBalancer);
      } else {
         LoadBalancer loadBalancer = this.beanContext.<DiscoveryClientLoadBalancerFactory>getBean(DiscoveryClientLoadBalancerFactory.class).create(serviceID);
         return Optional.of(loadBalancer);
      }
   }
}
