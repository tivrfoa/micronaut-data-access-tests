package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.util.Annotations;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;

public final class AnnotationMap implements Annotations {
   protected HashMap<Class<?>, Annotation> _annotations;

   public AnnotationMap() {
   }

   public static AnnotationMap of(Class<?> type, Annotation value) {
      HashMap<Class<?>, Annotation> ann = new HashMap(4);
      ann.put(type, value);
      return new AnnotationMap(ann);
   }

   AnnotationMap(HashMap<Class<?>, Annotation> a) {
      this._annotations = a;
   }

   @Override
   public <A extends Annotation> A get(Class<A> cls) {
      return (A)(this._annotations == null ? null : this._annotations.get(cls));
   }

   @Override
   public boolean has(Class<?> cls) {
      return this._annotations == null ? false : this._annotations.containsKey(cls);
   }

   @Override
   public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) {
      if (this._annotations != null) {
         int i = 0;

         for(int end = annoClasses.length; i < end; ++i) {
            if (this._annotations.containsKey(annoClasses[i])) {
               return true;
            }
         }
      }

      return false;
   }

   public Iterable<Annotation> annotations() {
      return (Iterable<Annotation>)(this._annotations != null && this._annotations.size() != 0 ? this._annotations.values() : Collections.emptyList());
   }

   public static AnnotationMap merge(AnnotationMap primary, AnnotationMap secondary) {
      if (primary == null || primary._annotations == null || primary._annotations.isEmpty()) {
         return secondary;
      } else if (secondary != null && secondary._annotations != null && !secondary._annotations.isEmpty()) {
         HashMap<Class<?>, Annotation> annotations = new HashMap();

         for(Annotation ann : secondary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
         }

         for(Annotation ann : primary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
         }

         return new AnnotationMap(annotations);
      } else {
         return primary;
      }
   }

   @Override
   public int size() {
      return this._annotations == null ? 0 : this._annotations.size();
   }

   public boolean addIfNotPresent(Annotation ann) {
      if (this._annotations != null && this._annotations.containsKey(ann.annotationType())) {
         return false;
      } else {
         this._add(ann);
         return true;
      }
   }

   public boolean add(Annotation ann) {
      return this._add(ann);
   }

   public String toString() {
      return this._annotations == null ? "[null]" : this._annotations.toString();
   }

   protected final boolean _add(Annotation ann) {
      if (this._annotations == null) {
         this._annotations = new HashMap();
      }

      Annotation previous = (Annotation)this._annotations.put(ann.annotationType(), ann);
      return previous == null || !previous.equals(ann);
   }
}
