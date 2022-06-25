package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.exceptions.OptimisticLockException;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Internal
abstract class BaseOperations<T, Exc extends Exception> {
   protected final EntityEventListener<Object> entityEventListener;
   protected final RuntimePersistentEntity<T> persistentEntity;
   protected final ConversionService<?> conversionService;

   BaseOperations(EntityEventListener<Object> entityEventListener, RuntimePersistentEntity<T> persistentEntity, ConversionService<?> conversionService) {
      this.entityEventListener = entityEventListener;
      this.persistentEntity = persistentEntity;
      this.conversionService = conversionService;
   }

   protected void checkOptimisticLocking(long expected, long received) {
      if (received != expected) {
         throw new OptimisticLockException("Execute update returned unexpected row count. Expected: " + expected + " got: " + received);
      }
   }

   public void persist() {
      try {
         boolean vetoed = this.triggerPrePersist();
         if (vetoed) {
            return;
         }

         this.cascadePre(Relation.Cascade.PERSIST);
         this.execute();
         this.triggerPostPersist();
         this.cascadePost(Relation.Cascade.PERSIST);
      } catch (Exception var2) {
         this.failed(var2, "PERSIST");
      }

   }

   public void delete() {
      this.collectAutoPopulatedPreviousValues();
      boolean vetoed = this.triggerPreRemove();
      if (!vetoed) {
         try {
            this.execute();
            this.triggerPostRemove();
         } catch (OptimisticLockException var3) {
            throw var3;
         } catch (Exception var4) {
            this.failed(var4, "DELETE");
         }

      }
   }

   public void update() {
      this.collectAutoPopulatedPreviousValues();
      boolean vetoed = this.triggerPreUpdate();
      if (!vetoed) {
         try {
            this.cascadePre(Relation.Cascade.UPDATE);
            this.execute();
            this.triggerPostUpdate();
            this.cascadePost(Relation.Cascade.UPDATE);
         } catch (OptimisticLockException var3) {
            throw var3;
         } catch (Exception var4) {
            this.failed(var4, "UPDATE");
         }

      }
   }

   protected void failed(Exception e, String operation) throws DataAccessException {
      throw new DataAccessException("Error executing " + operation + ": " + e.getMessage(), e);
   }

   protected abstract void cascadePre(Relation.Cascade cascadeType);

   protected abstract void cascadePost(Relation.Cascade cascadeType);

   protected abstract void collectAutoPopulatedPreviousValues();

   protected abstract void execute() throws Exc;

   public abstract void veto(Predicate<T> predicate);

   protected T updateEntityId(BeanProperty<T, Object> identity, T entity, Object id) {
      if (id == null) {
         return entity;
      } else {
         return (T)(identity.getType().isInstance(id) ? this.setProperty(identity, entity, id) : this.convertAndSetWithValue(identity, entity, id));
      }
   }

   protected boolean triggerPrePersist() {
      return !this.persistentEntity.hasPrePersistEventListeners() ? false : this.triggerPre(this.entityEventListener::prePersist);
   }

   protected boolean triggerPreUpdate() {
      return !this.persistentEntity.hasPreUpdateEventListeners() ? false : this.triggerPre(this.entityEventListener::preUpdate);
   }

   protected boolean triggerPreRemove() {
      return !this.persistentEntity.hasPreRemoveEventListeners() ? false : this.triggerPre(this.entityEventListener::preRemove);
   }

   protected void triggerPostUpdate() {
      if (this.persistentEntity.hasPostUpdateEventListeners()) {
         this.triggerPost(this.entityEventListener::postUpdate);
      }
   }

   protected void triggerPostRemove() {
      if (this.persistentEntity.hasPostRemoveEventListeners()) {
         this.triggerPost(this.entityEventListener::postRemove);
      }
   }

   protected void triggerPostPersist() {
      if (this.persistentEntity.hasPostPersistEventListeners()) {
         this.triggerPost(this.entityEventListener::postPersist);
      }
   }

   protected abstract boolean triggerPre(Function<EntityEventContext<Object>, Boolean> fn);

   protected abstract void triggerPost(Consumer<EntityEventContext<Object>> fn);

   private <X, Y> X setProperty(BeanProperty<X, Y> beanProperty, X x, Y y) {
      if (beanProperty.isReadOnly()) {
         return beanProperty.withValue(x, y);
      } else {
         beanProperty.set(x, y);
         return x;
      }
   }

   private <B, L> B convertAndSetWithValue(BeanProperty<B, L> beanProperty, B bean, L value) {
      Argument<L> argument = beanProperty.asArgument();
      ArgumentConversionContext<L> context = ConversionContext.of(argument);
      L convertedValue = (L)this.conversionService
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
}
