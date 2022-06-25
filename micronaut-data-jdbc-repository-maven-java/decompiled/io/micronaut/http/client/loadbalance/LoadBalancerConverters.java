package io.micronaut.http.client.loadbalance;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.http.client.LoadBalancer;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Singleton
public class LoadBalancerConverters implements TypeConverterRegistrar {
   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(URI.class, LoadBalancer.class, LoadBalancer::fixed);
      conversionService.addConverter(URL.class, LoadBalancer.class, LoadBalancer::fixed);
      conversionService.addConverter(String.class, LoadBalancer.class, url -> {
         try {
            return LoadBalancer.fixed(new URI(url));
         } catch (URISyntaxException var2) {
            return null;
         }
      });
   }
}
