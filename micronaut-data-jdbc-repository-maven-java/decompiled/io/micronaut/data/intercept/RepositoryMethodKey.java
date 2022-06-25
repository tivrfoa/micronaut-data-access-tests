package io.micronaut.data.intercept;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.ExecutableMethod;
import java.util.Objects;

@Internal
public final class RepositoryMethodKey {
   private final Object repository;
   private final ExecutableMethod method;
   private final int hashCode;

   public RepositoryMethodKey(Object repository, ExecutableMethod method) {
      this.repository = repository;
      this.method = method;
      this.hashCode = Objects.hash(new Object[]{repository, method});
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RepositoryMethodKey that = (RepositoryMethodKey)o;
         return this.repository.equals(that.repository) && this.method.equals(that.method);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }
}
