package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public final class SyncCascadeOperations<Ctx extends OperationContext> extends AbstractCascadeOperations {
   private static final Logger LOG = LoggerFactory.getLogger(SyncCascadeOperations.class);
   private final SyncCascadeOperations.SyncCascadeOperationsHelper<Ctx> helper;

   public SyncCascadeOperations(ConversionService<?> conversionService, SyncCascadeOperations.SyncCascadeOperationsHelper<Ctx> helper) {
      super(conversionService);
      this.helper = helper;
   }

   public <T> T cascadeEntity(Ctx ctx, T entity, RuntimePersistentEntity<T> persistentEntity, boolean isPost, Relation.Cascade cascadeType) {
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

      for(AbstractCascadeOperations.CascadeOp cascadeOp : cascadeOps) {
         if (cascadeOp instanceof AbstractCascadeOperations.CascadeOneOp) {
            AbstractCascadeOperations.CascadeOneOp cascadeOneOp = (AbstractCascadeOperations.CascadeOneOp)cascadeOp;
            RuntimePersistentEntity<Object> childPersistentEntity = cascadeOp.childPersistentEntity;
            Object child = cascadeOneOp.child;
            if (!ctx.persisted.contains(child)) {
               RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
               boolean hasId = identity.getProperty().get(child) != null;
               if ((!hasId || identity instanceof Association) && cascadeType == Relation.Cascade.PERSIST) {
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Cascading PERSIST for '{}' association: '{}'", persistentEntity.getName(), cascadeOp.ctx.associations);
                  }

                  Object persisted = this.helper.persistOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity);
                  entity = this.afterCascadedOne(entity, cascadeOp.ctx.associations, child, persisted);
                  child = persisted;
               } else if (hasId && cascadeType == Relation.Cascade.UPDATE) {
                  if (LOG.isDebugEnabled()) {
                     LOG.debug(
                        "Cascading MERGE for '{}' ({}) association: '{}'",
                        persistentEntity.getName(),
                        persistentEntity.getIdentity().getProperty().get(entity),
                        cascadeOp.ctx.associations
                     );
                  }

                  Object updated = this.helper.updateOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity);
                  entity = this.afterCascadedOne(entity, cascadeOp.ctx.associations, child, updated);
                  child = updated;
               }

               RuntimeAssociation<Object> association = (RuntimeAssociation)cascadeOp.ctx.getAssociation();
               if (!hasId
                  && (cascadeType == Relation.Cascade.PERSIST || cascadeType == Relation.Cascade.UPDATE)
                  && SqlQueryBuilder.isForeignKeyWithJoinTable(association)) {
                  this.helper.persistManyAssociation(ctx, association, entity, persistentEntity, child, childPersistentEntity);
               }

               ctx.persisted.add(child);
            }
         } else if (cascadeOp instanceof AbstractCascadeOperations.CascadeManyOp) {
            AbstractCascadeOperations.CascadeManyOp cascadeManyOp = (AbstractCascadeOperations.CascadeManyOp)cascadeOp;
            RuntimePersistentEntity<Object> childPersistentEntity = cascadeManyOp.childPersistentEntity;
            List<Object> entities;
            if (cascadeType == Relation.Cascade.UPDATE) {
               entities = CollectionUtils.iterableToList(cascadeManyOp.children);
               ListIterator<Object> iterator = entities.listIterator();

               while(iterator.hasNext()) {
                  Object child = iterator.next();
                  if (!ctx.persisted.contains(child)) {
                     RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
                     Object value;
                     if (identity.getProperty().get(child) == null) {
                        value = this.helper.persistOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity);
                     } else {
                        value = this.helper.updateOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity);
                     }

                     iterator.set(value);
                  }
               }
            } else {
               if (cascadeType != Relation.Cascade.PERSIST) {
                  continue;
               }

               if (this.helper.isSupportsBatchInsert(ctx, childPersistentEntity)) {
                  RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
                  Predicate<Object> veto = val -> ctx.persisted.contains(val) || identity.getProperty().get(val) != null && !(identity instanceof Association);
                  entities = this.helper.persistBatch(ctx, cascadeManyOp.children, childPersistentEntity, veto);
               } else {
                  entities = CollectionUtils.iterableToList(cascadeManyOp.children);
                  ListIterator<Object> iterator = entities.listIterator();

                  while(iterator.hasNext()) {
                     Object child = iterator.next();
                     if (!ctx.persisted.contains(child)) {
                        RuntimePersistentProperty<Object> identity = childPersistentEntity.getIdentity();
                        if (identity.getProperty().get(child) == null) {
                           Object persisted = this.helper.persistOne(ctx, (RuntimePersistentEntity<Object>)child, childPersistentEntity);
                           iterator.set(persisted);
                        }
                     }
                  }
               }
            }

            entity = this.afterCascadedMany(entity, cascadeOp.ctx.associations, cascadeManyOp.children, entities);
            RuntimeAssociation<Object> association = (RuntimeAssociation)cascadeOp.ctx.getAssociation();
            if (SqlQueryBuilder.isForeignKeyWithJoinTable(association) && !entities.isEmpty()) {
               if (this.helper.isSupportsBatchInsert(ctx, childPersistentEntity)) {
                  this.helper
                     .persistManyAssociationBatch(ctx, association, cascadeOp.ctx.parent, cascadeOp.ctx.parentPersistentEntity, entities, childPersistentEntity);
               } else {
                  for(Object e : cascadeManyOp.children) {
                     if (!ctx.persisted.contains(e)) {
                        this.helper
                           .persistManyAssociation(ctx, association, cascadeOp.ctx.parent, cascadeOp.ctx.parentPersistentEntity, e, childPersistentEntity);
                     }
                  }
               }
            }

            ctx.persisted.addAll(entities);
         }
      }

      return entity;
   }

   public interface SyncCascadeOperationsHelper<Ctx extends OperationContext> {
      default boolean isSupportsBatchInsert(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      default boolean isSupportsBatchUpdate(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      default boolean isSupportsBatchDelete(Ctx ctx, RuntimePersistentEntity<?> persistentEntity) {
         return true;
      }

      <T> T persistOne(Ctx ctx, T entityValue, RuntimePersistentEntity<T> persistentEntity);

      <T> List<T> persistBatch(Ctx ctx, Iterable<T> entityValues, RuntimePersistentEntity<T> persistentEntity, Predicate<T> predicate);

      <T> T updateOne(Ctx ctx, T entityValue, RuntimePersistentEntity<T> persistentEntity);

      void persistManyAssociation(
         Ctx ctx,
         RuntimeAssociation runtimeAssociation,
         Object parentEntityValue,
         RuntimePersistentEntity<Object> parentPersistentEntity,
         Object childEntityValue,
         RuntimePersistentEntity<Object> childPersistentEntity
      );

      void persistManyAssociationBatch(
         Ctx ctx,
         RuntimeAssociation runtimeAssociation,
         Object parentEntityValue,
         RuntimePersistentEntity<Object> parentPersistentEntity,
         Iterable<Object> childEntityValues,
         RuntimePersistentEntity<Object> childPersistentEntity
      );
   }
}
