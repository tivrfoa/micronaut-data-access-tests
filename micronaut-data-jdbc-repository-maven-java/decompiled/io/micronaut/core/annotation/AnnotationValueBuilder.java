package io.micronaut.core.annotation;

import io.micronaut.core.reflect.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AnnotationValueBuilder<T extends Annotation> {
   private final String annotationName;
   private final Map<CharSequence, Object> values = new LinkedHashMap(5);
   private final RetentionPolicy retentionPolicy;
   private final List<AnnotationValue<?>> stereotypes = new ArrayList();
   private final Map<String, Object> defaultValues = new LinkedHashMap();

   @Internal
   AnnotationValueBuilder(String annotationName) {
      this(annotationName, RetentionPolicy.RUNTIME);
   }

   @Internal
   AnnotationValueBuilder(String annotationName, RetentionPolicy retentionPolicy) {
      this.annotationName = annotationName;
      this.retentionPolicy = retentionPolicy != null ? retentionPolicy : RetentionPolicy.RUNTIME;
   }

   @Internal
   AnnotationValueBuilder(Class<?> annotation) {
      this(annotation.getName());
   }

   @Internal
   AnnotationValueBuilder(AnnotationValue<T> value, RetentionPolicy retentionPolicy) {
      this.annotationName = value.getAnnotationName();
      this.values.putAll(value.getValues());
      this.retentionPolicy = retentionPolicy != null ? retentionPolicy : RetentionPolicy.RUNTIME;
   }

   @NonNull
   public AnnotationValue<T> build() {
      return new AnnotationValue<>(this.annotationName, this.values, this.defaultValues, this.retentionPolicy, this.stereotypes);
   }

   @NonNull
   public AnnotationValueBuilder<T> stereotype(AnnotationValue<?> annotation) {
      if (annotation != null) {
         this.stereotypes.add(annotation);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> defaultValues(Map<String, Object> defaultValues) {
      if (defaultValues != null) {
         this.defaultValues.putAll(defaultValues);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> value(int i) {
      return this.member("value", i);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable int... ints) {
      return this.member("value", ints);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(long i) {
      return this.member("value", i);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable long... longs) {
      return this.member("value", longs);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(@Nullable String str) {
      return this.member("value", str);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable String... strings) {
      return this.member("value", strings);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(boolean bool) {
      return this.member("value", bool);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(char character) {
      return this.member("value", character);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(double number) {
      return this.member("value", number);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(float f) {
      return this.member("value", f);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(@Nullable Enum<?> enumObj) {
      return this.member("value", enumObj);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable Enum<?>... enumObjs) {
      return this.member("value", enumObjs);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(@Nullable Class<?> type) {
      return this.member("value", type);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable Class<?>... types) {
      return this.member("value", types);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable AnnotationClassValue<?>... types) {
      return this.member("value", types);
   }

   @NonNull
   public AnnotationValueBuilder<T> value(@Nullable AnnotationValue<?> annotation) {
      return this.member("value", annotation);
   }

   @NonNull
   public AnnotationValueBuilder<T> values(@Nullable AnnotationValue<?>... annotations) {
      return this.member("value", annotations);
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, int i) {
      this.values.put(name, i);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, byte b) {
      this.values.put(name, b);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, char c) {
      this.values.put(name, c);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, char... chars) {
      this.values.put(name, chars);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, double d) {
      this.values.put(name, d);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, double... doubles) {
      this.values.put(name, doubles);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, float f) {
      this.values.put(name, f);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, float... floats) {
      this.values.put(name, floats);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable int... ints) {
      if (ints != null) {
         this.values.put(name, ints);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable byte... bytes) {
      if (bytes != null) {
         this.values.put(name, bytes);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, long i) {
      this.values.put(name, i);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, short i) {
      this.values.put(name, i);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, short... shorts) {
      this.values.put(name, shorts);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable long... longs) {
      if (longs != null) {
         this.values.put(name, longs);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable String str) {
      if (str != null) {
         this.values.put(name, str);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable String... strings) {
      if (strings != null) {
         this.values.put(name, strings);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, boolean bool) {
      this.values.put(name, bool);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, boolean... booleans) {
      this.values.put(name, booleans);
      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable Enum<?> enumObj) {
      if (enumObj != null) {
         this.values.put(name, enumObj);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable Enum<?>... enumObjs) {
      if (enumObjs != null) {
         this.values.put(name, enumObjs);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable Class<?> type) {
      if (type != null) {
         this.values.put(name, new AnnotationClassValue(type));
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable Class<?>... types) {
      if (types != null) {
         AnnotationClassValue<?>[] classValues = new AnnotationClassValue[types.length];

         for(int i = 0; i < types.length; ++i) {
            Class<?> type = types[i];
            classValues[i] = new AnnotationClassValue(type);
         }

         this.values.put(name, classValues);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable AnnotationValue<?> annotation) {
      if (annotation != null) {
         this.values.put(name, annotation);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable AnnotationValue<?>... annotations) {
      if (annotations != null) {
         this.values.put(name, annotations);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> member(@NonNull String name, @Nullable AnnotationClassValue<?>... classValues) {
      if (classValues != null) {
         this.values.put(name, classValues);
      }

      return this;
   }

   @NonNull
   public AnnotationValueBuilder<T> members(@Nullable Map<CharSequence, Object> members) {
      if (members != null) {
         for(Entry<CharSequence, Object> entry : members.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
               Class clazz = value.getClass();
               boolean isArray = clazz.isArray();
               if (isArray) {
                  clazz = clazz.getComponentType();
               }

               boolean isValid = !clazz.isArray()
                  && (
                     clazz.isPrimitive()
                        || ReflectionUtils.getPrimitiveType(clazz).isPrimitive() && !isArray
                        || clazz.isEnum()
                        || clazz == Class.class
                        || clazz == String.class
                        || clazz == Enum.class
                        || clazz == AnnotationClassValue.class
                        || clazz == AnnotationValue.class
                  );
               if (!isValid) {
                  throw new IllegalArgumentException(
                     "The member named ["
                        + ((CharSequence)entry.getKey()).toString()
                        + "] with type ["
                        + value.getClass().getName()
                        + "] is not a valid member type"
                  );
               }
            }
         }

         for(Entry<CharSequence, Object> entry : members.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
               Class<?> clazz = value.getClass();
               String key = ((CharSequence)entry.getKey()).toString();
               if (clazz == Class.class) {
                  this.member(key, (Class<?>)value);
               } else if (clazz.isArray() && clazz.getComponentType() == Class.class) {
                  this.member(key, (Class[])value);
               } else {
                  this.values.put(key, value);
               }
            }
         }
      }

      return this;
   }
}
