package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Internal
abstract class AbstractAnnotationMetadata implements AnnotationMetadata {
   protected final Map<String, Annotation> annotationMap;
   protected final Map<String, Annotation> declaredAnnotationMap;
   private Annotation[] allAnnotationArray;
   private Annotation[] declaredAnnotationArray;

   protected AbstractAnnotationMetadata(
      @Nullable Map<String, Map<CharSequence, Object>> declaredAnnotations, @Nullable Map<String, Map<CharSequence, Object>> allAnnotations
   ) {
      this.declaredAnnotationMap = declaredAnnotations != null ? new ConcurrentHashMap(declaredAnnotations.size()) : null;
      this.annotationMap = allAnnotations != null ? new ConcurrentHashMap(allAnnotations.size()) : null;
   }

   protected AbstractAnnotationMetadata() {
      this.annotationMap = new ConcurrentHashMap(2);
      this.declaredAnnotationMap = new ConcurrentHashMap(2);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      if (this.annotationMap == null) {
         return null;
      } else if (!this.hasAnnotation(annotationClass) && !this.hasStereotype(annotationClass)) {
         return null;
      } else {
         String annotationName = annotationClass.getName();
         return (T)this.annotationMap.computeIfAbsent(annotationName, s -> {
            AnnotationValue<T> annotationValue = (AnnotationValue)this.findAnnotation(annotationClass).orElse(null);
            return AnnotationMetadataSupport.buildAnnotation(annotationClass, annotationValue);
         });
      }
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      ArgumentUtils.requireNonNull("sourceAnnotation", (T)sourceAnnotation);
      if (this.annotationMap == null) {
         return null;
      } else if (!this.hasAnnotation(sourceAnnotation) && !this.hasStereotype(sourceAnnotation)) {
         return null;
      } else {
         String annotationName = annotationClass.getName();
         return (T)this.annotationMap.computeIfAbsent(annotationName, s -> {
            AnnotationValue<T> annotationValue = (AnnotationValue)this.findAnnotation(sourceAnnotation).orElse(null);
            return AnnotationMetadataSupport.buildAnnotation(annotationClass, annotationValue);
         });
      }
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      ArgumentUtils.requireNonNull("sourceAnnotation", (T)sourceAnnotation);
      if (this.declaredAnnotationMap == null) {
         return null;
      } else {
         String annotationName = annotationClass.getName();
         return (T)(!this.hasDeclaredAnnotation(sourceAnnotation) && !this.hasDeclaredStereotype(sourceAnnotation)
            ? null
            : this.declaredAnnotationMap.computeIfAbsent(annotationName, s -> {
               AnnotationValue<T> annotationValue = (AnnotationValue)this.findDeclaredAnnotation(sourceAnnotation).orElse(null);
               return AnnotationMetadataSupport.buildAnnotation(annotationClass, annotationValue);
            }));
      }
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      if (this.declaredAnnotationMap == null) {
         return null;
      } else {
         String annotationName = annotationClass.getName();
         return (T)(!this.hasDeclaredAnnotation(annotationName) && !this.hasDeclaredStereotype(annotationName)
            ? null
            : this.declaredAnnotationMap.computeIfAbsent(annotationName, s -> {
               AnnotationValue<T> annotationValue = (AnnotationValue)this.findDeclaredAnnotation(annotationClass).orElse(null);
               return AnnotationMetadataSupport.buildAnnotation(annotationClass, annotationValue);
            }));
      }
   }

   @NonNull
   @Override
   public Annotation[] synthesizeAll() {
      if (this.annotationMap == null) {
         return AnnotationUtil.ZERO_ANNOTATIONS;
      } else {
         Annotation[] annotations = this.allAnnotationArray;
         if (annotations == null) {
            synchronized(this) {
               annotations = this.allAnnotationArray;
               if (annotations == null) {
                  annotations = this.initializeAnnotations(this.getAnnotationNames());
                  this.allAnnotationArray = annotations;
               }
            }
         }

         return annotations;
      }
   }

   @NonNull
   @Override
   public Annotation[] synthesizeDeclared() {
      if (this.declaredAnnotationMap == null) {
         return AnnotationUtil.ZERO_ANNOTATIONS;
      } else {
         Annotation[] annotations = this.declaredAnnotationArray;
         if (annotations == null) {
            synchronized(this) {
               annotations = this.declaredAnnotationArray;
               if (annotations == null) {
                  annotations = this.initializeAnnotations(this.getDeclaredAnnotationNames());
                  this.declaredAnnotationArray = annotations;
               }
            }
         }

         return annotations;
      }
   }

   protected final void addAnnotationValuesFromData(List results, Map<CharSequence, Object> values) {
      if (values != null) {
         Object v = values.get("value");
         if (v instanceof AnnotationValue[]) {
            AnnotationValue[] avs = (AnnotationValue[])v;

            for(AnnotationValue av : avs) {
               this.addValuesToResults(results, av);
            }
         } else if (v instanceof Collection) {
            for(Object o : (Collection)v) {
               if (o instanceof AnnotationValue) {
                  this.addValuesToResults(results, (AnnotationValue)o);
               }
            }
         }
      }

   }

   protected void addValuesToResults(List<AnnotationValue> results, AnnotationValue values) {
      results.add(values);
   }

   private Annotation[] initializeAnnotations(Set<String> names) {
      if (!CollectionUtils.isNotEmpty(names)) {
         return AnnotationUtil.ZERO_ANNOTATIONS;
      } else {
         List<Annotation> annotations = new ArrayList();

         for(String name : names) {
            Optional<Class> loaded = ClassUtils.forName(name, this.getClass().getClassLoader());
            loaded.ifPresent(aClass -> {
               Annotation ann = this.synthesize(aClass);
               if (ann != null) {
                  annotations.add(ann);
               }

            });
         }

         return (Annotation[])annotations.toArray(new Annotation[0]);
      }
   }
}
