package io.micronaut.data.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.StringJoiner;

public class PersistentPropertyPath {
   private final List<Association> associations;
   private final PersistentProperty property;
   private String path;

   public PersistentPropertyPath(List<Association> associations, @NonNull PersistentProperty property) {
      this(associations, property, null);
   }

   public PersistentPropertyPath(List<Association> associations, @NonNull PersistentProperty property, @Nullable String path) {
      this.associations = associations;
      this.property = property;
      this.path = path;
   }

   public static PersistentPropertyPath of(List<Association> associations, @NonNull PersistentProperty property) {
      return of(associations, property, null);
   }

   public static PersistentPropertyPath of(List<Association> associations, @NonNull PersistentProperty property, @Nullable String path) {
      return (PersistentPropertyPath)(property instanceof Association
         ? new PersistentAssociationPath(associations, (Association)property, path)
         : new PersistentPropertyPath(associations, property, path));
   }

   public Object setPropertyValue(Object bean, Object value) {
      if (!(this.property instanceof RuntimePersistentProperty)) {
         throw new IllegalStateException("Expected runtime property!");
      } else {
         return this.setProperty(this.associations, (RuntimePersistentProperty)this.property, bean, value);
      }
   }

   private Object setProperty(List<Association> associations, RuntimePersistentProperty property, Object bean, Object value) {
      if (associations.isEmpty()) {
         BeanProperty beanProperty = property.getProperty();
         return this.setProperty(beanProperty, bean, value);
      } else {
         Association association = (Association)associations.iterator().next();
         RuntimePersistentProperty<?> p = (RuntimePersistentProperty)association;
         BeanProperty beanProperty = p.getProperty();
         Object prevBean = beanProperty.get(bean);
         Object newBean = this.setProperty(associations.subList(1, associations.size()), property, prevBean, value);
         return prevBean != newBean ? this.setProperty(beanProperty, bean, newBean) : bean;
      }
   }

   private <X, Y> X setProperty(BeanProperty<X, Y> beanProperty, X x, Y y) {
      if (beanProperty.isReadOnly()) {
         return beanProperty.withValue(x, y);
      } else {
         beanProperty.set(x, y);
         return x;
      }
   }

   public Object getPropertyValue(Object bean) {
      if (!(this.property instanceof RuntimePersistentProperty)) {
         throw new IllegalStateException("Expected runtime property!");
      } else {
         Object value = bean;

         for(Association association : this.associations) {
            RuntimePersistentProperty<?> property = (RuntimePersistentProperty)association;
            BeanProperty beanProperty = property.getProperty();
            value = beanProperty.get(value);
            if (value == null) {
               return null;
            }
         }

         RuntimePersistentProperty<?> p = (RuntimePersistentProperty)this.property;
         if (value != null) {
            BeanProperty beanProperty = p.getProperty();
            value = beanProperty.get(value);
         }

         return value;
      }
   }

   @NonNull
   public List<Association> getAssociations() {
      return this.associations;
   }

   @NonNull
   public PersistentProperty getProperty() {
      return this.property;
   }

   @NonNull
   public String getPath() {
      if (this.path == null) {
         if (this.associations.isEmpty()) {
            return this.property.getName();
         }

         StringJoiner joiner = new StringJoiner(".");

         for(Association association : this.associations) {
            joiner.add(association.getName());
         }

         joiner.add(this.property.getName());
         this.path = joiner.toString();
      }

      return this.path;
   }

   public Optional<PersistentEntity> findPropertyOwner() {
      PersistentEntity owner = this.property.getOwner();
      if (!owner.isEmbeddable()) {
         return Optional.of(owner);
      } else {
         ListIterator<Association> listIterator = this.associations.listIterator(this.associations.size());

         while(listIterator.hasPrevious()) {
            Association association = (Association)listIterator.previous();
            if (!association.getOwner().isEmbeddable()) {
               return Optional.of(association.getOwner());
            }
         }

         return Optional.empty();
      }
   }

   public NamingStrategy getNamingStrategy() {
      PersistentEntity owner = this.property.getOwner();
      if (!owner.isEmbeddable()) {
         return owner.getNamingStrategy();
      } else {
         Optional<NamingStrategy> namingStrategy = owner.findNamingStrategy();
         if (namingStrategy.isPresent()) {
            return (NamingStrategy)namingStrategy.get();
         } else {
            ListIterator<Association> listIterator = this.associations.listIterator(this.associations.size());

            while(listIterator.hasPrevious()) {
               Association association = (Association)listIterator.previous();
               if (!association.getOwner().isEmbeddable()) {
                  return association.getOwner().getNamingStrategy();
               }

               Optional<NamingStrategy> embeddedNamingStrategy = owner.findNamingStrategy();
               if (embeddedNamingStrategy.isPresent()) {
                  return (NamingStrategy)embeddedNamingStrategy.get();
               }
            }

            return owner.getNamingStrategy();
         }
      }
   }
}
