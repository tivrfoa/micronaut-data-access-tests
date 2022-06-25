package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.runtime.event.DefaultEntityEventContext;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Internal
public abstract class AbstractSyncEntityOperations<Ctx extends OperationContext, T, Exc extends Exception> extends SyncEntityOperations<T, Exc> {
   protected SyncCascadeOperations<Ctx> cascadeOperations;
   protected final Ctx ctx;
   protected final boolean insert;
   protected final boolean hasGeneratedId;
   protected T entity;

   protected AbstractSyncEntityOperations(
      Ctx ctx,
      SyncCascadeOperations<Ctx> cascadeOperations,
      EntityEventListener<Object> entityEventListener,
      RuntimePersistentEntity<T> persistentEntity,
      ConversionService<?> conversionService,
      T entity,
      boolean insert
   ) {
      super(entityEventListener, persistentEntity, conversionService);
      this.cascadeOperations = cascadeOperations;
      this.ctx = ctx;
      this.insert = insert;
      this.hasGeneratedId = insert && persistentEntity.getIdentity() != null && persistentEntity.getIdentity().isGenerated();
      Objects.requireNonNull(entity, "Passed entity cannot be null");
      this.entity = entity;
   }

   @Override
   protected void cascadePre(Relation.Cascade cascadeType) {
      this.entity = this.cascadeOperations.cascadeEntity(this.ctx, this.entity, this.persistentEntity, false, cascadeType);
   }

   @Override
   protected void cascadePost(Relation.Cascade cascadeType) {
      this.entity = this.cascadeOperations.cascadeEntity(this.ctx, this.entity, this.persistentEntity, true, cascadeType);
   }

   @Override
   protected void collectAutoPopulatedPreviousValues() {
   }

   @Override
   protected boolean triggerPre(Function<EntityEventContext<Object>, Boolean> fn) {
      DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(this.persistentEntity, this.entity);
      boolean vetoed = !fn.apply(event);
      if (vetoed) {
         return true;
      } else {
         T newEntity = event.getEntity();
         if (this.entity != newEntity) {
            this.entity = newEntity;
         }

         return false;
      }
   }

   @Override
   protected void triggerPost(Consumer<EntityEventContext<Object>> fn) {
      DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(this.persistentEntity, this.entity);
      fn.accept(event);
   }

   @Override
   public void veto(Predicate<T> predicate) {
      throw new IllegalStateException("Not supported");
   }

   @Override
   public T getEntity() {
      return this.entity;
   }
}
