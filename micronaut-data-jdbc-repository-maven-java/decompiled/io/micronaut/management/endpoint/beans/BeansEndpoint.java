package io.micronaut.management.endpoint.beans;

import io.micronaut.context.BeanContext;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Endpoint("beans")
public class BeansEndpoint {
   private BeanContext beanContext;
   private BeanDefinitionDataCollector beanDefinitionDataCollector;

   public BeansEndpoint(BeanContext beanContext, BeanDefinitionDataCollector beanDefinitionDataCollector) {
      this.beanContext = beanContext;
      this.beanDefinitionDataCollector = beanDefinitionDataCollector;
   }

   @Read
   @SingleResult
   public Publisher<?> getBeans() {
      List<BeanDefinition<?>> beanDefinitions = (List)this.beanContext
         .getAllBeanDefinitions()
         .stream()
         .sorted(Comparator.comparing(bd -> bd.getClass().getName()))
         .collect(Collectors.toList());
      return Mono.from(this.beanDefinitionDataCollector.getData(beanDefinitions)).defaultIfEmpty(Collections.emptyMap());
   }
}
