package io.micronaut.data.runtime.query.internal;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.inject.ExecutableMethod;

@Internal
public final class DefaultPagedQuery<E> implements PagedQuery<E> {
   private final ExecutableMethod<?, ?> method;
   @NonNull
   private final Class<E> rootEntity;
   private final Pageable pageable;

   public DefaultPagedQuery(ExecutableMethod<?, ?> method, @NonNull Class<E> rootEntity, Pageable pageable) {
      this.method = method;
      this.rootEntity = rootEntity;
      this.pageable = pageable;
   }

   @NonNull
   @Override
   public Class<E> getRootEntity() {
      return this.rootEntity;
   }

   @NonNull
   @Override
   public Pageable getPageable() {
      return this.pageable;
   }

   @NonNull
   @Override
   public String getName() {
      return this.method.getMethodName();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.method.getAnnotationMetadata();
   }
}
