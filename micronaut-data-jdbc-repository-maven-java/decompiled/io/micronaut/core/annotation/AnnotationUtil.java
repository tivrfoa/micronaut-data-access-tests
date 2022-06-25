package io.micronaut.core.annotation;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class AnnotationUtil {
   public static final String KOTLIN_METADATA = "kotlin.Metadata";
   public static final List<String> INTERNAL_ANNOTATION_NAMES = Arrays.asList(
      Retention.class.getName(),
      "javax.annotation.meta.TypeQualifier",
      "javax.annotation.meta.TypeQualifierNickname",
      "kotlin.annotation.Retention",
      Inherited.class.getName(),
      SuppressWarnings.class.getName(),
      Override.class.getName(),
      Repeatable.class.getName(),
      Documented.class.getName(),
      "kotlin.annotation.MustBeDocumented",
      Target.class.getName(),
      "kotlin.annotation.Target",
      "kotlin.Metadata"
   );
   public static final List<String> STEREOTYPE_EXCLUDES = Arrays.asList(
      "javax.annotation", "java.lang.annotation", "io.micronaut.core.annotation", "edu.umd.cs.findbugs.annotations"
   );
   public static final Annotation[] ZERO_ANNOTATIONS = new Annotation[0];
   public static final java.lang.reflect.AnnotatedElement[] ZERO_ANNOTATED_ELEMENTS = new java.lang.reflect.AnnotatedElement[0];
   public static final AnnotationValue<?>[] ZERO_ANNOTATION_VALUES = new AnnotationValue[0];
   public static final java.lang.reflect.AnnotatedElement EMPTY_ANNOTATED_ELEMENT = new java.lang.reflect.AnnotatedElement() {
      public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
         return null;
      }

      public Annotation[] getAnnotations() {
         return AnnotationUtil.ZERO_ANNOTATIONS;
      }

      public Annotation[] getDeclaredAnnotations() {
         return AnnotationUtil.ZERO_ANNOTATIONS;
      }
   };
   public static final String NULLABLE = "javax.annotation.Nullable";
   public static final String NON_NULL = "javax.annotation.Nonnull";
   public static final String ANN_AROUND = "io.micronaut.aop.Around";
   public static final String ANN_AROUND_CONSTRUCT = "io.micronaut.aop.AroundConstruct";
   public static final String ANN_INTRODUCTION = "io.micronaut.aop.Introduction";
   public static final String ANN_INTERCEPTOR_BINDING = "io.micronaut.aop.InterceptorBinding";
   public static final String ANN_INTERCEPTOR_BINDING_QUALIFIER = "io.micronaut.inject.qualifiers.InterceptorBindingQualifier";
   public static final Set<String> ADVICE_STEREOTYPES = CollectionUtils.setOf(
      "io.micronaut.aop.Around", "io.micronaut.aop.AroundConstruct", "io.micronaut.aop.Introduction"
   );
   public static final String ANN_INTERCEPTOR_BINDINGS = "io.micronaut.aop.InterceptorBindingDefinitions";
   public static final String INJECT = "javax.inject.Inject";
   public static final String SCOPE = "javax.inject.Scope";
   public static final String SINGLETON = "javax.inject.Singleton";
   public static final String QUALIFIER = "javax.inject.Qualifier";
   public static final String NAMED = "javax.inject.Named";
   public static final String PRE_DESTROY = "javax.annotation.PreDestroy";
   public static final String POST_CONSTRUCT = "javax.annotation.PostConstruct";
   private static final Map<Integer, List<String>> INTERN_LIST_POOL = new ConcurrentHashMap();
   private static final Map<String, Map<String, Object>> INTERN_MAP_POOL = new ConcurrentHashMap();

   public static List<String> internListOf(Object... objects) {
      if (objects != null && objects.length != 0) {
         Integer hash = Arrays.hashCode(objects);
         return (List<String>)INTERN_LIST_POOL.computeIfAbsent(hash, integer -> StringUtils.internListOf(objects));
      } else {
         return Collections.emptyList();
      }
   }

   public static Map<String, Object> internMapOf(Object... values) {
      if (values != null && values.length != 0) {
         int len = values.length;
         if (len % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values");
         } else {
            return len == 2 ? internMapOf((String)values[0], values[1]) : StringUtils.internMapOf(values);
         }
      } else {
         return Collections.emptyMap();
      }
   }

   public static Map<String, Object> internMapOf(String key, Object value) {
      Objects.requireNonNull(key);
      return value == Collections.EMPTY_MAP
         ? (Map)INTERN_MAP_POOL.computeIfAbsent(key, s -> Collections.singletonMap(s, Collections.emptyMap()))
         : Collections.singletonMap(key, value);
   }

   public static Map<String, Object> mapOf(Object... array) {
      int len = array.length;
      if (len % 2 != 0) {
         throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values");
      } else if (array.length == 0) {
         return Collections.EMPTY_MAP;
      } else {
         int size = len / 2;
         String[] keys = new String[size];
         Object[] values = new Object[size];
         int k = 0;
         int i = 0;

         for(int arrayLength = array.length; i < arrayLength; i += 2) {
            keys[k] = (String)array[i];
            values[k] = array[i + 1];
            ++k;
         }

         return new ImmutableSortedStringsArrayMap(keys, values);
      }
   }

   public static Map<String, Object> mapOf(String key1, Object value1) {
      return Collections.singletonMap(key1, value1);
   }

   public static Map<String, Object> mapOf(String key1, Object value1, String key2, Object value2) {
      String[] keys = new String[]{key1, key2};
      Object[] values = new Object[]{value1, value2};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
      String[] keys = new String[]{key1, key2, key3};
      Object[] values = new Object[]{value1, value2, value3};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
      String[] keys = new String[]{key1, key2, key3, key4};
      Object[] values = new Object[]{value1, value2, value3, value4};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5};
      Object[] values = new Object[]{value1, value2, value3, value4, value5};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1,
      Object value1,
      String key2,
      Object value2,
      String key3,
      Object value3,
      String key4,
      Object value4,
      String key5,
      Object value5,
      String key6,
      Object value6
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5, key6};
      Object[] values = new Object[]{value1, value2, value3, value4, value5, value6};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1,
      Object value1,
      String key2,
      Object value2,
      String key3,
      Object value3,
      String key4,
      Object value4,
      String key5,
      Object value5,
      String key6,
      Object value6,
      String key7,
      Object value7
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5, key6, key7};
      Object[] values = new Object[]{value1, value2, value3, value4, value5, value6, value7};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1,
      Object value1,
      String key2,
      Object value2,
      String key3,
      Object value3,
      String key4,
      Object value4,
      String key5,
      Object value5,
      String key6,
      Object value6,
      String key7,
      Object value7,
      String key8,
      Object value8
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5, key6, key7, key8};
      Object[] values = new Object[]{value1, value2, value3, value4, value5, value6, value7, value8};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1,
      Object value1,
      String key2,
      Object value2,
      String key3,
      Object value3,
      String key4,
      Object value4,
      String key5,
      Object value5,
      String key6,
      Object value6,
      String key7,
      Object value7,
      String key8,
      Object value8,
      String key9,
      Object value9
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5, key6, key7, key8, key9};
      Object[] values = new Object[]{value1, value2, value3, value4, value5, value6, value7, value8, value9};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static Map<String, Object> mapOf(
      String key1,
      Object value1,
      String key2,
      Object value2,
      String key3,
      Object value3,
      String key4,
      Object value4,
      String key5,
      Object value5,
      String key6,
      Object value6,
      String key7,
      Object value7,
      String key8,
      Object value8,
      String key9,
      Object value9,
      String key10,
      Object value10
   ) {
      String[] keys = new String[]{key1, key2, key3, key4, key5, key6, key7, key8, key9, key10};
      Object[] values = new Object[]{value1, value2, value3, value4, value5, value6, value7, value8, value9, value10};
      return new ImmutableSortedStringsArrayMap(keys, values);
   }

   public static int calculateHashCode(Map<? extends CharSequence, Object> values) {
      int hashCode = 0;

      for(Entry<? extends CharSequence, Object> member : values.entrySet()) {
         Object value = member.getValue();
         int nameHashCode = ((CharSequence)member.getKey()).hashCode();
         int valueHashCode = !value.getClass().isArray()
            ? value.hashCode()
            : (
               value.getClass() == boolean[].class
                  ? Arrays.hashCode((boolean[])value)
                  : (
                     value.getClass() == byte[].class
                        ? Arrays.hashCode((byte[])value)
                        : (
                           value.getClass() == char[].class
                              ? Arrays.hashCode((char[])value)
                              : (
                                 value.getClass() == double[].class
                                    ? Arrays.hashCode((double[])value)
                                    : (
                                       value.getClass() == float[].class
                                          ? Arrays.hashCode((float[])value)
                                          : (
                                             value.getClass() == int[].class
                                                ? Arrays.hashCode((int[])value)
                                                : (
                                                   value.getClass() == long[].class
                                                      ? Arrays.hashCode((long[])value)
                                                      : (value.getClass() == short[].class ? Arrays.hashCode((short[])value) : Arrays.hashCode(value))
                                                )
                                          )
                                    )
                              )
                        )
                  )
            );
         hashCode += 127 * nameHashCode ^ valueHashCode;
      }

      return hashCode;
   }

   public static boolean areEqual(Object o1, Object o2) {
      return !o1.getClass().isArray()
         ? o1.equals(o2)
         : (
            o1.getClass() == boolean[].class
               ? Arrays.equals((boolean[])o1, (boolean[])o2)
               : (
                  o1.getClass() == byte[].class
                     ? Arrays.equals((byte[])o1, (byte[])o2)
                     : (
                        o1.getClass() == char[].class
                           ? Arrays.equals((char[])o1, (char[])o2)
                           : (
                              o1.getClass() == double[].class
                                 ? Arrays.equals((double[])o1, (double[])o2)
                                 : (
                                    o1.getClass() == float[].class
                                       ? Arrays.equals((float[])o1, (float[])o2)
                                       : (
                                          o1.getClass() == int[].class
                                             ? Arrays.equals((int[])o1, (int[])o2)
                                             : (
                                                o1.getClass() == long[].class
                                                   ? Arrays.equals((long[])o1, (long[])o2)
                                                   : (o1.getClass() == short[].class ? Arrays.equals((short[])o1, (short[])o2) : Arrays.equals(o1, o2))
                                             )
                                       )
                                 )
                           )
                     )
               )
         );
   }
}
