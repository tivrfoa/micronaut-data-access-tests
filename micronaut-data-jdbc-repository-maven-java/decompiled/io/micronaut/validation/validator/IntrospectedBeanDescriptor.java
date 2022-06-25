package io.micronaut.validation.validator;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.util.ArgumentUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ContainerElementTypeDescriptor;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.metadata.Scope;

@Internal
class IntrospectedBeanDescriptor implements BeanDescriptor, ElementDescriptor.ConstraintFinder {
   private final BeanIntrospection<?> beanIntrospection;

   IntrospectedBeanDescriptor(BeanIntrospection<?> beanIntrospection) {
      ArgumentUtils.requireNonNull("beanIntrospection", beanIntrospection);
      this.beanIntrospection = beanIntrospection;
   }

   @Override
   public boolean isBeanConstrained() {
      return this.hasConstraints();
   }

   @Override
   public PropertyDescriptor getConstraintsForProperty(String propertyName) {
      return (PropertyDescriptor)this.beanIntrospection
         .getProperty(propertyName)
         .map(x$0 -> new IntrospectedBeanDescriptor.IntrospectedPropertyDescriptor(x$0))
         .orElse(null);
   }

   @Override
   public Set<PropertyDescriptor> getConstrainedProperties() {
      return (Set<PropertyDescriptor>)this.beanIntrospection
         .getIndexedProperties(Constraint.class)
         .stream()
         .map(x$0 -> new IntrospectedBeanDescriptor.IntrospectedPropertyDescriptor(x$0))
         .collect(Collectors.toSet());
   }

   @Override
   public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
      return null;
   }

   @Override
   public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType, MethodType... methodTypes) {
      return Collections.emptySet();
   }

   @Override
   public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
      return null;
   }

   @Override
   public Set<ConstructorDescriptor> getConstrainedConstructors() {
      return Collections.emptySet();
   }

   @Override
   public boolean hasConstraints() {
      return this.beanIntrospection.getIndexedProperty(Constraint.class).isPresent();
   }

   @Override
   public Class<?> getElementClass() {
      return this.beanIntrospection.getBeanType();
   }

   @Override
   public ElementDescriptor.ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
      return this;
   }

   @Override
   public ElementDescriptor.ConstraintFinder lookingAt(Scope scope) {
      return this;
   }

   @Override
   public ElementDescriptor.ConstraintFinder declaredOn(ElementType... types) {
      return this;
   }

   @Override
   public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
      return Collections.emptySet();
   }

   @Override
   public ElementDescriptor.ConstraintFinder findConstraints() {
      return this;
   }

   private final class IntrospectedPropertyDescriptor implements PropertyDescriptor, ElementDescriptor.ConstraintFinder {
      private final BeanProperty<?, ?> beanProperty;

      IntrospectedPropertyDescriptor(BeanProperty<?, ?> beanProperty) {
         this.beanProperty = beanProperty;
      }

      @Override
      public String getPropertyName() {
         return this.beanProperty.getName();
      }

      @Override
      public boolean isCascaded() {
         return this.beanProperty.hasAnnotation(Valid.class);
      }

      @Override
      public Set<GroupConversionDescriptor> getGroupConversions() {
         return Collections.emptySet();
      }

      @Override
      public Set<ContainerElementTypeDescriptor> getConstrainedContainerElementTypes() {
         return Collections.emptySet();
      }

      @Override
      public boolean hasConstraints() {
         return this.beanProperty.hasStereotype(Constraint.class);
      }

      @Override
      public Class<?> getElementClass() {
         return this.beanProperty.getType();
      }

      @Override
      public ElementDescriptor.ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
         return this;
      }

      @Override
      public ElementDescriptor.ConstraintFinder lookingAt(Scope scope) {
         return this;
      }

      @Override
      public ElementDescriptor.ConstraintFinder declaredOn(ElementType... types) {
         return this;
      }

      @Override
      public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
         return (Set<ConstraintDescriptor<?>>)this.beanProperty.getAnnotationTypesByStereotype(Constraint.class).stream().map(type -> {
            AnnotationValue<? extends Annotation> annotation = this.beanProperty.getAnnotation(type);
            return new DefaultConstraintDescriptor<>(this.beanProperty.getAnnotationMetadata(), type, annotation);
         }).collect(Collectors.toSet());
      }

      @Override
      public ElementDescriptor.ConstraintFinder findConstraints() {
         return this;
      }
   }
}
