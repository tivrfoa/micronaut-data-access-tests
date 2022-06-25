package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentAssociationPath;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

@Internal
abstract class AbstractCascadeOperations {
   private final ConversionService<?> conversionService;

   protected AbstractCascadeOperations(ConversionService<?> conversionService) {
      this.conversionService = conversionService;
   }

   protected <T> void cascade(
      AnnotationMetadata annotationMetadata,
      Class<?> repositoryType,
      boolean fkOnly,
      Relation.Cascade cascadeType,
      AbstractCascadeOperations.CascadeContext ctx,
      RuntimePersistentEntity<T> persistentEntity,
      T entity,
      List<AbstractCascadeOperations.CascadeOp> cascadeOps
   ) {
      for(RuntimeAssociation<T> association : persistentEntity.getAssociations()) {
         BeanProperty<T, Object> beanProperty = association.getProperty();
         Object child = beanProperty.get(entity);
         if (child != null) {
            if (association instanceof Embedded) {
               this.cascade(
                  annotationMetadata, repositoryType, fkOnly, cascadeType, ctx.embedded(association), association.getAssociatedEntity(), child, cascadeOps
               );
            } else if (association.doesCascade(new Relation.Cascade[]{cascadeType})
               && (fkOnly || !association.isForeignKey())
               && !association.getInverseSide().map(assoc -> ctx.rootAssociations.contains(assoc) || ctx.associations.contains(assoc)).orElse(false)) {
               RuntimePersistentEntity<Object> associatedEntity = association.getAssociatedEntity();
               switch(association.getKind()) {
                  case ONE_TO_ONE:
                  case MANY_TO_ONE:
                     cascadeOps.add(
                        new AbstractCascadeOperations.CascadeOneOp(
                           annotationMetadata, repositoryType, ctx.relation(association), cascadeType, associatedEntity, child
                        )
                     );
                     break;
                  case ONE_TO_MANY:
                  case MANY_TO_MANY:
                     PersistentAssociationPath inverse = (PersistentAssociationPath)association.getInversePathSide().orElse(null);
                     Iterable<Object> children = (Iterable)association.getProperty().get(entity);
                     if (children == null || !children.iterator().hasNext()) {
                        break;
                     }

                     if (inverse != null && inverse.getAssociation().getKind() == Relation.Kind.MANY_TO_ONE) {
                        List<Object> entities = new ArrayList(CollectionUtils.iterableToList(children));
                        ListIterator<Object> iterator = entities.listIterator();

                        while(iterator.hasNext()) {
                           Object c = iterator.next();
                           Object newC = inverse.setPropertyValue(c, entity);
                           if (c != newC) {
                              iterator.set(newC);
                           }
                        }

                        children = entities;
                     }

                     cascadeOps.add(
                        new AbstractCascadeOperations.CascadeManyOp(
                           annotationMetadata, repositoryType, ctx.relation(association), cascadeType, associatedEntity, children
                        )
                     );
                     break;
                  default:
                     throw new IllegalArgumentException("Cannot cascade for relation: " + association.getKind());
               }
            }
         }
      }

   }

   protected <T> T afterCascadedOne(T entity, List<Association> associations, Object prevChild, Object newChild) {
      RuntimeAssociation<Object> association = (RuntimeAssociation)associations.iterator().next();
      if (associations.size() == 1) {
         if (association.isForeignKey()) {
            PersistentAssociationPath inverse = (PersistentAssociationPath)association.getInversePathSide().orElse(null);
            if (inverse != null) {
               newChild = inverse.setPropertyValue(newChild, entity);
            }
         }

         if (prevChild != newChild) {
            entity = this.setProperty(association.getProperty(), entity, newChild);
         }

         return entity;
      } else {
         BeanProperty<T, Object> property = association.getProperty();
         Object innerEntity = property.get(entity);
         Object newInnerEntity = this.afterCascadedOne(innerEntity, associations.subList(1, associations.size()), prevChild, newChild);
         if (newInnerEntity != innerEntity) {
            innerEntity = this.<T, Object>convertAndSetWithValue(property, entity, newInnerEntity);
         }

         return (T)innerEntity;
      }
   }

   protected <T> T afterCascadedMany(T entity, List<Association> associations, Iterable<Object> prevChildren, List<Object> newChildren) {
      RuntimeAssociation<Object> association = (RuntimeAssociation)associations.iterator().next();
      if (associations.size() == 1) {
         ListIterator<Object> iterator = newChildren.listIterator();

         while(iterator.hasNext()) {
            Object c = iterator.next();
            if (association.isForeignKey()) {
               PersistentAssociationPath inverse = (PersistentAssociationPath)association.getInversePathSide().orElse(null);
               if (inverse != null) {
                  Object newc = inverse.setPropertyValue(c, entity);
                  if (c != newc) {
                     iterator.set(newc);
                  }
               }
            }
         }

         if (prevChildren != newChildren) {
            entity = this.convertAndSetWithValue(association.getProperty(), entity, newChildren);
         }

         return entity;
      } else {
         BeanProperty<T, Object> property = association.getProperty();
         Object innerEntity = property.get(entity);
         Object newInnerEntity = this.afterCascadedMany(innerEntity, associations.subList(1, associations.size()), prevChildren, newChildren);
         if (newInnerEntity != innerEntity) {
            innerEntity = this.<T, Object>convertAndSetWithValue(property, entity, newInnerEntity);
         }

         return (T)innerEntity;
      }
   }

   private <X, Y> X setProperty(BeanProperty<X, Y> beanProperty, X x, Y y) {
      if (beanProperty.isReadOnly()) {
         return beanProperty.withValue(x, y);
      } else {
         beanProperty.set(x, y);
         return x;
      }
   }

   private <B, T> B convertAndSetWithValue(BeanProperty<B, T> beanProperty, B bean, T value) {
      Argument<T> argument = beanProperty.asArgument();
      ArgumentConversionContext<T> context = ConversionContext.of(argument);
      T convertedValue = (T)this.conversionService
         .convert(value, context)
         .orElseThrow(
            () -> new ConversionErrorException(
                  argument,
                  (ConversionError)context.getLastError()
                     .orElse(
                        (ConversionError)() -> new IllegalArgumentException("Value [" + value + "] cannot be converted to type : " + beanProperty.getType())
                     )
               )
         );
      if (beanProperty.isReadOnly()) {
         return beanProperty.withValue(bean, convertedValue);
      } else {
         beanProperty.set(bean, convertedValue);
         return bean;
      }
   }

   private static List<Association> associated(List<Association> associations, Association association) {
      if (associations == null) {
         return Collections.singletonList(association);
      } else {
         List<Association> newAssociations = new ArrayList(associations.size() + 1);
         newAssociations.addAll(associations);
         newAssociations.add(association);
         return newAssociations;
      }
   }

   protected static final class CascadeContext {
      public final List<Association> rootAssociations;
      public final Object parent;
      public final RuntimePersistentEntity<Object> parentPersistentEntity;
      public final List<Association> associations;

      CascadeContext(List<Association> rootAssociations, Object parent, RuntimePersistentEntity<Object> parentPersistentEntity, List<Association> associations) {
         this.rootAssociations = rootAssociations;
         this.parent = parent;
         this.parentPersistentEntity = parentPersistentEntity;
         this.associations = associations;
      }

      public static AbstractCascadeOperations.CascadeContext of(
         List<Association> rootAssociations, Object parent, RuntimePersistentEntity<Object> parentPersistentEntity
      ) {
         return new AbstractCascadeOperations.CascadeContext(rootAssociations, parent, parentPersistentEntity, Collections.emptyList());
      }

      AbstractCascadeOperations.CascadeContext embedded(Association association) {
         return new AbstractCascadeOperations.CascadeContext(
            this.rootAssociations, this.parent, this.parentPersistentEntity, AbstractCascadeOperations.associated(this.associations, association)
         );
      }

      AbstractCascadeOperations.CascadeContext relation(Association association) {
         return new AbstractCascadeOperations.CascadeContext(
            this.rootAssociations, this.parent, this.parentPersistentEntity, AbstractCascadeOperations.associated(this.associations, association)
         );
      }

      public Association getAssociation() {
         return CollectionUtils.last(this.associations);
      }
   }

   protected static final class CascadeManyOp extends AbstractCascadeOperations.CascadeOp {
      public final Iterable<Object> children;

      CascadeManyOp(
         AnnotationMetadata annotationMetadata,
         Class<?> repositoryType,
         AbstractCascadeOperations.CascadeContext ctx,
         Relation.Cascade cascadeType,
         RuntimePersistentEntity<Object> childPersistentEntity,
         Iterable<Object> children
      ) {
         super(annotationMetadata, repositoryType, ctx, cascadeType, childPersistentEntity);
         this.children = children;
      }
   }

   protected static final class CascadeOneOp extends AbstractCascadeOperations.CascadeOp {
      public final Object child;

      CascadeOneOp(
         AnnotationMetadata annotationMetadata,
         Class<?> repositoryType,
         AbstractCascadeOperations.CascadeContext ctx,
         Relation.Cascade cascadeType,
         RuntimePersistentEntity<Object> childPersistentEntity,
         Object child
      ) {
         super(annotationMetadata, repositoryType, ctx, cascadeType, childPersistentEntity);
         this.child = child;
      }
   }

   protected abstract static class CascadeOp {
      public final AnnotationMetadata annotationMetadata;
      public final Class<?> repositoryType;
      public final AbstractCascadeOperations.CascadeContext ctx;
      public final Relation.Cascade cascadeType;
      public final RuntimePersistentEntity<Object> childPersistentEntity;

      CascadeOp(
         AnnotationMetadata annotationMetadata,
         Class<?> repositoryType,
         AbstractCascadeOperations.CascadeContext ctx,
         Relation.Cascade cascadeType,
         RuntimePersistentEntity<Object> childPersistentEntity
      ) {
         this.annotationMetadata = annotationMetadata;
         this.repositoryType = repositoryType;
         this.ctx = ctx;
         this.cascadeType = cascadeType;
         this.childPersistentEntity = childPersistentEntity;
      }
   }
}
