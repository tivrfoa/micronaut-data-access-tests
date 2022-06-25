package io.micronaut.management.endpoint.beans.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.management.endpoint.beans.BeanDefinitionData;
import io.micronaut.management.endpoint.beans.BeanDefinitionDataCollector;
import io.micronaut.management.endpoint.beans.BeansEndpoint;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@Requires(
   beans = {BeansEndpoint.class}
)
public class DefaultBeanDefinitionDataCollector implements BeanDefinitionDataCollector<Map<String, Object>> {
   private BeanDefinitionData beanDefinitionData;

   DefaultBeanDefinitionDataCollector(BeanDefinitionData beanDefinitionData) {
      this.beanDefinitionData = beanDefinitionData;
   }

   @Override
   public Publisher<Map<String, Object>> getData(Collection<BeanDefinition<?>> beanDefinitions) {
      return Mono.from(this.getBeans(beanDefinitions)).map(beans -> {
         Map<String, Object> beanData = new LinkedHashMap(1);
         beanData.put("beans", beans);
         return beanData;
      });
   }

   protected Publisher<Map<String, Object>> getBeans(Collection<BeanDefinition<?>> definitions) {
      return Flux.fromIterable(definitions)
         .collectMap(definition -> definition.getClass().getName(), definition -> this.beanDefinitionData.getData(definition));
   }
}
