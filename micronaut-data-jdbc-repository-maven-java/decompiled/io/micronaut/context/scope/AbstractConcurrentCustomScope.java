package io.micronaut.context.scope;

import io.micronaut.context.BeanRegistration;
import io.micronaut.context.LifeCycle;
import io.micronaut.context.exceptions.BeanDestructionException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanIdentifier;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConcurrentCustomScope<A extends Annotation> implements CustomScope<A>, LifeCycle<AbstractConcurrentCustomScope<A>>, AutoCloseable {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractConcurrentCustomScope.class);
   private final Class<A> annotationType;
   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
   private final Lock r = this.rwl.readLock();
   private final Lock w = this.rwl.writeLock();

   protected AbstractConcurrentCustomScope(Class<A> annotationType) {
      this.annotationType = (Class)Objects.requireNonNull(annotationType, "Annotation type cannot be null");
   }

   @NonNull
   protected abstract Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation);

   @Override
   public final Class<A> annotationType() {
      return this.annotationType;
   }

   @Override
   public abstract void close();

   @NonNull
   public final AbstractConcurrentCustomScope<A> stop() {
      this.w.lock();

      AbstractConcurrentCustomScope var7;
      try {
         try {
            Map<BeanIdentifier, CreatedBean<?>> scopeMap = this.getScopeMap(false);
            this.destroyScope(scopeMap);
         } catch (IllegalStateException var5) {
         }

         this.close();
         var7 = this;
      } finally {
         this.w.unlock();
      }

      return var7;
   }

   protected void destroyScope(@Nullable Map<BeanIdentifier, CreatedBean<?>> scopeMap) {
      this.w.lock();

      try {
         if (CollectionUtils.isNotEmpty(scopeMap)) {
            for(CreatedBean<?> createdBean : scopeMap.values()) {
               try {
                  createdBean.close();
               } catch (BeanDestructionException var8) {
                  this.handleDestructionException(var8);
               }
            }

            scopeMap.clear();
         }
      } finally {
         this.w.unlock();
      }

   }

   @Override
   public final <T> T getOrCreate(BeanCreationContext<T> creationContext) {
      this.r.lock();

      Object var5;
      try {
         Map<BeanIdentifier, CreatedBean<?>> scopeMap = this.getScopeMap(true);
         BeanIdentifier id = creationContext.id();
         CreatedBean<?> createdBean = (CreatedBean)scopeMap.get(id);
         if (createdBean == null) {
            this.r.unlock();
            this.w.lock();

            try {
               createdBean = (CreatedBean)scopeMap.get(id);
               if (createdBean != null) {
                  this.r.lock();
                  return (T)createdBean.bean();
               }

               try {
                  createdBean = this.doCreate(creationContext);
                  scopeMap.put(id, createdBean);
               } finally {
                  this.r.lock();
               }

               return (T)createdBean.bean();
            } finally {
               this.w.unlock();
            }
         }

         var5 = createdBean.bean();
      } finally {
         this.r.unlock();
      }

      return (T)var5;
   }

   @NonNull
   protected <T> CreatedBean<T> doCreate(@NonNull BeanCreationContext<T> creationContext) {
      return creationContext.create();
   }

   @Override
   public final <T> Optional<T> remove(BeanIdentifier identifier) {
      if (identifier == null) {
         return Optional.empty();
      } else {
         this.w.lock();

         try {
            Map<BeanIdentifier, CreatedBean<?>> scopeMap;
            try {
               scopeMap = this.getScopeMap(false);
            } catch (IllegalStateException var10) {
               return Optional.empty();
            }

            if (!CollectionUtils.isNotEmpty(scopeMap)) {
               return Optional.empty();
            } else {
               CreatedBean<?> createdBean = (CreatedBean)scopeMap.get(identifier);
               if (createdBean != null) {
                  try {
                     createdBean.close();
                  } catch (BeanDestructionException var9) {
                     this.handleDestructionException(var9);
                  }

                  return Optional.ofNullable(createdBean.bean());
               } else {
                  return Optional.empty();
               }
            }
         } finally {
            this.w.unlock();
         }
      }
   }

   protected void handleDestructionException(BeanDestructionException e) {
      LOG.error("Error occurred destroying bean of scope @" + this.annotationType.getSimpleName() + ": " + e.getMessage(), e);
   }

   @Override
   public final <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      this.r.lock();

      try {
         Map<BeanIdentifier, CreatedBean<?>> scopeMap;
         try {
            scopeMap = this.getScopeMap(false);
         } catch (Exception var9) {
            return Optional.empty();
         }

         for(CreatedBean<?> createdBean : scopeMap.values()) {
            if (createdBean.bean() == bean) {
               if (createdBean instanceof BeanRegistration) {
                  return Optional.of(createdBean);
               }

               return Optional.of(new BeanRegistration<>(createdBean.id(), createdBean.definition(), bean));
            }
         }

         return Optional.empty();
      } finally {
         this.r.unlock();
      }
   }
}
