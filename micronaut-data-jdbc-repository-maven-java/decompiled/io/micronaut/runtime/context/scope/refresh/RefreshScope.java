package io.micronaut.runtime.context.scope.refresh;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.LifeCycle;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.scope.BeanCreationContext;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.context.scope.CustomScope;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
@Requires(
   notEnv = {"function", "android"}
)
public class RefreshScope implements CustomScope<Refreshable>, LifeCycle<RefreshScope>, ApplicationEventListener<RefreshEvent>, Ordered {
   public static final int POSITION = -2147483548;
   private final Map<BeanIdentifier, CreatedBean<?>> refreshableBeans = new ConcurrentHashMap(10);
   private final ConcurrentMap<Object, ReadWriteLock> locks = new ConcurrentHashMap();
   private final BeanContext beanContext;

   @Deprecated
   public RefreshScope(BeanContext beanContext, @Named("io") Executor executorService) {
      this.beanContext = beanContext;
   }

   @Inject
   public RefreshScope(BeanContext beanContext) {
      this.beanContext = beanContext;
   }

   @Override
   public boolean isRunning() {
      return true;
   }

   @Override
   public Class<Refreshable> annotationType() {
      return Refreshable.class;
   }

   @Override
   public <T> T getOrCreate(BeanCreationContext<T> creationContext) {
      BeanIdentifier id = creationContext.id();
      CreatedBean<?> created = (CreatedBean)this.refreshableBeans.computeIfAbsent(id, key -> {
         CreatedBean<T> createdBean = creationContext.create();
         this.locks.putIfAbsent(createdBean.bean(), new ReentrantReadWriteLock());
         return createdBean;
      });
      return (T)created.bean();
   }

   public RefreshScope stop() {
      this.disposeOfAllBeans();
      this.locks.clear();
      return this;
   }

   @Override
   public <T> Optional<T> remove(BeanIdentifier identifier) {
      CreatedBean<?> createdBean = (CreatedBean)this.refreshableBeans.get(identifier);
      if (createdBean != null) {
         createdBean.close();
         return Optional.ofNullable(createdBean.bean());
      } else {
         return Optional.empty();
      }
   }

   public void onApplicationEvent(RefreshEvent event) {
      this.onRefreshEvent(event);
   }

   public final void onRefreshEvent(RefreshEvent event) {
      Map<String, Object> changes = event.getSource();
      if (changes == RefreshEvent.ALL_KEYS) {
         this.disposeOfAllBeans();
         this.refreshAllConfigurationProperties();
      } else {
         this.disposeOfBeanSubset(changes.keySet());
         this.refreshSubsetOfConfigurationProperties(changes.keySet());
      }

   }

   @Override
   public int getOrder() {
      return -2147483548;
   }

   @Override
   public <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      if (bean instanceof InterceptedProxy) {
         bean = (T)((InterceptedProxy)bean).interceptedTarget();
      }

      for(CreatedBean<?> created : this.refreshableBeans.values()) {
         if (created.bean() == bean) {
            return Optional.of(BeanRegistration.of(this.beanContext, created.id(), created.definition(), created.bean()));
         }
      }

      return Optional.empty();
   }

   protected ReadWriteLock getLock(Object object) {
      ReadWriteLock readWriteLock = (ReadWriteLock)this.locks.get(object);
      if (readWriteLock == null) {
         throw new IllegalStateException("No lock present for object: " + object);
      } else {
         return readWriteLock;
      }
   }

   private void refreshSubsetOfConfigurationProperties(Set<String> keySet) {
      for(BeanRegistration<?> registration : this.beanContext.getActiveBeanRegistrations(Qualifiers.byStereotype(ConfigurationProperties.class))) {
         BeanDefinition<?> definition = registration.getBeanDefinition();
         Optional<String> value = definition.stringValue(ConfigurationReader.class, "prefix");
         if (value.isPresent()) {
            String configPrefix = (String)value.get();
            if (keySet.stream().anyMatch(key -> key.startsWith(configPrefix))) {
               this.beanContext.refreshBean(registration);
            }
         }
      }

   }

   private void refreshAllConfigurationProperties() {
      for(BeanRegistration<?> registration : this.beanContext.getActiveBeanRegistrations(Qualifiers.byStereotype(ConfigurationProperties.class))) {
         this.beanContext.refreshBean(registration);
      }

   }

   private void disposeOfBeanSubset(Collection<String> keys) {
      for(Entry<BeanIdentifier, CreatedBean<?>> entry : this.refreshableBeans.entrySet()) {
         BeanDefinition<?> definition = ((CreatedBean)entry.getValue()).definition();
         String[] strings = definition.stringValues(Refreshable.class);
         if (!ArrayUtils.isEmpty(strings)) {
            for(String prefix : strings) {
               for(String k : keys) {
                  if (k.startsWith(prefix)) {
                     this.disposeOfBean((BeanIdentifier)entry.getKey());
                  }
               }
            }
         } else {
            this.disposeOfBean((BeanIdentifier)entry.getKey());
         }
      }

   }

   private void disposeOfAllBeans() {
      for(BeanIdentifier key : this.refreshableBeans.keySet()) {
         this.disposeOfBean(key);
      }

   }

   private void disposeOfBean(BeanIdentifier key) {
      CreatedBean<?> createdBean = (CreatedBean)this.refreshableBeans.remove(key);
      if (createdBean != null) {
         Object bean = createdBean.bean();
         Lock lock = this.getLock(bean).writeLock();

         try {
            lock.lock();
            createdBean.close();
            this.locks.remove(bean);
         } finally {
            lock.unlock();
         }
      }

   }
}
