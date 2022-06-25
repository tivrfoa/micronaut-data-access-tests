package io.micronaut.data.runtime.event;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.event.EntityEventMapping;
import io.micronaut.data.annotation.event.PostLoad;
import io.micronaut.data.annotation.event.PostPersist;
import io.micronaut.data.annotation.event.PostRemove;
import io.micronaut.data.annotation.event.PostUpdate;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.annotation.event.PreRemove;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.event.PersistenceEventException;
import io.micronaut.data.event.QueryEventContext;
import io.micronaut.data.event.listeners.PostPersistEventListener;
import io.micronaut.data.event.listeners.PostRemoveEventListener;
import io.micronaut.data.event.listeners.PostUpdateEventListener;
import io.micronaut.data.event.listeners.PrePersistEventListener;
import io.micronaut.data.event.listeners.PreRemoveEventListener;
import io.micronaut.data.event.listeners.PreUpdateEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionMethodReference;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
@Primary
public class EntityEventRegistry implements EntityEventListener<Object>, ExecutableMethodProcessor<EntityEventMapping> {
   public static final List<Class<? extends Annotation>> EVENT_TYPES = Arrays.asList(
      PostLoad.class, PostPersist.class, PostRemove.class, PostUpdate.class, PrePersist.class, PreRemove.class, PreUpdate.class
   );
   private final Collection<BeanDefinition<EntityEventListener>> allEventListeners;
   private final Map<RuntimePersistentEntity<Object>, Map<Class<? extends Annotation>, EntityEventListener<Object>>> entityToEventListeners = new ConcurrentHashMap(
      50
   );
   private final BeanContext beanContext;
   private final Map<Class<? extends Annotation>, BeanDefinitionMethodReference<Object, Object>> beanEventHandlers = new HashMap(10);

   public EntityEventRegistry(BeanContext beanContext) {
      this.beanContext = beanContext;
      this.allEventListeners = (Collection)beanContext.getBeanDefinitions(EntityEventListener.class)
         .stream()
         .filter(bd -> bd.getBeanType() != this.getClass())
         .collect(Collectors.toList());
   }

   @Override
   public boolean supports(RuntimePersistentEntity<Object> entity, Class<? extends Annotation> eventType) {
      Map<Class<? extends Annotation>, EntityEventListener<Object>> listeners = this.getListeners(entity);
      return listeners.containsKey(eventType);
   }

   @Override
   public boolean prePersist(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PrePersist.class);
         return target != null ? target.prePersist(context) : true;
      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking pre-persist event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void postPersist(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PostPersist.class);
         if (target != null) {
            target.postPersist(context);
         }

      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking post-persist event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void postLoad(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PostLoad.class);
         if (target != null) {
            target.postLoad(context);
         }

      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking post-load event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public boolean preRemove(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PreRemove.class);
         return target != null ? target.preRemove(context) : true;
      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking pre-remove event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void postRemove(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PostRemove.class);
         if (target != null) {
            target.postRemove(context);
         }

      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking post-remove event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public boolean preUpdate(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PreUpdate.class);
         return target != null ? target.preUpdate(context) : true;
      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking pre-update event listeners: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void postUpdate(@NonNull EntityEventContext<Object> context) {
      try {
         EntityEventListener<Object> target = (EntityEventListener)this.getListeners(context.getPersistentEntity()).get(PostUpdate.class);
         if (target != null) {
            target.postUpdate(context);
         }

      } catch (Exception var3) {
         throw new PersistenceEventException("An error occurred invoking post-update event listeners: " + var3.getMessage(), var3);
      }
   }

   @NonNull
   private Map<Class<? extends Annotation>, EntityEventListener<Object>> getListeners(RuntimePersistentEntity<Object> entity) {
      Map<Class<? extends Annotation>, EntityEventListener<Object>> listeners = (Map)this.entityToEventListeners.get(entity);
      if (listeners == null) {
         listeners = this.initListeners(entity);
         this.entityToEventListeners.put(entity, listeners);
      }

      return listeners;
   }

   @NonNull
   private Map<Class<? extends Annotation>, EntityEventListener<Object>> initListeners(RuntimePersistentEntity<Object> entity) {
      Map<Class<? extends Annotation>, Collection<EntityEventListener<Object>>> listeners = new HashMap(8);

      for(BeanDefinition<EntityEventListener> beanDefinition : this.allEventListeners) {
         List<Argument<?>> typeArguments = beanDefinition.getTypeArguments();
         if (typeArguments.isEmpty()) {
            typeArguments = beanDefinition.getTypeArguments(EntityEventListener.class);
         }

         if (this.isApplicableListener(entity, typeArguments)) {
            EntityEventListener<Object> eventListener = this.beanContext.getBean(beanDefinition);

            for(Class<? extends Annotation> et : EVENT_TYPES) {
               if (eventListener.supports(entity, et)) {
                  Collection<EntityEventListener<Object>> eventListeners = (Collection)listeners.computeIfAbsent(et, t -> new ArrayList(5));
                  eventListeners.add(eventListener);
               }
            }
         }
      }

      this.beanEventHandlers
         .forEach(
            (annotation, reference) -> {
               if (this.isApplicableListener(entity, Arrays.asList(reference.getArguments()))) {
                  Object bean = this.beanContext.getBean(reference.getBeanDefinition());
                  Collection<EntityEventListener<Object>> eventListenersx = (Collection)listeners.computeIfAbsent(annotation, t -> new ArrayList(5));
                  if (annotation == PrePersist.class) {
                     eventListenersx.add(
                        (PrePersistEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                              return true;
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking pre-persist event listener method [" + reference.getDescription(true) + "]: " + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  } else if (annotation == PreRemove.class) {
                     eventListenersx.add(
                        (PreRemoveEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                              return true;
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking pre-remove event listener method [" + reference.getDescription(true) + "]: " + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  } else if (annotation == PreUpdate.class) {
                     eventListenersx.add(
                        (PreUpdateEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                              return true;
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking pre-update event listener method [" + reference.getDescription(true) + "]: " + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  } else if (annotation == PostPersist.class) {
                     eventListenersx.add(
                        (PostPersistEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking post-persist event listener method ["
                                    + reference.getDescription(true)
                                    + "]: "
                                    + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  } else if (annotation == PostRemove.class) {
                     eventListenersx.add(
                        (PostRemoveEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking post-remove event listener method [" + reference.getDescription(true) + "]: " + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  } else if (annotation == PostUpdate.class) {
                     eventListenersx.add(
                        (PostUpdateEventListener<Object>)entity1 -> {
                           try {
                              reference.invoke(bean, new Object[]{entity1});
                           } catch (Exception var4x) {
                              throw new PersistenceEventException(
                                 "An error occurred invoking post-update event listener method [" + reference.getDescription(true) + "]: " + var4x.getMessage(),
                                 var4x
                              );
                           }
                        }
                     );
                  }
               }
      
            }
         );
      Map<Class<? extends Annotation>, EntityEventListener<Object>> finalListeners;
      if (listeners.isEmpty()) {
         finalListeners = Collections.emptyMap();
      } else {
         finalListeners = (Map)listeners.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
            Collection<EntityEventListener<Object>> v = (Collection)entry.getValue();
            if (v.isEmpty()) {
               return EntityEventListener.NOOP;
            } else {
               return (EntityEventListener)(v.size() == 1 ? (EntityEventListener)v.iterator().next() : new EntityEventRegistry.CompositeEventListener(v));
            }
         }));
      }

      return finalListeners;
   }

   private boolean isApplicableListener(RuntimePersistentEntity<Object> entity, List<Argument<?>> typeArguments) {
      return typeArguments.isEmpty() || ((Argument)typeArguments.get(0)).getType().isAssignableFrom(entity.getIntrospection().getBeanType());
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      Argument[] arguments = method.getArguments();
      if (arguments.length == 1) {
         for(Class<? extends Annotation> eventType : method.getAnnotationTypesByStereotype(EntityEventMapping.class)) {
            BeanDefinitionMethodReference<Object, Object> ref = BeanDefinitionMethodReference.of(beanDefinition, method);
            this.beanEventHandlers.put(eventType, ref);
         }
      }

   }

   private static final class CompositeEventListener implements EntityEventListener<Object> {
      private final EntityEventListener<Object>[] listenerArray;

      public CompositeEventListener(Collection<EntityEventListener<Object>> listeners) {
         this.listenerArray = (EntityEventListener[])listeners.stream().sorted(OrderUtil.COMPARATOR).toArray(x$0 -> new EntityEventListener[x$0]);
      }

      @Override
      public boolean supports(RuntimePersistentEntity<Object> entity, Class<? extends Annotation> eventType) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            if (listener.supports(entity, eventType)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean prePersist(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            if (!listener.prePersist(context)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void postPersist(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            listener.postPersist(context);
         }

      }

      @Override
      public void postLoad(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            listener.postLoad(context);
         }

      }

      @Override
      public boolean preRemove(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            if (!listener.preRemove(context)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void postRemove(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            listener.postRemove(context);
         }

      }

      @Override
      public boolean preUpdate(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            if (!listener.preUpdate(context)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean preQuery(@NonNull QueryEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            if (!listener.preQuery(context)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void postUpdate(@NonNull EntityEventContext<Object> context) {
         for(EntityEventListener<Object> listener : this.listenerArray) {
            listener.postUpdate(context);
         }

      }
   }
}
