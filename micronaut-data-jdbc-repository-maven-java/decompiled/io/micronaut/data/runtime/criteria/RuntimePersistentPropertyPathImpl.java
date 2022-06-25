package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentPropertyPath;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import jakarta.persistence.criteria.Path;
import java.util.List;

final class RuntimePersistentPropertyPathImpl<I, T> extends AbstractPersistentPropertyPath<T> {
   private final Path<?> parentPath;
   private final RuntimePersistentProperty<I> runtimePersistentProperty;

   public RuntimePersistentPropertyPathImpl(Path<?> parentPath, List<Association> path, RuntimePersistentProperty<I> persistentProperty) {
      super(persistentProperty, path);
      this.parentPath = parentPath;
      this.runtimePersistentProperty = persistentProperty;
   }

   @Override
   public Path<?> getParentPath() {
      return this.parentPath;
   }

   @Override
   public Class<? extends T> getJavaType() {
      return this.runtimePersistentProperty.getType();
   }

   public RuntimePersistentProperty<I> getRuntimePersistentProperty() {
      return this.runtimePersistentProperty;
   }

   @Override
   public String toString() {
      return "RuntimePersistentPropertyPath{runtimePersistentProperty=" + this.runtimePersistentProperty + '}';
   }
}
