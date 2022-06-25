package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class ReactiveCascadeOperations<Ctx extends OperationContext> extends AbstractCascadeOperations {
   private static final Logger LOG = LoggerFactory.getLogger(ReactiveCascadeOperations.class);
   private final ReactiveCascadeOperations.ReactiveCascadeOperationsHelper<Ctx> helper;

   public ReactiveCascadeOperations(ConversionService<?> conversionService, ReactiveCascadeOperations.ReactiveCascadeOperationsHelper<Ctx> helper) {
      super(conversionService);
      this.helper = helper;
   }

   public <T> Mono<T> cascadeEntity(Ctx ctx, T entity, RuntimePersistentEntity<T> persistentEntity, boolean isPost, Relation.Cascade cascadeType) {
      List<AbstractCascadeOperations.CascadeOp> cascadeOps = new ArrayList();
      this.cascade(
         ctx.annotationMetadata,
         ctx.repositoryType,
         isPost,
         cascadeType,
         AbstractCascadeOperations.CascadeContext.of(ctx.associations, entity, persistentEntity),
         persistentEntity,
         entity,
         cascadeOps
      );
      Mono<T> monoEntity = Mono.just(entity);

      for(AbstractCascadeOperations.CascadeOp cascadeOp : cascadeOps) {
         if (cascadeOp instanceof AbstractCascadeOperations.CascadeOneOp) {
            AbstractCascadeOperations.CascadeOneOp cascadeOneOp = (AbstractCascadeOperations.CascadeOneOp)cascadeOp;
            Object child = cascadeOneOp.child;
            RuntimePersistentEntity<Object> childPersistentEntity = cascadeOneOp.childPersistentEntity;
            RuntimeAssociation<Object> association = (RuntimeAssociation)cascadeOp.ctx.getAssociation();
            if (!ctx.persisted.contains(child)) {
               monoEntity = monoEntity.flatMap(
                  e -> {
                     RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
                     boolean hasId = identity.getProperty().get(child) != null;
                     Mono<T> thisEntity;
                     Mono<Object> childMono;
                     if ((!hasId || identity instanceof Association) && cascadeType == Relation.Cascade.PERSIST) {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Cascading one PERSIST for '{}' association: '{}'", persistentEntity.getName(), cascadeOp.ctx.associations);
                        }
   
                        Mono<Object> var15x = this.helper.persistOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity).cache();
                        thisEntity = var15x.map(persistedEntity -> this.afterCascadedOne(e, cascadeOp.ctx.associations, child, persistedEntity));
                        childMono = var15x;
                     } else if (hasId && cascadeType == Relation.Cascade.UPDATE) {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug(
                              "Cascading one UPDATE for '{}' ({}) association: '{}'",
                              persistentEntity.getName(),
                              persistentEntity.getIdentity().getProperty().get(entity),
                              cascadeOp.ctx.associations
                           );
                        }
   
                        Mono<Object> updated = this.helper.updateOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity).cache();
                        thisEntity = updated.map(updatedEntity -> this.afterCascadedOne(e, cascadeOp.ctx.associations, child, updatedEntity));
                        childMono = updated;
                     } else {
                        childMono = Mono.just(child);
                        thisEntity = Mono.just((T)e);
                     }
   
                     return !hasId
                           && (cascadeType == Relation.Cascade.PERSIST || cascadeType == Relation.Cascade.UPDATE)
                           && SqlQueryBuilder.isForeignKeyWithJoinTable(association)
                        ? childMono.flatMap(c -> {
                           if (ctx.persisted.contains(c)) {
                              return Mono.just(e);
                           } else {
                              ctx.persisted.add(c);
                              return thisEntity.flatMap(e2 -> {
                                 Mono<Void> op = this.helper.persistManyAssociation(ctx, association, e2, persistentEntity, c, childPersistentEntity);
                                 return op.thenReturn(e2);
                              });
                           }
                        })
                        : childMono.flatMap(c -> {
                           ctx.persisted.add(c);
                           return thisEntity;
                        });
                  }
               );
            }
         } else if (cascadeOp instanceof AbstractCascadeOperations.CascadeManyOp) {
            AbstractCascadeOperations.CascadeManyOp cascadeManyOp = (AbstractCascadeOperations.CascadeManyOp)cascadeOp;
            RuntimePersistentEntity<Object> childPersistentEntity = cascadeManyOp.childPersistentEntity;
            if (cascadeType == Relation.Cascade.UPDATE) {
               monoEntity = this.updateChildren(ctx, monoEntity, cascadeOp, cascadeManyOp, childPersistentEntity, e -> {
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Cascading many UPDATE for '{}' association: '{}'", persistentEntity.getName(), cascadeOp.ctx.associations);
                  }

                  Flux<Object> childrenFlux = Flux.empty();

                  for(Object child : cascadeManyOp.children) {
                     if (!ctx.persisted.contains(child)) {
                        Mono<Object> modifiedEntity;
                        if (childPersistentEntity.getIdentity().getProperty().get(child) == null) {
                           modifiedEntity = this.helper.persistOne(ctx, child, childPersistentEntity);
                        } else {
                           modifiedEntity = this.helper.updateOne(ctx, child, childPersistentEntity);
                        }

                        childrenFlux = childrenFlux.concatWith(modifiedEntity);
                     }
                  }

                  return childrenFlux.collectList();
               });
            } else if (cascadeType == Relation.Cascade.PERSIST) {
               if (this.helper.isSupportsBatchInsert(ctx, persistentEntity)) {
                  monoEntity = this.updateChildren(
                     ctx,
                     monoEntity,
                     cascadeOp,
                     cascadeManyOp,
                     childPersistentEntity,
                     e -> {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Cascading many PERSIST for '{}' association: '{}'", persistentEntity.getName(), cascadeOp.ctx.associations);
                        }
   
                        RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
                        Predicate<Object> veto = val -> ctx.persisted.contains(val)
                              || identity.getProperty().get(val) != null && !(identity instanceof Association);
                        Flux<Object> inserted = this.helper.persistBatch(ctx, cascadeManyOp.children, childPersistentEntity, veto);
                        return inserted.collectList();
                     }
                  );
               } else {
                  monoEntity = this.updateChildren(ctx, monoEntity, cascadeOp, cascadeManyOp, childPersistentEntity, e -> {
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("Cascading many PERSIST for '{}' association: '{}'", persistentEntity.getName(), cascadeOp.ctx.associations);
                     }

                     Flux<Object> childrenFlux = Flux.empty();

                     for(Object child : cascadeManyOp.children) {
                        if (!ctx.persisted.contains(child) && childPersistentEntity.getIdentity().getProperty().get(child) == null) {
                           Mono<Object> persisted = this.helper.persistOne(ctx, child, childPersistentEntity);
                           childrenFlux = childrenFlux.concatWith(persisted);
                        } else {
                           childrenFlux = childrenFlux.concatWith(Mono.just(child));
                        }
                     }

                     return childrenFlux.collectList();
                  });
               }
            }
         }
      }

      return monoEntity;
   }

   private <T> Mono<T> updateChildren(
      Ctx ctx,
      Mono<T> monoEntity,
      AbstractCascadeOperations.CascadeOp cascadeOp,
      AbstractCascadeOperations.CascadeManyOp cascadeManyOp,
      RuntimePersistentEntity<Object> childPersistentEntity,
      Function<T, Mono<List<Object>>> fn
   ) {
      return monoEntity.flatMap(
         e -> ((Mono)fn.apply(e))
               .flatMap(
                  newChildren -> {
                     T entityAfterCascade = this.afterCascadedMany((T)e, cascadeOp.ctx.associations, cascadeManyOp.children, newChildren);
                     RuntimeAssociation<Object> association = (RuntimeAssociation)cascadeOp.ctx.getAssociation();
                     if (SqlQueryBuilder.isForeignKeyWithJoinTable(association)) {
                        if (this.helper.isSupportsBatchInsert(ctx, cascadeOp.ctx.parentPersistentEntity)) {
                           Predicate<Object> veto = ctx.persisted::contains;
                           Mono<Void> op = this.helper
                              .persistManyAssociationBatch(
                                 ctx, association, cascadeOp.ctx.parent, cascadeOp.ctx.parentPersistentEntity, newChildren, childPersistentEntity, veto
                              );
                           return op.thenReturn(entityAfterCascade);
                        } else {
                           Mono<T> res = Mono.just(entityAfterCascade);
         
                           for(Object child : newChildren) {
                              if (!ctx.persisted.contains(child)) {
                                 Mono<Void> op = this.helper
                                    .persistManyAssociation(
                                       ctx, association, cascadeOp.ctx.parent, cascadeOp.ctx.parentPersistentEntity, child, childPersistentEntity
                                    );
                                 res = res.flatMap(op::thenReturn);
                              }
                           }
         
                           return res;
                        }
                     } else {
                        ctx.persisted.addAll(newChildren);
                        return Mono.just(entityAfterCascade);
                     }
                  }
               )
      );
   }

   public interface ReactiveCascadeOperationsHelper<Ctx extends OperationContext> {
      default boolean isSupportsBatchInsert(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      default boolean isSupportsBatchUpdate(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      default boolean isSupportsBatchDelete(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      <T> Mono<T> persistOne(Ctx ctx, T entityValue, RuntimePersistentEntity<T> persistentEntity);

      <T> Flux<T> persistBatch(Ctx ctx, Iterable<T> entityValues, RuntimePersistentEntity<T> persistentEntity, Predicate<T> predicate);

      <T> Mono<T> updateOne(Ctx ctx, T entityValue, RuntimePersistentEntity<T> persistentEntity);

      Mono<Void> persistManyAssociation(
         Ctx ctx,
         RuntimeAssociation runtimeAssociation,
         Object parentEntityValue,
         RuntimePersistentEntity<Object> parentPersistentEntity,
         Object childEntityValue,
         RuntimePersistentEntity<Object> childPersistentEntity
      );

      Mono<Void> persistManyAssociationBatch(
         Ctx ctx,
         RuntimeAssociation runtimeAssociation,
         Object parentEntityValue,
         RuntimePersistentEntity<Object> parentPersistentEntity,
         Iterable<Object> childEntityValues,
         RuntimePersistentEntity<Object> childPersistentEntity,
         Predicate<Object> veto
      );
   }
}
