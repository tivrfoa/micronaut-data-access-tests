package io.micronaut.data.model;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.model.naming.NamingStrategy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractPersistentEntity implements PersistentEntity {
   private static final Map<String, NamingStrategy> NAMING_STRATEGIES = new ConcurrentHashMap(3);
   private final AnnotationMetadataProvider annotationMetadataProvider;
   @Nullable
   private final NamingStrategy namingStrategy;

   protected AbstractPersistentEntity(AnnotationMetadataProvider annotationMetadataProvider) {
      this.annotationMetadataProvider = annotationMetadataProvider;
      this.namingStrategy = this.getNamingStrategy(annotationMetadataProvider.getAnnotationMetadata());
   }

   @NonNull
   @Override
   public String getAliasName() {
      return (String)this.getAnnotationMetadata()
         .stringValue(MappedEntity.class, "alias")
         .orElseGet(() -> NamingStrategy.DEFAULT.mappedName(this.getSimpleName()) + "_");
   }

   private NamingStrategy getNamingStrategy(AnnotationMetadata annotationMetadata) {
      return (NamingStrategy)annotationMetadata.stringValue(io.micronaut.data.annotation.NamingStrategy.class)
         .flatMap(className -> getNamingStrategy(className, this.getClass().getClassLoader()))
         .orElse(null);
   }

   @NonNull
   private static Optional<NamingStrategy> getNamingStrategy(String className, ClassLoader classLoader) {
      NamingStrategy namingStrategy = (NamingStrategy)NAMING_STRATEGIES.get(className);
      if (namingStrategy != null) {
         return Optional.of(namingStrategy);
      } else {
         Object o = InstantiationUtils.tryInstantiate(className, classLoader).orElse(null);
         if (o instanceof NamingStrategy) {
            NamingStrategy ns = (NamingStrategy)o;
            NAMING_STRATEGIES.put(className, ns);
            return Optional.of(ns);
         } else {
            return Optional.empty();
         }
      }
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadataProvider.getAnnotationMetadata();
   }

   @NonNull
   @Override
   public NamingStrategy getNamingStrategy() {
      return this.namingStrategy == null ? NamingStrategy.DEFAULT : this.namingStrategy;
   }

   @NonNull
   @Override
   public Optional<NamingStrategy> findNamingStrategy() {
      return Optional.ofNullable(this.namingStrategy);
   }

   @NonNull
   @Override
   public String getPersistedName() {
      return this.getNamingStrategy().mappedName(this);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!this.getClass().isInstance(o)) {
         return false;
      } else {
         AbstractPersistentEntity that = (AbstractPersistentEntity)o;
         return this.getName().equals(that.getName());
      }
   }

   public int hashCode() {
      return this.getName().hashCode();
   }
}
