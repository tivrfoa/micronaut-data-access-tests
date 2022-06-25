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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.publisher.Mono;

@Internal
public abstract class AbstractReactiveEntityOperations<Ctx extends OperationContext, T, Exc extends Exception> extends ReactiveEntityOperations<T, Exc> {
   protected final Ctx ctx;
   protected final ReactiveCascadeOperations<Ctx> cascadeOperations;
   protected final boolean insert;
   protected final boolean hasGeneratedId;
   protected Mono<AbstractReactiveEntityOperations<Ctx, T, Exc>.Data> data;

   protected AbstractReactiveEntityOperations(
      Ctx ctx,
      ReactiveCascadeOperations<Ctx> cascadeOperations,
      ConversionService<?> conversionService,
      EntityEventListener<Object> entityEventListener,
      RuntimePersistentEntity<T> persistentEntity,
      T entity,
      boolean insert
   ) {
      super(entityEventListener, persistentEntity, conversionService);
      this.cascadeOperations = cascadeOperations;
      this.ctx = ctx;
      this.insert = insert;
      this.hasGeneratedId = insert && persistentEntity.getIdentity() != null && persistentEntity.getIdentity().isGenerated();
      AbstractReactiveEntityOperations<Ctx, T, Exc>.Data data = new AbstractReactiveEntityOperations.Data();
      data.entity = entity;
      this.data = Mono.just(data);
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
      this.data = this.data.flatMap(d -> {
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
   protected boolean triggerPre(Function<EntityEventContext<Object>, Boolean> fn) {
      this.data = this.data.map(d -> {
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
      this.data = this.data.map(d -> {
         if (d.vetoed) {
            return d;
         } else {
            DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(this.persistentEntity, d.entity);
            fn.accept(event);
            return d;
         }
      });
   }

   @Override
   public void veto(Predicate<T> predicate) {
      this.data = this.data.map(d -> {
         if (d.vetoed) {
            return d;
         } else {
            d.vetoed = predicate.test(d.entity);
            return d;
         }
      });
   }

   private boolean notVetoed(AbstractReactiveEntityOperations<Ctx, T, Exc>.Data data) {
      return !data.vetoed;
   }

   @Override
   public Mono<T> getEntity() {
      return this.data.filter(this::notVetoed).map(d -> d.entity);
   }

   public Mono<Number> getRowsUpdated() {
      return this.data.filter(this::notVetoed).map(d -> d.rowsUpdated).switchIfEmpty(Mono.just((T)0L));
   }

   protected final class Data {
      public T entity;
      public Object filter;
      public Map<QueryParameterBinding, Object> previousValues;
      public long rowsUpdated;
      public boolean vetoed = false;
   }
}
