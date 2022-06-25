package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Internal
final class SingletonScope {
   private final Map<SingletonScope.BeanDefinitionIdentity, Object> singletonsInCreationLocks = new ConcurrentHashMap(5, 1.0F);
   private final Map<SingletonScope.BeanDefinitionIdentity, BeanRegistration> singletonByBeanDefinition = new ConcurrentHashMap(100);
   private final Map<DefaultBeanContext.BeanKey, BeanRegistration> singletonByArgumentAndQualifier = new ConcurrentHashMap(100);

   @NonNull
   <T> BeanRegistration<T> getOrCreate(
      @NonNull DefaultBeanContext beanContext,
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> definition,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier
   ) {
      BeanRegistration<T> beanRegistration = this.findBeanRegistration(definition, beanType, qualifier);
      if (beanRegistration != null) {
         return beanRegistration;
      } else {
         SingletonScope.BeanDefinitionIdentity identity = SingletonScope.BeanDefinitionIdentity.of(definition);
         BeanRegistration<T> existingRegistration = (BeanRegistration)this.singletonByBeanDefinition.get(identity);
         if (existingRegistration != null) {
            return existingRegistration;
         } else {
            Object lock = this.singletonsInCreationLocks.computeIfAbsent(identity, beanDefinitionIdentity -> new Object());
            synchronized(lock) {
               BeanRegistration<T> newRegistration;
               try {
                  existingRegistration = (BeanRegistration)this.singletonByBeanDefinition.get(identity);
                  if (existingRegistration == null) {
                     newRegistration = beanContext.createRegistration(resolutionContext, beanType, qualifier, definition, false);
                     this.registerSingletonBean(newRegistration, qualifier);
                     return newRegistration;
                  }

                  newRegistration = existingRegistration;
               } finally {
                  this.singletonsInCreationLocks.remove(identity);
               }

               return newRegistration;
            }
         }
      }
   }

   @NonNull
   <T> BeanRegistration<T> registerSingletonBean(@NonNull BeanRegistration<T> registration, Qualifier qualifier) {
      BeanDefinition<T> beanDefinition = registration.beanDefinition;
      this.singletonByBeanDefinition.put(SingletonScope.BeanDefinitionIdentity.of(beanDefinition), registration);
      if (!beanDefinition.isSingleton()) {
         DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanDefinition, qualifier);
         this.singletonByArgumentAndQualifier.put(beanKey, registration);
      }

      if (beanDefinition instanceof BeanDefinitionDelegate || beanDefinition instanceof NoInjectionBeanDefinition) {
         DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanDefinition, beanDefinition.getDeclaredQualifier());
         this.singletonByArgumentAndQualifier.put(beanKey, registration);
      }

      if (registration.bean != null && registration.bean.getClass() != beanDefinition.getBeanType()) {
         DefaultBeanContext.BeanKey<T> concrete = new DefaultBeanContext.BeanKey<>(registration.bean.getClass(), qualifier);
         this.singletonByArgumentAndQualifier.put(concrete, registration);
      }

      return registration;
   }

   <T> boolean containsBean(Argument<T> beanType, Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
      return this.singletonByArgumentAndQualifier.containsKey(beanKey);
   }

   @NonNull
   Collection<BeanRegistration> getBeanRegistrations() {
      return this.singletonByBeanDefinition.values();
   }

   @NonNull
   Collection<BeanRegistration<?>> getBeanRegistrations(@NonNull Qualifier<?> qualifier) {
      List<BeanRegistration<?>> beanRegistrations = new ArrayList();

      for(BeanRegistration<?> beanRegistration : this.singletonByBeanDefinition.values()) {
         BeanDefinition beanDefinition = beanRegistration.beanDefinition;
         if (qualifier.reduce(beanDefinition.getBeanType(), Stream.of(beanDefinition)).findFirst().isPresent()) {
            beanRegistrations.add(beanRegistration);
         }
      }

      return beanRegistrations;
   }

   @NonNull
   <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Class<T> beanType) {
      List<BeanRegistration<T>> beanRegistrations = new ArrayList();

      for(BeanRegistration<?> beanRegistration : this.singletonByBeanDefinition.values()) {
         BeanDefinition beanDefinition = beanRegistration.beanDefinition;
         if (beanType.isAssignableFrom(beanDefinition.getBeanType())) {
            beanRegistrations.add(beanRegistration);
         }
      }

      return beanRegistrations;
   }

   @Nullable
   <T> BeanRegistration<T> findBeanRegistration(@NonNull BeanDefinition<T> beanDefinition, @Nullable Qualifier<T> qualifier) {
      return this.findBeanRegistration(beanDefinition, beanDefinition.asArgument(), qualifier);
   }

   @Nullable
   <T> BeanRegistration<T> findBeanRegistration(@NonNull BeanIdentifier identifier) {
      for(BeanRegistration registration : this.singletonByBeanDefinition.values()) {
         if (registration.identifier.equals(identifier)) {
            return registration;
         }
      }

      return null;
   }

   @Nullable
   <T> BeanRegistration<T> findBeanRegistration(@Nullable T bean) {
      if (bean == null) {
         return null;
      } else {
         for(BeanRegistration beanRegistration : this.singletonByBeanDefinition.values()) {
            if (bean == beanRegistration.getBean()) {
               return beanRegistration;
            }
         }

         return null;
      }
   }

   @Nullable
   <T> BeanRegistration<T> findBeanRegistration(@NonNull BeanDefinition<T> definition) {
      return (BeanRegistration<T>)this.singletonByBeanDefinition.get(SingletonScope.BeanDefinitionIdentity.of(definition));
   }

   @Nullable
   <T> BeanRegistration<T> findBeanRegistration(@NonNull BeanDefinition<T> beanDefinition, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      BeanRegistration<T> beanRegistration = (BeanRegistration)this.singletonByBeanDefinition.get(SingletonScope.BeanDefinitionIdentity.of(beanDefinition));
      return beanRegistration == null ? this.findCachedSingletonBeanRegistration(beanType, qualifier) : beanRegistration;
   }

   @Nullable
   <T> BeanRegistration<T> findCachedSingletonBeanRegistration(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
      BeanRegistration<T> beanRegistration = (BeanRegistration)this.singletonByArgumentAndQualifier.get(beanKey);
      return beanRegistration != null && beanRegistration.bean != null ? beanRegistration : null;
   }

   @Nullable
   <T> BeanDefinition<T> findCachedSingletonBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      BeanRegistration<T> reg = this.findCachedSingletonBeanRegistration(beanType, qualifier);
      return reg != null ? reg.getBeanDefinition() : null;
   }

   synchronized <T> void purgeCacheForBeanInstance(BeanDefinition<T> beanDefinition, T bean) {
      this.singletonByBeanDefinition.remove(SingletonScope.BeanDefinitionIdentity.of(beanDefinition));
      this.singletonByArgumentAndQualifier.entrySet().removeIf(entry -> ((DefaultBeanContext.BeanKey)entry.getKey()).beanType.isInstance(bean));
   }

   void clear() {
      this.singletonByBeanDefinition.clear();
      this.singletonByArgumentAndQualifier.clear();
   }

   static final class BeanDefinitionDelegatedIdentity implements SingletonScope.BeanDefinitionIdentity {
      private final BeanDefinitionDelegate<?> beanDefinitionDelegate;

      BeanDefinitionDelegatedIdentity(BeanDefinitionDelegate<?> beanDefinitionDelegate) {
         this.beanDefinitionDelegate = beanDefinitionDelegate;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            SingletonScope.BeanDefinitionDelegatedIdentity that = (SingletonScope.BeanDefinitionDelegatedIdentity)o;
            if (this.beanDefinitionDelegate.definition.getClass() != that.beanDefinitionDelegate.definition.getClass()) {
               return false;
            } else {
               return Objects.equals(this.beanDefinitionDelegate.getAttributes(), that.beanDefinitionDelegate.getAttributes())
                  && Objects.equals(this.beanDefinitionDelegate.getQualifier(), that.beanDefinitionDelegate.getQualifier());
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.beanDefinitionDelegate.definition.hashCode();
      }
   }

   interface BeanDefinitionIdentity {
      static SingletonScope.BeanDefinitionIdentity of(BeanDefinition<?> beanDefinition) {
         if (beanDefinition instanceof BeanDefinitionDelegate) {
            return new SingletonScope.BeanDefinitionDelegatedIdentity((BeanDefinitionDelegate<?>)beanDefinition);
         } else {
            return (SingletonScope.BeanDefinitionIdentity)(beanDefinition instanceof NoInjectionBeanDefinition
               ? new SingletonScope.NoInjectionBeanDefinitionIdentity((NoInjectionBeanDefinition<?>)beanDefinition)
               : new SingletonScope.SimpleBeanDefinitionIdentity(beanDefinition));
         }
      }
   }

   static final class NoInjectionBeanDefinitionIdentity implements SingletonScope.BeanDefinitionIdentity {
      private final NoInjectionBeanDefinition<?> beanDefinition;

      NoInjectionBeanDefinitionIdentity(NoInjectionBeanDefinition<?> beanDefinition) {
         this.beanDefinition = beanDefinition;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            SingletonScope.NoInjectionBeanDefinitionIdentity that = (SingletonScope.NoInjectionBeanDefinitionIdentity)o;
            if (this.beanDefinition.getBeanType() != that.beanDefinition.getBeanType()) {
               return false;
            } else {
               Qualifier<?> qualifier = this.beanDefinition.getQualifier();
               Qualifier<?> thatQualifier = that.beanDefinition.getQualifier();
               if (qualifier == thatQualifier) {
                  return true;
               } else {
                  return qualifier != null && qualifier.equals(thatQualifier);
               }
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.beanDefinition.getBeanType().hashCode();
      }
   }

   static final class SimpleBeanDefinitionIdentity implements SingletonScope.BeanDefinitionIdentity {
      private final Class<?> beanDefinitionClass;

      SimpleBeanDefinitionIdentity(BeanDefinition<?> beanDefinition) {
         this.beanDefinitionClass = beanDefinition.getClass();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            SingletonScope.SimpleBeanDefinitionIdentity that = (SingletonScope.SimpleBeanDefinitionIdentity)o;
            return this.beanDefinitionClass == that.beanDefinitionClass;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.beanDefinitionClass.hashCode();
      }
   }
}
