package io.micronaut.core.beans;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Internal
final class BeanIntrospectionMap<T> implements BeanMap<T> {
   private final BeanIntrospection<T> beanIntrospection;
   private final T bean;

   BeanIntrospectionMap(BeanIntrospection<T> beanIntrospection, T bean) {
      this.beanIntrospection = beanIntrospection;
      this.bean = bean;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BeanIntrospectionMap<?> that = (BeanIntrospectionMap)o;
         return this.beanIntrospection.equals(that.beanIntrospection) && this.bean.equals(that.bean);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.beanIntrospection, this.bean});
   }

   @NonNull
   @Override
   public Class<T> getBeanType() {
      return this.beanIntrospection.getBeanType();
   }

   public int size() {
      return this.beanIntrospection.getPropertyNames().length;
   }

   public boolean isEmpty() {
      return this.size() == 0;
   }

   public boolean containsKey(Object key) {
      return key == null ? false : this.beanIntrospection.getProperty(key.toString()).isPresent();
   }

   public boolean containsValue(Object value) {
      return this.values().contains(value);
   }

   public Object get(Object key) {
      return key == null ? null : this.beanIntrospection.getProperty(key.toString()).map(bp -> bp.get(this.bean)).orElse(null);
   }

   public Object put(String key, Object value) {
      if (key == null) {
         return null;
      } else {
         this.beanIntrospection.getProperty(key).ifPresent(bp -> {
            Class<Object> propertyType = bp.getType();
            if (value != null && !propertyType.isInstance(value)) {
               Optional<?> converted = ConversionService.SHARED.convert(value, propertyType);
               converted.ifPresent(o -> bp.set(this.bean, o));
            } else {
               bp.set(this.bean, value);
            }

         });
         return null;
      }
   }

   public Object remove(Object key) {
      throw new UnsupportedOperationException("Removal is not supported");
   }

   public void putAll(Map<? extends String, ?> m) {
      for(Entry<? extends String, ?> entry : m.entrySet()) {
         this.put((String)entry.getKey(), entry.getValue());
      }

   }

   public void clear() {
      throw new UnsupportedOperationException("Removal is not supported");
   }

   public Set<String> keySet() {
      return CollectionUtils.setOf((T[])this.beanIntrospection.getPropertyNames());
   }

   public Collection<Object> values() {
      return (Collection<Object>)this.keySet().stream().map(this::get).collect(Collectors.toList());
   }

   public Set<Entry<String, Object>> entrySet() {
      return (Set<Entry<String, Object>>)this.keySet().stream().map(key -> new Entry<String, Object>() {
            public String getKey() {
               return key;
            }

            public Object getValue() {
               return BeanIntrospectionMap.this.get(key);
            }

            public Object setValue(Object value) {
               return BeanIntrospectionMap.this.put(key, value);
            }
         }).collect(Collectors.toSet());
   }
}
