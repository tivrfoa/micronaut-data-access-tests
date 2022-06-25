package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.runtime.event.DefaultEntityEventContext;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Internal
public abstract class AbstractReactiveEntitiesOperations<Ctx extends OperationContext, T, Exc extends Exception> extends ReactiveEntitiesOperations<T, Exc> {
   protected final Ctx ctx;
   protected final ReactiveCascadeOperations<Ctx> cascadeOperations;
   protected final boolean insert;
   protected final boolean hasGeneratedId;
   protected Flux<AbstractReactiveEntitiesOperations<Ctx, T, Exc>.Data> entities;
   protected Mono<Long> rowsUpdated;

   protected AbstractReactiveEntitiesOperations(
      Ctx ctx,
      ReactiveCascadeOperations<Ctx> cascadeOperations,
      ConversionService<?> conversionService,
      EntityEventListener<Object> entityEventListener,
      RuntimePersistentEntity<T> persistentEntity,
      Iterable<T> entities,
      boolean insert
   ) {
      super(entityEventListener, persistentEntity, conversionService);
      this.ctx = ctx;
      this.cascadeOperations = cascadeOperations;
      this.insert = insert;
      this.hasGeneratedId = insert && persistentEntity.getIdentity() != null && persistentEntity.getIdentity().isGenerated();
      Objects.requireNonNull(entities, "Entities cannot be null");
      if (!entities.iterator().hasNext()) {
         throw new IllegalStateException("Entities cannot be empty");
      } else {
         this.entities = Flux.fromIterable(entities).map(entity -> {
            AbstractReactiveEntitiesOperations<Ctx, T, Exc>.Data data = new AbstractReactiveEntitiesOperations.Data();
            data.entity = (T)entity;
            return data;
         });
      }
   }

   @Override
   protected void cascadePre(Relation.Cascade cascadeType) {
      this.doCascade(false, cascadeType);
   }

   @Override
   protected void cascadePost(Relation.Cascade cascadeType) {
      this.doCascade(true, cascadeType);
   }

   private void doCascade(boolean isPost, Relation.Cascade cascadeType) {
      this.entities = this.entities.concatMap(d -> {
         if (d.vetoed) {
            return Mono.just(d);
         } else {
            Mono<T> entity = this.cascadeOperations.cascadeEntity(this.ctx, d.entity, this.persistentEntity, isPost, cascadeType);
            return entity.map(e -> {
               d.entity = (T)e;
               return d;
            });
         }
      });
   }

   @Override
   public void veto(Predicate<T> predicate) {
      this.entities = this.entities.map(d -> {
         if (d.vetoed) {
            return d;
         } else {
            d.vetoed = predicate.test(d.entity);
            return d;
         }
      });
   }

   @Override
   protected boolean triggerPre(Function<EntityEventContext<Object>, Boolean> fn) {
      this.entities = this.entities.map(d -> {
         if (d.vetoed) {
            return d;
         } else {
            DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(this.persistentEntity, d.entity);
            d.vetoed = !fn.apply(event);
            d.entity = event.getEntity();
            return d;
         }
      });
      return false;
   }

   @Override
   protected void triggerPost(Consumer<EntityEventContext<Object>> fn) {
      this.entities = this.entities.map(d -> {
         if (d.vetoed) {
            return d;
         } else {
            DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(this.persistentEntity, d.entity);
            fn.accept(event);
            d.entity = event.getEntity();
            return d;
         }
      });
   }

   protected boolean notVetoed(AbstractReactiveEntitiesOperations<Ctx, T, Exc>.Data data) {
      return !data.vetoed;
   }

   @Override
   public Flux<T> getEntities() {
      return this.entities.map(d -> d.entity);
   }

   public Mono<Number> getRowsUpdated() {
      return this.rowsUpdated.flatMap(rows -> this.entities.then(Mono.just((T)rows)));
   }

   protected final class Data {
      public T entity;
      public Object filter;
      public Map<QueryParameterBinding, Object> previousValues;
      public boolean vetoed = false;
   }
}
