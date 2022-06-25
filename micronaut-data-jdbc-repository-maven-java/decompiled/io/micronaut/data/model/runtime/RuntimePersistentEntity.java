package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.Version;
import io.micronaut.data.exceptions.MappingException;
import io.micronaut.data.model.AbstractPersistentEntity;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RuntimePersistentEntity<T> extends AbstractPersistentEntity implements PersistentEntity {
   private final BeanIntrospection<T> introspection;
   private final RuntimePersistentProperty<T>[] identity;
   private final RuntimePersistentProperty<T>[] allPersistentProperties;
   private final RuntimePersistentProperty<T>[] persistentProperties;
   private final RuntimePersistentProperty<T>[] constructorArguments;
   private final String aliasName;
   private final RuntimePersistentProperty<T> version;
   private Boolean hasAutoPopulatedProperties;
   private List<String> allPersistentPropertiesNames;
   private List<RuntimePersistentProperty<T>> persistentPropertiesValues;

   public RuntimePersistentEntity(@NonNull Class<T> type) {
      this(BeanIntrospection.getIntrospection(type));
   }

   public RuntimePersistentEntity(@NonNull BeanIntrospection<T> introspection) {
      super(introspection);
      ArgumentUtils.requireNonNull("introspection", introspection);
      this.introspection = introspection;
      Argument<?>[] constructorArguments = introspection.getConstructorArguments();
      Set<String> constructorArgumentNames = (Set)Arrays.stream(constructorArguments).map(Argument::getName).collect(Collectors.toSet());
      RuntimePersistentProperty<T> version = null;
      List<RuntimePersistentProperty<T>> ids = new LinkedList();
      Collection<BeanProperty<T, Object>> beanProperties = introspection.getBeanProperties();
      this.allPersistentProperties = new RuntimePersistentProperty[beanProperties.size()];
      this.persistentProperties = new RuntimePersistentProperty[beanProperties.size()];

      for(BeanProperty<T, Object> bp : beanProperties) {
         if (!bp.hasStereotype(Transient.class)) {
            int propertyIndex = introspection.propertyIndexOf(bp.getName());
            if (bp.hasStereotype(Id.class)) {
               RuntimePersistentProperty<T> id;
               if (this.isEmbedded(bp)) {
                  id = new RuntimeEmbedded<>(this, bp, constructorArgumentNames.contains(bp.getName()));
               } else {
                  id = new RuntimePersistentProperty<>(this, bp, constructorArgumentNames.contains(bp.getName()));
               }

               ids.add(id);
               this.allPersistentProperties[propertyIndex] = id;
            } else if (bp.hasStereotype(Version.class)) {
               version = new RuntimePersistentProperty<>(this, bp, constructorArgumentNames.contains(bp.getName()));
               this.allPersistentProperties[propertyIndex] = version;
            } else {
               RuntimePersistentProperty<T> prop;
               if (bp.hasAnnotation(Relation.class)) {
                  if (this.isEmbedded(bp)) {
                     prop = new RuntimeEmbedded<>(this, bp, constructorArgumentNames.contains(bp.getName()));
                  } else {
                     prop = new RuntimeAssociation<>(this, bp, constructorArgumentNames.contains(bp.getName()));
                  }
               } else {
                  prop = new RuntimePersistentProperty<>(this, bp, constructorArgumentNames.contains(bp.getName()));
               }

               this.allPersistentProperties[propertyIndex] = prop;
               this.persistentProperties[propertyIndex] = prop;
            }
         }
      }

      this.identity = (RuntimePersistentProperty[])ids.toArray(new RuntimePersistentProperty[0]);
      this.version = version;
      this.constructorArguments = new RuntimePersistentProperty[constructorArguments.length];

      for(int i = 0; i < constructorArguments.length; ++i) {
         Argument<?> constructorArgument = constructorArguments[i];
         String argumentName = constructorArgument.getName();
         RuntimePersistentProperty<T> prop = this.getPropertyByName(argumentName);
         if (prop == null) {
            throw new MappingException("Constructor argument [" + argumentName + "] for type [" + this.getName() + "] must have an associated getter");
         }

         this.constructorArguments[i] = prop;
      }

      this.aliasName = super.getAliasName();
   }

   @NonNull
   protected AttributeConverter<Object, Object> resolveConverter(@NonNull Class<?> converterClass) {
      throw new MappingException("Converters not supported");
   }

   public boolean hasPrePersistEventListeners() {
      return false;
   }

   public boolean hasPreRemoveEventListeners() {
      return false;
   }

   public boolean hasPreUpdateEventListeners() {
      return false;
   }

   public boolean hasPostPersistEventListeners() {
      return false;
   }

   public boolean hasPostUpdateEventListeners() {
      return false;
   }

   public boolean hasPostRemoveEventListeners() {
      return false;
   }

   public boolean hasPostLoadEventListeners() {
      return false;
   }

   public String toString() {
      return this.getName();
   }

   @NonNull
   @Override
   public String getAliasName() {
      return this.aliasName;
   }

   public BeanIntrospection<T> getIntrospection() {
      return this.introspection;
   }

   @NonNull
   @Override
   public String getName() {
      return this.introspection.getBeanType().getName();
   }

   @Override
   public boolean hasCompositeIdentity() {
      return this.identity.length > 1;
   }

   @Override
   public boolean hasIdentity() {
      return this.identity.length == 1;
   }

   @Nullable
   public RuntimePersistentProperty<T>[] getCompositeIdentity() {
      return this.identity.length > 1 ? this.identity : null;
   }

   @Nullable
   public RuntimePersistentProperty<T> getIdentity() {
      return this.identity.length == 1 ? this.identity[0] : null;
   }

   @Nullable
   public RuntimePersistentProperty<T> getVersion() {
      return this.version;
   }

   @NonNull
   @Override
   public Collection<RuntimePersistentProperty<T>> getPersistentProperties() {
      if (this.persistentPropertiesValues == null) {
         this.persistentPropertiesValues = Collections.unmodifiableList(
            (List)Arrays.stream(this.persistentProperties).filter(Objects::nonNull).collect(Collectors.toList())
         );
      }

      return this.persistentPropertiesValues;
   }

   @NonNull
   @Override
   public Collection<RuntimeAssociation<T>> getAssociations() {
      return super.getAssociations();
   }

   @Nullable
   public RuntimePersistentProperty<T> getPropertyByName(String name) {
      int propertyIndex = this.introspection.propertyIndexOf(name);
      return propertyIndex == -1 ? null : this.allPersistentProperties[propertyIndex];
   }

   @Nullable
   public RuntimePersistentProperty<T> getIdentityByName(String name) {
      return (RuntimePersistentProperty<T>)super.getIdentityByName(name);
   }

   @NonNull
   public List<String> getPersistentPropertyNames() {
      if (this.allPersistentPropertiesNames == null) {
         this.allPersistentPropertiesNames = Collections.unmodifiableList(
            (List)Arrays.stream(this.allPersistentProperties).filter(Objects::nonNull).map(RuntimePersistentProperty::getName).collect(Collectors.toList())
         );
      }

      return this.allPersistentPropertiesNames;
   }

   @Override
   public boolean isOwningEntity(PersistentEntity owner) {
      return true;
   }

   @Nullable
   @Override
   public PersistentEntity getParentEntity() {
      return null;
   }

   private boolean isEmbedded(BeanProperty bp) {
      return bp.enumValue(Relation.class, Relation.Kind.class).orElse(null) == Relation.Kind.EMBEDDED;
   }

   protected RuntimePersistentEntity<T> getEntity(Class<T> type) {
      return PersistentEntity.of(type);
   }

   public RuntimePersistentProperty<T>[] getConstructorArguments() {
      return this.constructorArguments;
   }

   public boolean hasAutoPopulatedProperties() {
      if (this.hasAutoPopulatedProperties == null) {
         this.hasAutoPopulatedProperties = Arrays.stream(this.allPersistentProperties).filter(Objects::nonNull).anyMatch(PersistentProperty::isAutoPopulated);
      }

      return this.hasAutoPopulatedProperties;
   }
}
