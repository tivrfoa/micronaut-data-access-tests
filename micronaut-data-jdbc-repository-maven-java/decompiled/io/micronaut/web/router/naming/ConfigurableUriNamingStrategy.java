package io.micronaut.web.router.naming;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.conventions.PropertyConvention;
import io.micronaut.inject.BeanDefinition;
import jakarta.inject.Singleton;

@Primary
@Singleton
@Replaces(HyphenatedUriNamingStrategy.class)
@Requires(
   property = "micronaut.server.context-path"
)
public class ConfigurableUriNamingStrategy extends HyphenatedUriNamingStrategy {
   private final String contextPath;

   public ConfigurableUriNamingStrategy(@Value("${micronaut.server.context-path}") String contextPath) {
      this.contextPath = this.normalizeContextPath(contextPath);
   }

   @Override
   public String resolveUri(Class type) {
      return this.contextPath + super.resolveUri(type);
   }

   @NonNull
   @Override
   public String resolveUri(BeanDefinition<?> beanDefinition) {
      return this.contextPath + super.resolveUri(beanDefinition);
   }

   @NonNull
   @Override
   public String resolveUri(String property) {
      return this.contextPath + super.resolveUri(property);
   }

   @NonNull
   @Override
   public String resolveUri(Class type, PropertyConvention id) {
      return this.contextPath + super.resolveUri(type, id);
   }

   private String normalizeContextPath(String contextPath) {
      if (contextPath.charAt(0) != '/') {
         contextPath = '/' + contextPath;
      }

      if (contextPath.charAt(contextPath.length() - 1) == '/') {
         contextPath = contextPath.substring(0, contextPath.length() - 1);
      }

      return contextPath;
   }
}
