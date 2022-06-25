package io.micronaut.core.reflect;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.optim.StaticOptimizations;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

public class ClassUtils {
   public static final String PROPERTY_MICRONAUT_CLASSLOADER_LOGGING = "micronaut.classloader.logging";
   public static final int EMPTY_OBJECT_ARRAY_HASH_CODE = Arrays.hashCode(ArrayUtils.EMPTY_OBJECT_ARRAY);
   public static final Map<String, Class> COMMON_CLASS_MAP = new HashMap(34);
   public static final Map<String, Class> BASIC_TYPE_MAP = new HashMap(18);
   public static final String CLASS_EXTENSION = ".class";
   public static final Logger REFLECTION_LOGGER = getLogger(ClassUtils.class);
   private static final boolean ENABLE_CLASS_LOADER_LOGGING = Boolean.getBoolean("micronaut.classloader.logging");
   private static final Set<String> MISSING_TYPES = (Set<String>)StaticOptimizations.get(ClassUtils.Optimizations.class)
      .map(ClassUtils.Optimizations::getMissingTypes)
      .orElse(Collections.emptySet());
   private static final Map<String, Class> PRIMITIVE_TYPE_MAP = CollectionUtils.mapOf(
      "int",
      Integer.TYPE,
      "boolean",
      Boolean.TYPE,
      "long",
      Long.TYPE,
      "byte",
      Byte.TYPE,
      "double",
      Double.TYPE,
      "float",
      Float.TYPE,
      "char",
      Character.TYPE,
      "short",
      Short.TYPE,
      "void",
      Void.TYPE
   );
   private static final Map<String, Class> PRIMITIVE_ARRAY_MAP = CollectionUtils.mapOf(
      "int",
      int[].class,
      "boolean",
      boolean[].class,
      "long",
      long[].class,
      "byte",
      byte[].class,
      "double",
      double[].class,
      "float",
      float[].class,
      "char",
      char[].class,
      "short",
      short[].class
   );

   @NonNull
   public static Logger getLogger(@NonNull Class type) {
      return (Logger)(ENABLE_CLASS_LOADER_LOGGING ? LoggerFactory.getLogger(type) : NOPLogger.NOP_LOGGER);
   }

   @NonNull
   public static Optional<Class> arrayTypeForPrimitive(String primitiveType) {
      return primitiveType != null ? Optional.ofNullable(PRIMITIVE_ARRAY_MAP.get(primitiveType)) : Optional.empty();
   }

   public static String pathToClassName(String path) {
      path = path.replace('/', '.');
      if (path.endsWith(".class")) {
         path = path.substring(0, path.length() - ".class".length());
      }

      return path;
   }

   public static boolean isPresent(String name, @Nullable ClassLoader classLoader) {
      return forName(name, classLoader).isPresent();
   }

   public static boolean isJavaLangType(Class type) {
      String typeName = type.getName();
      return isJavaLangType(typeName);
   }

   public static boolean isJavaLangType(String typeName) {
      return COMMON_CLASS_MAP.containsKey(typeName);
   }

   public static boolean isJavaBasicType(@Nullable Class<?> type) {
      if (type == null) {
         return false;
      } else {
         String name = type.getName();
         return isJavaBasicType(name);
      }
   }

   public static boolean isJavaBasicType(@Nullable String name) {
      if (StringUtils.isEmpty(name)) {
         return false;
      } else {
         return isJavaLangType(name) || BASIC_TYPE_MAP.containsKey(name);
      }
   }

   public static Optional<Class> getPrimitiveType(String primitiveType) {
      return Optional.ofNullable(PRIMITIVE_TYPE_MAP.get(primitiveType));
   }

   public static Optional<Class> forName(String name, @Nullable ClassLoader classLoader) {
      try {
         if (MISSING_TYPES.contains(name)) {
            return Optional.empty();
         } else {
            if (classLoader == null) {
               classLoader = Thread.currentThread().getContextClassLoader();
            }

            if (classLoader == null) {
               classLoader = ClassLoader.getSystemClassLoader();
            }

            Optional<Class> commonType = Optional.ofNullable(COMMON_CLASS_MAP.get(name));
            if (commonType.isPresent()) {
               return commonType;
            } else {
               if (REFLECTION_LOGGER.isDebugEnabled()) {
                  REFLECTION_LOGGER.debug("Attempting to dynamically load class {}", name);
               }

               Class<?> type = Class.forName(name, true, classLoader);
               if (REFLECTION_LOGGER.isDebugEnabled()) {
                  REFLECTION_LOGGER.debug("Successfully loaded class {}", name);
               }

               return Optional.of(type);
            }
         }
      } catch (NoClassDefFoundError | ClassNotFoundException var4) {
         if (REFLECTION_LOGGER.isDebugEnabled()) {
            REFLECTION_LOGGER.debug("Class {} is not present", name);
         }

         return Optional.empty();
      }
   }

   public static List<Class> resolveHierarchy(Class<?> type) {
      Class<?> superclass = type.getSuperclass();
      List<Class> hierarchy = new ArrayList();
      List<Class> interfaces = new ArrayList();
      if (superclass != null) {
         hierarchy.add(type);
         populateHierarchyInterfaces(type, interfaces);

         while(superclass != Object.class) {
            if (!hierarchy.contains(superclass)) {
               hierarchy.add(superclass);
            }

            populateHierarchyInterfaces(superclass, interfaces);
            superclass = superclass.getSuperclass();
         }

         hierarchy.addAll(interfaces);
      } else if (type.isInterface()) {
         hierarchy.add(type);
         populateHierarchyInterfaces(type, hierarchy);
      }

      if (type.isArray()) {
         if (!type.getComponentType().isPrimitive()) {
            hierarchy.add(Object[].class);
         }
      } else {
         hierarchy.add(Object.class);
      }

      return hierarchy;
   }

   private static void populateHierarchyInterfaces(Class<?> superclass, List<Class> hierarchy) {
      for(Class<?> aClass : superclass.getInterfaces()) {
         if (!hierarchy.contains(aClass)) {
            hierarchy.add(aClass);
         }

         populateHierarchyInterfaces(aClass, hierarchy);
      }

   }

   static {
      COMMON_CLASS_MAP.put(Boolean.TYPE.getName(), Boolean.TYPE);
      COMMON_CLASS_MAP.put(Byte.TYPE.getName(), Byte.TYPE);
      COMMON_CLASS_MAP.put(Integer.TYPE.getName(), Integer.TYPE);
      COMMON_CLASS_MAP.put(Long.TYPE.getName(), Long.TYPE);
      COMMON_CLASS_MAP.put(Double.TYPE.getName(), Double.TYPE);
      COMMON_CLASS_MAP.put(Float.TYPE.getName(), Float.TYPE);
      COMMON_CLASS_MAP.put(Character.TYPE.getName(), Character.TYPE);
      COMMON_CLASS_MAP.put(Short.TYPE.getName(), Short.TYPE);
      COMMON_CLASS_MAP.put(boolean[].class.getName(), boolean[].class);
      COMMON_CLASS_MAP.put(byte[].class.getName(), byte[].class);
      COMMON_CLASS_MAP.put(int[].class.getName(), int[].class);
      COMMON_CLASS_MAP.put(long[].class.getName(), long[].class);
      COMMON_CLASS_MAP.put(double[].class.getName(), double[].class);
      COMMON_CLASS_MAP.put(float[].class.getName(), float[].class);
      COMMON_CLASS_MAP.put(char[].class.getName(), char[].class);
      COMMON_CLASS_MAP.put(short[].class.getName(), short[].class);
      COMMON_CLASS_MAP.put(Boolean.class.getName(), Boolean.class);
      COMMON_CLASS_MAP.put(Byte.class.getName(), Byte.class);
      COMMON_CLASS_MAP.put(Integer.class.getName(), Integer.class);
      COMMON_CLASS_MAP.put(Long.class.getName(), Long.class);
      COMMON_CLASS_MAP.put(Short.class.getName(), Short.class);
      COMMON_CLASS_MAP.put(Double.class.getName(), Double.class);
      COMMON_CLASS_MAP.put(Float.class.getName(), Float.class);
      COMMON_CLASS_MAP.put(Character.class.getName(), Character.class);
      COMMON_CLASS_MAP.put(String.class.getName(), String.class);
      COMMON_CLASS_MAP.put(CharSequence.class.getName(), CharSequence.class);
      BASIC_TYPE_MAP.put(UUID.class.getName(), UUID.class);
      BASIC_TYPE_MAP.put(BigDecimal.class.getName(), BigDecimal.class);
      BASIC_TYPE_MAP.put(BigInteger.class.getName(), BigInteger.class);
      BASIC_TYPE_MAP.put(URL.class.getName(), URL.class);
      BASIC_TYPE_MAP.put(URI.class.getName(), URI.class);
      BASIC_TYPE_MAP.put(TimeZone.class.getName(), TimeZone.class);
      BASIC_TYPE_MAP.put(Charset.class.getName(), Charset.class);
      BASIC_TYPE_MAP.put(Locale.class.getName(), Locale.class);
      BASIC_TYPE_MAP.put(Duration.class.getName(), Duration.class);
      BASIC_TYPE_MAP.put(Date.class.getName(), Date.class);
      BASIC_TYPE_MAP.put(LocalDate.class.getName(), LocalDate.class);
      BASIC_TYPE_MAP.put(Instant.class.getName(), Instant.class);
      BASIC_TYPE_MAP.put(ZonedDateTime.class.getName(), ZonedDateTime.class);
   }

   @Internal
   public static final class Optimizations {
      private final Set<String> missingTypes;

      public Optimizations(Set<String> missingTypes) {
         this.missingTypes = missingTypes;
      }

      public Set<String> getMissingTypes() {
         return this.missingTypes;
      }
   }
}
