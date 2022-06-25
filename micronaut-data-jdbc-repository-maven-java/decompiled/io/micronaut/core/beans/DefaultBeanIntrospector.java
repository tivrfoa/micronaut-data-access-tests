package io.micronaut.core.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.ArgumentUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;

class DefaultBeanIntrospector implements BeanIntrospector {
   private static final Logger LOG = ClassUtils.getLogger(DefaultBeanIntrospector.class);
   private Map<String, BeanIntrospectionReference<Object>> introspectionMap;
   private final ClassLoader classLoader;

   DefaultBeanIntrospector() {
      this.classLoader = DefaultBeanIntrospector.class.getClassLoader();
   }

   DefaultBeanIntrospector(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   @NonNull
   @Override
   public Collection<BeanIntrospection<Object>> findIntrospections(@NonNull Predicate<? super BeanIntrospectionReference<?>> filter) {
      ArgumentUtils.requireNonNull("filter", filter);
      return (Collection<BeanIntrospection<Object>>)this.getIntrospections()
         .values()
         .stream()
         .filter(filter)
         .map(BeanIntrospectionReference::load)
         .collect(Collectors.toList());
   }

   @NonNull
   @Override
   public Collection<Class<?>> findIntrospectedTypes(@NonNull Predicate<? super BeanIntrospectionReference<?>> filter) {
      ArgumentUtils.requireNonNull("filter", filter);
      return (Collection<Class<?>>)this.getIntrospections()
         .values()
         .stream()
         .filter(filter)
         .map(BeanIntrospectionReference::getBeanType)
         .collect(Collectors.toSet());
   }

   @NonNull
   @Override
   public <T> Optional<BeanIntrospection<T>> findIntrospection(@NonNull Class<T> beanType) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      BeanIntrospectionReference<T> reference = (BeanIntrospectionReference)this.getIntrospections().get(beanType.getName());

      try {
         if (reference != null) {
            return Optional.of(reference).map(ref -> {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Found BeanIntrospection for type: {},", ref.getBeanType());
               }

               return ref.load();
            });
         } else {
            if (LOG.isDebugEnabled()) {
               LOG.debug("No BeanIntrospection found for bean type: {}", beanType);
            }

            return Optional.empty();
         }
      } catch (Throwable var4) {
         throw new IntrospectionException("Error loading BeanIntrospection for type [" + beanType + "]: " + var4.getMessage(), var4);
      }
   }

   private Map<String, BeanIntrospectionReference<Object>> getIntrospections() {
      Map<String, BeanIntrospectionReference<Object>> resolvedIntrospectionMap = this.introspectionMap;
      if (resolvedIntrospectionMap == null) {
         synchronized(this) {
            resolvedIntrospectionMap = this.introspectionMap;
            if (resolvedIntrospectionMap == null) {
               resolvedIntrospectionMap = new HashMap(30);
               SoftServiceLoader<BeanIntrospectionReference<Object>> services = this.loadReferences();
               List<BeanIntrospectionReference<Object>> beanIntrospectionReferences = new ArrayList(300);
               services.collectAll(beanIntrospectionReferences, BeanIntrospectionReference::isPresent);

               for(BeanIntrospectionReference<Object> reference : beanIntrospectionReferences) {
                  resolvedIntrospectionMap.put(reference.getName(), reference);
               }

               this.introspectionMap = resolvedIntrospectionMap;
            }
         }
      }

      return resolvedIntrospectionMap;
   }

   private SoftServiceLoader loadReferences() {
      return SoftServiceLoader.load(BeanIntrospectionReference.class, this.classLoader);
   }
}
