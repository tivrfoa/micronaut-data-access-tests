package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

@Internal
public interface BeanDefinitionReference<T> extends BeanType<T> {
   String getBeanDefinitionName();

   BeanDefinition<T> load();

   default BeanDefinition<T> load(BeanContext context) {
      return this.load();
   }

   default boolean isContextScope() {
      return false;
   }

   boolean isPresent();

   default boolean isSingleton() {
      AnnotationMetadata am = this.getAnnotationMetadata();
      if (am.hasDeclaredStereotype("javax.inject.Singleton")) {
         return true;
      } else {
         return !am.hasDeclaredStereotype("javax.inject.Scope") && am.hasDeclaredStereotype(DefaultScope.class)
            ? am.stringValue(DefaultScope.class).map(t -> t.equals(Singleton.class.getName()) || t.equals("javax.inject.Singleton")).orElse(false)
            : false;
      }
   }

   default boolean isConfigurationProperties() {
      return this.getAnnotationMetadata().hasDeclaredStereotype(ConfigurationReader.class);
   }
}
