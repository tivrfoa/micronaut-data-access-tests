package io.micronaut.context;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class AbstractBeanDefinitionReference extends AbstractBeanContextConditional implements BeanDefinitionReference {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractBeanDefinitionReference.class);
   private final String beanTypeName;
   private final String beanDefinitionTypeName;
   private Boolean present;
   private Set<Class<?>> exposedTypes;

   public AbstractBeanDefinitionReference(String beanTypeName, String beanDefinitionTypeName) {
      this.beanTypeName = beanTypeName;
      this.beanDefinitionTypeName = beanDefinitionTypeName;
   }

   @Override
   public boolean isPrimary() {
      return this.getAnnotationMetadata().hasAnnotation(Primary.class);
   }

   @NonNull
   @Override
   public final Set<Class<?>> getExposedTypes() {
      if (this.exposedTypes == null) {
         this.exposedTypes = BeanDefinitionReference.super.getExposedTypes();
      }

      return this.exposedTypes;
   }

   @Override
   public String getName() {
      return this.beanTypeName;
   }

   @Override
   public BeanDefinition load(BeanContext context) {
      BeanDefinition definition = this.load();
      if (context instanceof ApplicationContext && definition instanceof EnvironmentConfigurable) {
         ((EnvironmentConfigurable)definition).configure(((ApplicationContext)context).getEnvironment());
      }

      return definition;
   }

   @Override
   public boolean isContextScope() {
      return this.getAnnotationMetadata().hasDeclaredStereotype(Context.class);
   }

   @Override
   public String getBeanDefinitionName() {
      return this.beanDefinitionTypeName;
   }

   @Override
   public boolean isPresent() {
      if (this.present == null) {
         try {
            this.getBeanDefinitionType();
            this.getBeanType();
            this.present = true;
         } catch (Throwable var2) {
            if (!(var2 instanceof TypeNotPresentException) && !(var2 instanceof ClassNotFoundException) && !(var2 instanceof NoClassDefFoundError)) {
               throw new BeanContextException("Unexpected error loading bean definition [" + this.beanDefinitionTypeName + "]: " + var2.getMessage(), var2);
            }

            if (LOG.isTraceEnabled()) {
               LOG.trace("Bean definition for type [" + this.beanTypeName + "] not loaded since it is not on the classpath", var2);
            }

            this.present = false;
         }
      }

      return this.present;
   }

   @Override
   public boolean isEnabled(BeanContext beanContext) {
      return this.isPresent() && super.isEnabled(beanContext);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractBeanDefinitionReference that = (AbstractBeanDefinitionReference)o;
         return this.beanDefinitionTypeName.equals(that.beanDefinitionTypeName);
      } else {
         return false;
      }
   }

   public String toString() {
      return this.beanDefinitionTypeName;
   }

   public int hashCode() {
      return this.beanDefinitionTypeName.hashCode();
   }

   protected abstract Class<? extends BeanDefinition<?>> getBeanDefinitionType();
}
