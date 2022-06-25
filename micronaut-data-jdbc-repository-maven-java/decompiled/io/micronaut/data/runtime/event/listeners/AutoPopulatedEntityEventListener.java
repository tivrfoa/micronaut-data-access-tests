package io.micronaut.data.runtime.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AutoPopulatedEntityEventListener implements EntityEventListener<Object> {
   private final Map<RuntimePersistentEntity<Object>, RuntimePersistentProperty<Object>[]> applicableProperties = new ConcurrentHashMap(30);

   @Override
   public final boolean supports(RuntimePersistentEntity<Object> entity, Class<? extends Annotation> eventType) {
      if (this.getEventTypes().contains(eventType) && entity.hasAutoPopulatedProperties()) {
         RuntimePersistentProperty<Object>[] properties = (RuntimePersistentProperty[])this.applicableProperties.get(entity);
         if (properties == null) {
            Collection<RuntimePersistentProperty<Object>> persistentProperties = entity.getPersistentProperties();
            List<RuntimePersistentProperty<Object>> propertyList = new ArrayList(persistentProperties.size());
            RuntimePersistentProperty<Object> identity = entity.getIdentity();
            if (identity != null && identity.isAutoPopulated()) {
               propertyList.add(identity);
            }

            RuntimePersistentProperty<Object>[] compositeIdentity = entity.getCompositeIdentity();
            if (compositeIdentity != null) {
               for(RuntimePersistentProperty<Object> compositeId : compositeIdentity) {
                  if (compositeId.isAutoPopulated()) {
                     propertyList.add(compositeId);
                  }
               }
            }

            propertyList.addAll((Collection)persistentProperties.stream().filter(PersistentProperty::isAutoPopulated).collect(Collectors.toList()));
            properties = (RuntimePersistentProperty[])propertyList.stream()
               .filter(this.getPropertyPredicate())
               .toArray(x$0 -> new RuntimePersistentProperty[x$0]);
            if (ArrayUtils.isEmpty(properties)) {
               this.applicableProperties.put(entity, RuntimePersistentProperty.EMPTY_PROPERTY_ARRAY);
            } else {
               this.applicableProperties.put(entity, properties);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @NonNull
   protected abstract List<Class<? extends Annotation>> getEventTypes();

   @NonNull
   protected abstract Predicate<RuntimePersistentProperty<Object>> getPropertyPredicate();

   @NonNull
   protected RuntimePersistentProperty<Object>[] getApplicableProperties(RuntimePersistentEntity<Object> entity) {
      RuntimePersistentProperty<Object>[] properties = (RuntimePersistentProperty[])this.applicableProperties.get(entity);
      return properties != null ? properties : RuntimePersistentProperty.EMPTY_PROPERTY_ARRAY;
   }
}
