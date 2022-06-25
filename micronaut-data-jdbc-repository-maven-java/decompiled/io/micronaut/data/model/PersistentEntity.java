package io.micronaut.data.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Embeddable;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PersistentEntity extends PersistentElement {
   @NonNull
   @Override
   String getName();

   @NonNull
   String getAliasName();

   default boolean hasCompositeIdentity() {
      return this.getCompositeIdentity() != null;
   }

   default boolean hasIdentity() {
      return this.getIdentity() != null;
   }

   @Nullable
   PersistentProperty[] getCompositeIdentity();

   @Nullable
   PersistentProperty getIdentity();

   @Nullable
   PersistentProperty getVersion();

   default boolean isVersioned() {
      return this.getVersion() != null;
   }

   @NonNull
   Collection<? extends PersistentProperty> getPersistentProperties();

   @NonNull
   default Collection<? extends Association> getAssociations() {
      return (Collection<? extends Association>)this.getPersistentProperties()
         .stream()
         .filter(bp -> bp instanceof Association)
         .map(bp -> (Association)bp)
         .collect(Collectors.toList());
   }

   @NonNull
   default Collection<Embedded> getEmbedded() {
      return (Collection<Embedded>)this.getPersistentProperties()
         .stream()
         .filter(p -> p instanceof Embedded)
         .map(p -> (Embedded)p)
         .collect(Collectors.toList());
   }

   @Nullable
   PersistentProperty getPropertyByName(String name);

   @Nullable
   default PersistentProperty getIdentityByName(String name) {
      PersistentProperty identity = this.getIdentity();
      if (identity != null && identity.getName().equals(name)) {
         return identity;
      } else {
         PersistentProperty[] compositeIdentities = this.getCompositeIdentity();
         if (compositeIdentities != null) {
            for(PersistentProperty compositeIdentity : compositeIdentities) {
               if (compositeIdentity.getName().equals(name)) {
                  return compositeIdentity;
               }
            }
         }

         return null;
      }
   }

   @NonNull
   Collection<String> getPersistentPropertyNames();

   default boolean isEmbeddable() {
      return this.getAnnotationMetadata().hasAnnotation(Embeddable.class);
   }

   @NonNull
   default String getSimpleName() {
      return NameUtils.getSimpleName(this.getName());
   }

   @NonNull
   default String getDecapitalizedName() {
      return NameUtils.decapitalize(this.getSimpleName());
   }

   boolean isOwningEntity(PersistentEntity owner);

   @Nullable
   PersistentEntity getParentEntity();

   default Optional<String> getPath(String camelCasePath) {
      List<String> path = (List)Arrays.stream(AssociationUtils.CAMEL_CASE_SPLIT_PATTERN.split(camelCasePath))
         .map(NameUtils::decapitalize)
         .collect(Collectors.toList());
      if (!CollectionUtils.isNotEmpty(path)) {
         return Optional.empty();
      } else {
         Iterator<String> i = path.iterator();
         StringBuilder b = new StringBuilder();
         PersistentEntity currentEntity = this;
         String name = null;

         while(i.hasNext()) {
            name = name == null ? (String)i.next() : name + NameUtils.capitalize((String)i.next());
            PersistentProperty sp = currentEntity.getPropertyByName(name);
            if (sp == null) {
               PersistentProperty identity = currentEntity.getIdentity();
               if (identity != null) {
                  if (identity.getName().equals(name)) {
                     sp = identity;
                  } else if (identity instanceof Association) {
                     PersistentEntity idEntity = ((Association)identity).getAssociatedEntity();
                     sp = idEntity.getPropertyByName(name);
                  }
               }
            }

            if (sp != null) {
               if (sp instanceof Association) {
                  b.append(name);
                  if (i.hasNext()) {
                     b.append('.');
                  }

                  currentEntity = ((Association)sp).getAssociatedEntity();
                  name = null;
               } else if (!i.hasNext()) {
                  b.append(name);
               }
            }
         }

         return b.length() != 0 && b.charAt(b.length() - 1) != '.' ? Optional.of(b.toString()) : Optional.empty();
      }
   }

   @NonNull
   default PersistentEntity getRootEntity() {
      return this;
   }

   default boolean isRoot() {
      return this.getRootEntity() == this;
   }

   default Optional<PersistentProperty> getPropertyByPath(String path) {
      if (path.indexOf(46) == -1) {
         PersistentProperty pp = this.getPropertyByName(path);
         if (pp == null) {
            PersistentProperty identity = this.getIdentity();
            if (identity != null) {
               if (identity.getName().equals(path)) {
                  pp = identity;
               } else if (identity instanceof Embedded) {
                  PersistentEntity idEntity = ((Embedded)identity).getAssociatedEntity();
                  pp = idEntity.getPropertyByName(path);
               }
            }
         }

         return Optional.ofNullable(pp);
      } else {
         String[] tokens = path.split("\\.");
         PersistentEntity startingEntity = this;
         PersistentProperty prop = null;

         for(String token : tokens) {
            prop = startingEntity.getPropertyByName(token);
            if (prop == null) {
               PersistentProperty identity = startingEntity.getIdentity();
               if (identity == null || !identity.getName().equals(token)) {
                  return Optional.empty();
               }

               prop = identity;
            }

            if (prop instanceof Association) {
               startingEntity = ((Association)prop).getAssociatedEntity();
            }
         }

         return Optional.ofNullable(prop);
      }
   }

   @Nullable
   default PersistentPropertyPath getPropertyPath(@NonNull String path) {
      return path.indexOf(46) == -1
         ? this.getPropertyPath(new String[]{path})
         : this.getPropertyPath((String[])StringUtils.splitOmitEmptyStringsList(path, '.').toArray(new String[0]));
   }

   @Nullable
   default PersistentPropertyPath getPropertyPath(@NonNull String[] propertyPath) {
      if (propertyPath.length == 0) {
         return null;
      } else if (propertyPath.length == 1) {
         String propertyName = propertyPath[0];
         PersistentProperty pp = this.getPropertyByName(propertyName);
         if (pp == null) {
            PersistentProperty identity = this.getIdentity();
            if (identity != null) {
               if (identity.getName().equals(propertyName)) {
                  pp = identity;
               } else if (identity instanceof Embedded) {
                  PersistentEntity idEntity = ((Embedded)identity).getAssociatedEntity();
                  pp = idEntity.getPropertyByName(propertyName);
                  if (pp != null) {
                     return PersistentPropertyPath.of(Collections.singletonList((Embedded)identity), pp, identity.getName() + "." + pp.getName());
                  }
               }
            }

            PersistentProperty version = this.getVersion();
            if (version != null && version.getName().equals(propertyName)) {
               pp = version;
            }
         }

         return pp == null ? null : PersistentPropertyPath.of(Collections.emptyList(), pp, propertyName);
      } else {
         List<Association> associations = new ArrayList(propertyPath.length - 1);
         PersistentEntity startingEntity = this;

         for(int i = 0; i < propertyPath.length - 1; ++i) {
            String propertyName = propertyPath[i];
            PersistentProperty prop = startingEntity.getPropertyByName(propertyName);
            if (!(prop instanceof Association)) {
               if (prop == null) {
                  return null;
               }

               throw new IllegalArgumentException(
                  "Invalid association path. Property ["
                     + propertyName
                     + "] of ["
                     + startingEntity
                     + "] is not an association in ["
                     + String.join(".", propertyPath)
                     + "]"
               );
            }

            Association association = (Association)prop;
            startingEntity = association.getAssociatedEntity();
            associations.add(association);
         }

         PersistentProperty prop = startingEntity.getPropertyByName(propertyPath[propertyPath.length - 1]);
         return prop == null ? null : PersistentPropertyPath.of(associations, prop);
      }
   }

   @NonNull
   NamingStrategy getNamingStrategy();

   @NonNull
   Optional<NamingStrategy> findNamingStrategy();

   @NonNull
   static <T> RuntimePersistentEntity<T> of(@NonNull Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      return new RuntimePersistentEntity<>(type);
   }

   @NonNull
   static <T> RuntimePersistentEntity<T> of(@NonNull BeanIntrospection<T> introspection) {
      ArgumentUtils.requireNonNull("introspection", introspection);
      return new RuntimePersistentEntity<>(introspection);
   }
}
