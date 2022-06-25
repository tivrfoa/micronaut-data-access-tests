package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanDefinitionReference;

@Internal
public abstract class AbstractBeanConfiguration extends AbstractBeanContextConditional implements BeanConfiguration {
   private final String packageName;

   protected AbstractBeanConfiguration(String thePackage) {
      this.packageName = thePackage;
   }

   @Override
   public Package getPackage() {
      return this.getClass().getPackage();
   }

   @Override
   public String getName() {
      return this.packageName;
   }

   @Override
   public String getVersion() {
      return this.getPackage().getImplementationVersion();
   }

   @Override
   public boolean isWithin(BeanDefinitionReference beanDefinitionReference) {
      String beanTypeName = beanDefinitionReference.getBeanDefinitionName();
      return this.isWithin(beanTypeName);
   }

   public String toString() {
      return "Configuration: " + this.getName();
   }

   @Override
   public boolean isWithin(String className) {
      int i = className.lastIndexOf(46);
      String pkgName = i > -1 ? className.substring(0, i) : className;
      return pkgName.equals(this.packageName) || pkgName.startsWith(this.packageName + '.');
   }
}
