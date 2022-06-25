package io.micronaut.web.router.naming;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.naming.conventions.TypeConvention;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.UriMapping;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.web.router.RouteBuilder;
import jakarta.inject.Singleton;

@Singleton
@Primary
public class HyphenatedUriNamingStrategy implements RouteBuilder.UriNamingStrategy {
   @Override
   public String resolveUri(Class type) {
      return '/' + TypeConvention.CONTROLLER.asHyphenatedName(type);
   }

   @NonNull
   @Override
   public String resolveUri(BeanDefinition<?> beanDefinition) {
      String uri = (String)beanDefinition.stringValue(UriMapping.class).orElseGet(() -> (String)beanDefinition.stringValue(Controller.class).orElse("/"));
      return this.normalizeUri(uri);
   }

   @NonNull
   @Override
   public String resolveUri(String property) {
      if (StringUtils.isEmpty(property)) {
         return "/";
      } else {
         return property.charAt(0) != '/' ? '/' + NameUtils.hyphenate(property, true) : property;
      }
   }
}
