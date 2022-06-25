package io.micronaut.context;

import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class AbstractInitializableBeanDefinitionReference<T> extends AbstractBeanContextConditional implements BeanDefinitionReference<T> {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractInitializableBeanDefinitionReference.class);
   private final String beanTypeName;
   private final String beanDefinitionTypeName;
   private final AnnotationMetadata annotationMetadata;
   private final boolean isPrimary;
   private final boolean isContextScope;
   private final boolean isConditional;
   private final boolean isContainerType;
   private final boolean isSingleton;
   private final boolean isConfigurationProperties;
   private final boolean hasExposedTypes;
   private final boolean requiresMethodProcessing;
   private Boolean present;
   private Set<Class<?>> exposedTypes;

   public AbstractInitializableBeanDefinitionReference(
      String beanTypeName,
      String beanDefinitionTypeName,
      AnnotationMetadata annotationMetadata,
      boolean isPrimary,
      boolean isContextScope,
      boolean isConditional,
      boolean isContainerType,
      boolean isSingleton,
      boolean isConfigurationProperties,
      boolean hasExposedTypes,
      boolean requiresMethodProcessing
   ) {
      this.beanTypeName = beanTypeName;
      this.beanDefinitionTypeName = beanDefinitionTypeName;
      this.annotationMetadata = annotationMetadata;
      this.isPrimary = isPrimary;
      this.isContextScope = isContextScope;
      this.isConditional = isConditional;
      this.isContainerType = isContainerType;
      this.isSingleton = isSingleton;
      this.isConfigurationProperties = isConfigurationProperties;
      this.hasExposedTypes = hasExposedTypes;
      this.requiresMethodProcessing = requiresMethodProcessing;
   }

   @Override
   public String getName() {
      return this.beanTypeName;
   }

   @Override
   public String getBeanDefinitionName() {
      return this.beanDefinitionTypeName;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public boolean isPrimary() {
      return this.isPrimary;
   }

   @Override
   public boolean isSingleton() {
      return this.isSingleton;
   }

   @Override
   public boolean isConfigurationProperties() {
      return this.isConfigurationProperties;
   }

   @Override
   public boolean isContainerType() {
      return this.isContainerType;
   }

   @Override
   public boolean isContextScope() {
      return this.isContextScope;
   }

   @Override
   public boolean requiresMethodProcessing() {
      return this.requiresMethodProcessing;
   }

   @NonNull
   @Override
   public final Set<Class<?>> getExposedTypes() {
      if (!this.hasExposedTypes) {
         return Collections.EMPTY_SET;
      } else {
         if (this.exposedTypes == null) {
            this.exposedTypes = BeanDefinitionReference.super.getExposedTypes();
         }

         return this.exposedTypes;
      }
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
   public boolean isEnabled(BeanContext context) {
      return this.isPresent() && (!this.isConditional || super.isEnabled(context, null));
   }

   @Override
   public boolean isEnabled(BeanContext context, BeanResolutionContext resolutionContext) {
      return this.isPresent() && (!this.isConditional || super.isEnabled(context, resolutionContext));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractInitializableBeanDefinitionReference that = (AbstractInitializableBeanDefinitionReference)o;
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
