package io.micronaut.core.reflect;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.exception.InvocationException;
import io.micronaut.core.util.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public class ReflectionUtils {
   public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
   private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;
   private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

   public static boolean isSetter(String name, Class[] args) {
      if (!StringUtils.isEmpty(name) && args != null) {
         return args.length != 1 ? false : NameUtils.isSetterName(name);
      } else {
         return false;
      }
   }

   public static Class getWrapperType(Class primitiveType) {
      return primitiveType.isPrimitive() ? (Class)PRIMITIVES_TO_WRAPPERS.get(primitiveType) : primitiveType;
   }

   public static Class getPrimitiveType(Class wrapperType) {
      Class<?> wrapper = (Class)WRAPPER_TO_PRIMITIVE.get(wrapperType);
      return wrapper != null ? wrapper : wrapperType;
   }

   public static Optional<Method> getDeclaredMethod(Class type, String methodName, Class... argTypes) {
      try {
         return Optional.of(type.getDeclaredMethod(methodName, argTypes));
      } catch (NoSuchMethodException var4) {
         return Optional.empty();
      }
   }

   public static Optional<Method> getMethod(Class type, String methodName, Class... argTypes) {
      try {
         return Optional.of(type.getMethod(methodName, argTypes));
      } catch (NoSuchMethodException var4) {
         return findMethod(type, methodName, argTypes);
      }
   }

   public static <T> Optional<Constructor<T>> findConstructor(Class<T> type, Class... argTypes) {
      try {
         return Optional.of(type.getDeclaredConstructor(argTypes));
      } catch (NoSuchMethodException var3) {
         return Optional.empty();
      }
   }

   public static <R, T> R invokeMethod(T instance, Method method, Object... arguments) {
      try {
         return (R)method.invoke(instance, arguments);
      } catch (IllegalAccessException var4) {
         throw new InvocationException("Illegal access invoking method [" + method + "]: " + var4.getMessage(), var4);
      } catch (InvocationTargetException var5) {
         throw new InvocationException("Exception occurred invoking method [" + method + "]: " + var5.getMessage(), var5);
      }
   }

   @Internal
   public static Optional<Method> findMethod(Class type, String name, Class... argumentTypes) {
      for(Class currentType = type; currentType != null; currentType = currentType.getSuperclass()) {
         Method[] methods = currentType.isInterface() ? currentType.getMethods() : currentType.getDeclaredMethods();

         for(Method method : methods) {
            if (name.equals(method.getName()) && Arrays.equals(argumentTypes, method.getParameterTypes())) {
               return Optional.of(method);
            }
         }
      }

      return Optional.empty();
   }

   @Internal
   public static Method getRequiredMethod(Class type, String name, Class... argumentTypes) {
      try {
         return type.getDeclaredMethod(name, argumentTypes);
      } catch (NoSuchMethodException var4) {
         return (Method)findMethod(type, name, argumentTypes).orElseThrow(() -> newNoSuchMethodError(type, name, argumentTypes));
      }
   }

   @Internal
   public static Method getRequiredInternalMethod(Class type, String name, Class... argumentTypes) {
      try {
         return type.getDeclaredMethod(name, argumentTypes);
      } catch (NoSuchMethodException var4) {
         return (Method)findMethod(type, name, argumentTypes).orElseThrow(() -> newNoSuchMethodInternalError(type, name, argumentTypes));
      }
   }

   @Internal
   public static <T> Constructor<T> getRequiredInternalConstructor(Class<T> type, Class... argumentTypes) {
      try {
         return type.getDeclaredConstructor(argumentTypes);
      } catch (NoSuchMethodException var3) {
         throw newNoSuchConstructorInternalError(type, argumentTypes);
      }
   }

   @Internal
   public static Field getRequiredField(Class type, String name) {
      try {
         return type.getDeclaredField(name);
      } catch (NoSuchFieldException var4) {
         Optional<Field> field = findField(type, name);
         return (Field)field.orElseThrow(() -> new NoSuchFieldError("No field '" + name + "' found for type: " + type.getName()));
      }
   }

   @Internal
   public static Optional<Field> findField(Class type, String name) {
      Optional<Field> declaredField = findDeclaredField(type, name);
      if (!declaredField.isPresent()) {
         while((type = type.getSuperclass()) != null) {
            declaredField = findField(type, name);
            if (declaredField.isPresent()) {
               break;
            }
         }
      }

      return declaredField;
   }

   public static Stream<Method> findMethodsByName(Class type, String name) {
      Class currentType = type;

      Set<Method> methodSet;
      for(methodSet = new HashSet(); currentType != null; currentType = currentType.getSuperclass()) {
         Method[] methods = currentType.isInterface() ? currentType.getMethods() : currentType.getDeclaredMethods();

         for(Method method : methods) {
            if (name.equals(method.getName())) {
               methodSet.add(method);
            }
         }
      }

      return methodSet.stream();
   }

   public static Optional<Field> findDeclaredField(Class type, String name) {
      try {
         Field declaredField = type.getDeclaredField(name);
         return Optional.of(declaredField);
      } catch (NoSuchFieldException var3) {
         return Optional.empty();
      }
   }

   public static Set<Class> getAllInterfaces(Class<?> aClass) {
      Set<Class> interfaces = new HashSet();
      return populateInterfaces(aClass, interfaces);
   }

   protected static Set<Class> populateInterfaces(Class<?> aClass, Set<Class> interfaces) {
      Class<?>[] theInterfaces = aClass.getInterfaces();
      interfaces.addAll(Arrays.asList(theInterfaces));

      for(Class<?> theInterface : theInterfaces) {
         populateInterfaces(theInterface, interfaces);
      }

      if (!aClass.isInterface()) {
         for(Class<?> superclass = aClass.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            populateInterfaces(superclass, interfaces);
         }
      }

      return interfaces;
   }

   public static NoSuchMethodError newNoSuchMethodError(Class declaringType, String name, Class[] argumentTypes) {
      Stream<String> stringStream = Arrays.stream(argumentTypes).map(Class::getSimpleName);
      String argsAsText = (String)stringStream.collect(Collectors.joining(","));
      return new NoSuchMethodError(
         "Required method "
            + name
            + "("
            + argsAsText
            + ") not found for class: "
            + declaringType.getName()
            + ". Most likely cause of this error is the method declaration is not annotated with @Executable. Alternatively check that there is not an unsupported or older version of a dependency present on the classpath. Check your classpath, and ensure the incompatible classes are not present and/or recompile classes as necessary."
      );
   }

   private static NoSuchMethodError newNoSuchMethodInternalError(Class declaringType, String name, Class[] argumentTypes) {
      Stream<String> stringStream = Arrays.stream(argumentTypes).map(Class::getSimpleName);
      String argsAsText = (String)stringStream.collect(Collectors.joining(","));
      return new NoSuchMethodError(
         "Micronaut method "
            + declaringType.getName()
            + "."
            + name
            + "("
            + argsAsText
            + ") not found. Most likely reason for this issue is that you are running a newer version of Micronaut with code compiled against an older version. Please recompile the offending classes"
      );
   }

   private static NoSuchMethodError newNoSuchConstructorInternalError(Class declaringType, Class[] argumentTypes) {
      Stream<String> stringStream = Arrays.stream(argumentTypes).map(Class::getSimpleName);
      String argsAsText = (String)stringStream.collect(Collectors.joining(","));
      return new NoSuchMethodError(
         "Micronaut constructor "
            + declaringType.getName()
            + "("
            + argsAsText
            + ") not found. Most likely reason for this issue is that you are running a newer version of Micronaut with code compiled against an older version. Please recompile the offending classes"
      );
   }

   public static void setField(@NonNull Field field, @NonNull Object instance, @Nullable Object value) {
      try {
         ClassUtils.REFLECTION_LOGGER.debug("Reflectively setting field {} to value {} on object {}", field, value, value);
         field.setAccessible(true);
         field.set(instance, value);
      } catch (Throwable var4) {
         throw new InvocationException("Exception occurred setting field [" + field + "]: " + var4.getMessage(), var4);
      }
   }

   static {
      LinkedHashMap<Class<?>, Class<?>> m = new LinkedHashMap();
      m.put(Boolean.TYPE, Boolean.class);
      m.put(Byte.TYPE, Byte.class);
      m.put(Character.TYPE, Character.class);
      m.put(Double.TYPE, Double.class);
      m.put(Float.TYPE, Float.class);
      m.put(Integer.TYPE, Integer.class);
      m.put(Long.TYPE, Long.class);
      m.put(Short.TYPE, Short.class);
      m.put(Void.TYPE, Void.class);
      PRIMITIVES_TO_WRAPPERS = Collections.unmodifiableMap(m);
      m = new LinkedHashMap();
      m.put(Boolean.class, Boolean.TYPE);
      m.put(Byte.class, Byte.TYPE);
      m.put(Character.class, Character.TYPE);
      m.put(Double.class, Double.TYPE);
      m.put(Float.class, Float.TYPE);
      m.put(Integer.class, Integer.TYPE);
      m.put(Long.class, Long.TYPE);
      m.put(Short.class, Short.TYPE);
      m.put(Void.class, Void.TYPE);
      WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(m);
   }
}
