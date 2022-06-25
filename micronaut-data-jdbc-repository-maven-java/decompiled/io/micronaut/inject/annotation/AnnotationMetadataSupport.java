package io.micronaut.inject.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Provided;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Type;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueProvider;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.Indexes;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;

@Internal
public final class AnnotationMetadataSupport {
   private static final Map<String, Map<String, Object>> ANNOTATION_DEFAULTS = new ConcurrentHashMap(20);
   private static final Map<String, String> REPEATABLE_ANNOTATIONS = new ConcurrentHashMap(20);
   private static final Map<Class<? extends Annotation>, Optional<Constructor<InvocationHandler>>> ANNOTATION_PROXY_CACHE = new ConcurrentHashMap(20);
   private static final Map<String, Class<? extends Annotation>> ANNOTATION_TYPES = new ConcurrentHashMap(20);

   @Internal
   public static List<Entry<Class<? extends Annotation>, Class<? extends Annotation>>> getCoreRepeatableAnnotations() {
      return Arrays.asList(
         new SimpleEntry(Indexed.class, Indexes.class),
         new SimpleEntry(Requires.class, Requirements.class),
         new SimpleEntry(AliasFor.class, Aliases.class),
         new SimpleEntry(Property.class, PropertySource.class)
      );
   }

   public static Map<String, Object> getDefaultValues(String annotation) {
      return (Map<String, Object>)ANNOTATION_DEFAULTS.getOrDefault(annotation, Collections.emptyMap());
   }

   @Internal
   public static String getRepeatableAnnotation(String annotation) {
      return (String)REPEATABLE_ANNOTATIONS.get(annotation);
   }

   static Optional<Class<? extends Annotation>> getAnnotationType(String name) {
      return getAnnotationType(name, AnnotationMetadataSupport.class.getClassLoader());
   }

   static Optional<Class<? extends Annotation>> getAnnotationType(String name, ClassLoader classLoader) {
      Class<? extends Annotation> type = (Class)ANNOTATION_TYPES.get(name);
      if (type != null) {
         return Optional.of(type);
      } else {
         Optional<Class> aClass = ClassUtils.forName(name, classLoader);
         return aClass.flatMap(aClass1 -> {
            if (Annotation.class.isAssignableFrom(aClass1)) {
               ANNOTATION_TYPES.put(name, aClass1);
               return Optional.of(aClass1);
            } else {
               return Optional.empty();
            }
         });
      }
   }

   static Optional<Class<? extends Annotation>> getRegisteredAnnotationType(String name) {
      Class<? extends Annotation> type = (Class)ANNOTATION_TYPES.get(name);
      return type != null ? Optional.of(type) : Optional.empty();
   }

   static Map<String, Object> getDefaultValues(Class<? extends Annotation> annotation) {
      return getDefaultValues(annotation.getName());
   }

   static boolean hasDefaultValues(String annotation) {
      return ANNOTATION_DEFAULTS.containsKey(annotation);
   }

   static void registerDefaultValues(String annotation, Map<String, Object> defaultValues) {
      if (StringUtils.isNotEmpty(annotation)) {
         ANNOTATION_DEFAULTS.put(annotation, defaultValues);
      }

   }

   static void registerDefaultValues(AnnotationClassValue<?> annotation, Map<String, Object> defaultValues) {
      if (defaultValues != null) {
         registerDefaultValues(annotation.getName(), defaultValues);
      }

      registerAnnotationType(annotation);
   }

   static void registerAnnotationType(AnnotationClassValue<?> annotationClassValue) {
      String name = annotationClassValue.getName();
      if (!ANNOTATION_TYPES.containsKey(name)) {
         annotationClassValue.getType().ifPresent(aClass -> {
            if (Annotation.class.isAssignableFrom(aClass)) {
               ANNOTATION_TYPES.put(name, aClass);
            }

         });
      }

   }

   @Internal
   static void registerRepeatableAnnotations(Map<String, String> repeatableAnnotations) {
      REPEATABLE_ANNOTATIONS.putAll(repeatableAnnotations);
   }

   @Internal
   static void removeCoreRepeatableAnnotations(@NotNull Map<String, String> repeatableAnnotations) {
      for(Entry<Class<? extends Annotation>, Class<? extends Annotation>> e : getCoreRepeatableAnnotations()) {
         repeatableAnnotations.remove(((Class)e.getKey()).getName());
      }

   }

   static Optional<Constructor<InvocationHandler>> getProxyClass(Class<? extends Annotation> annotation) {
      return (Optional<Constructor<InvocationHandler>>)ANNOTATION_PROXY_CACHE.computeIfAbsent(annotation, aClass -> {
         Class proxyClass = Proxy.getProxyClass(annotation.getClassLoader(), annotation, AnnotationValueProvider.class);
         return ReflectionUtils.findConstructor(proxyClass, InvocationHandler.class);
      });
   }

   static <T extends Annotation> T buildAnnotation(Class<T> annotationClass, @Nullable AnnotationValue<T> annotationValue) {
      Optional<Constructor<InvocationHandler>> proxyClass = getProxyClass(annotationClass);
      if (proxyClass.isPresent()) {
         Map<String, Object> values = new HashMap(getDefaultValues(annotationClass));
         if (annotationValue != null) {
            Map<CharSequence, Object> annotationValues = annotationValue.getValues();
            annotationValues.forEach((key, o) -> values.put(key.toString(), o));
         }

         int hashCode = AnnotationUtil.calculateHashCode(values);
         Optional instantiated = InstantiationUtils.tryInstantiate(
            (Constructor<T>)proxyClass.get(), new AnnotationMetadataSupport.AnnotationProxyHandler<>(hashCode, annotationClass, annotationValue)
         );
         if (instantiated.isPresent()) {
            return (T)instantiated.get();
         }
      }

      throw new AnnotationMetadataException("Failed to build annotation for type: " + annotationClass.getName());
   }

   static {
      Arrays.asList(
            Nullable.class,
            NonNull.class,
            PreDestroy.class,
            PostConstruct.class,
            Named.class,
            Singleton.class,
            Inject.class,
            Qualifier.class,
            Scope.class,
            Prototype.class,
            Executable.class,
            Bean.class,
            Primary.class,
            Value.class,
            Property.class,
            Provided.class,
            Requires.class,
            Secondary.class,
            Type.class,
            Context.class,
            EachBean.class,
            EachProperty.class,
            Configuration.class,
            ConfigurationProperties.class,
            ConfigurationBuilder.class,
            Introspected.class,
            Parameter.class,
            Replaces.class,
            Requirements.class,
            Factory.class
         )
         .forEach(ann -> {
            Class var10000 = (Class)ANNOTATION_TYPES.put(ann.getName(), ann);
         });

      for(Entry<Class<? extends Annotation>, Class<? extends Annotation>> e : getCoreRepeatableAnnotations()) {
         REPEATABLE_ANNOTATIONS.put(((Class)e.getKey()).getName(), ((Class)e.getValue()).getName());
      }

   }

   private static class AnnotationProxyHandler<A extends Annotation> implements InvocationHandler, AnnotationValueProvider<A> {
      private final int hashCode;
      private final Class<A> annotationClass;
      private final AnnotationValue<A> annotationValue;

      AnnotationProxyHandler(int hashCode, Class<A> annotationClass, @Nullable AnnotationValue<A> annotationValue) {
         this.hashCode = hashCode;
         this.annotationClass = annotationClass;
         this.annotationValue = annotationValue;
      }

      public int hashCode() {
         return this.hashCode;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (!this.annotationClass.isInstance(obj)) {
            return false;
         } else {
            Annotation other = (Annotation)this.annotationClass.cast(obj);
            AnnotationValue<?> otherValues = this.getAnnotationValues(other);
            if (this.annotationValue == null && otherValues == null) {
               return true;
            } else {
               return this.annotationValue != null && otherValues != null ? this.annotationValue.equals(otherValues) : false;
            }
         }
      }

      private AnnotationValue<?> getAnnotationValues(Annotation other) {
         return other instanceof AnnotationMetadataSupport.AnnotationProxyHandler
            ? ((AnnotationMetadataSupport.AnnotationProxyHandler)other).annotationValue
            : null;
      }

      public Object invoke(Object proxy, Method method, Object[] args) {
         String name = method.getName();
         if ((args == null || args.length == 0) && "hashCode".equals(name)) {
            return this.hashCode;
         } else if (args != null && args.length == 1 && "equals".equals(name)) {
            return this.equals(args[0]);
         } else if ("annotationType".equals(name)) {
            return this.annotationClass;
         } else if (method.getReturnType() == AnnotationValue.class) {
            return this.annotationValue;
         } else {
            return this.annotationValue != null && this.annotationValue.contains(name)
               ? this.annotationValue.getRequiredValue(name, method.getReturnType())
               : method.getDefaultValue();
         }
      }

      @NonNull
      @Override
      public AnnotationValue<A> annotationValue() {
         return this.annotationValue != null ? this.annotationValue : new AnnotationValue<>(this.annotationClass.getName());
      }
   }
}
