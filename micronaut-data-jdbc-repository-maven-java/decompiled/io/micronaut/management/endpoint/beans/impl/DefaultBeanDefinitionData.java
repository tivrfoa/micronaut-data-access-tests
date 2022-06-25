package io.micronaut.management.endpoint.beans.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.management.endpoint.beans.BeanDefinitionData;
import io.micronaut.management.endpoint.beans.BeansEndpoint;
import jakarta.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Requires(
   beans = {BeansEndpoint.class}
)
public class DefaultBeanDefinitionData implements BeanDefinitionData<Map<String, Object>> {
   DefaultBeanDefinitionData() {
   }

   public Map<String, Object> getData(BeanDefinition<?> beanDefinition) {
      Map<String, Object> beanData = new LinkedHashMap(3);
      beanData.put("dependencies", this.getDependencies(beanDefinition));
      beanData.put("scope", this.getScope(beanDefinition));
      beanData.put("type", this.getType(beanDefinition));
      return beanData;
   }

   protected List getDependencies(BeanDefinition<?> beanDefinition) {
      return (List)beanDefinition.getRequiredComponents().stream().map(Class::getName).sorted().collect(Collectors.toList());
   }

   protected String getScope(BeanDefinition<?> beanDefinition) {
      return (String)beanDefinition.getScopeName().orElse(null);
   }

   protected String getType(BeanDefinition<?> beanDefinition) {
      return beanDefinition.getBeanType().getName();
   }
}
