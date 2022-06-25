package io.micronaut.core.reflect;

import io.micronaut.core.util.ArrayUtils;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class GenericTypeUtils {
   public static Optional<Class> resolveGenericTypeArgument(Field field) {
      Type genericType = field != null ? field.getGenericType() : null;
      if (genericType instanceof ParameterizedType) {
         Type[] typeArguments = ((ParameterizedType)genericType).getActualTypeArguments();
         if (typeArguments.length > 0) {
            Type typeArg = typeArguments[0];
            return resolveParameterizedTypeArgument(typeArg);
         }
      }

      return Optional.empty();
   }

   public static Class[] resolveInterfaceTypeArguments(Class<?> type, Class<?> interfaceType) {
      Optional<Type> resolvedType = getAllGenericInterfaces(type).stream().filter(t -> {
         if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            return pt.getRawType() == interfaceType;
         } else {
            return false;
         }
      }).findFirst();
      return (Class[])resolvedType.map(GenericTypeUtils::resolveTypeArguments).orElse(ReflectionUtils.EMPTY_CLASS_ARRAY);
   }

   public static Class[] resolveSuperTypeGenericArguments(Class<?> type, Class<?> superTypeToResolve) {
      Type supertype = type.getGenericSuperclass();

      for(Class<?> superclass = type.getSuperclass(); superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
         if (supertype instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)supertype;
            if (pt.getRawType() == superTypeToResolve) {
               return resolveTypeArguments(supertype);
            }
         }

         supertype = superclass.getGenericSuperclass();
      }

      return ReflectionUtils.EMPTY_CLASS_ARRAY;
   }

   public static Optional<Class> resolveSuperGenericTypeArgument(Class type) {
      try {
         Type genericSuperclass = type.getGenericSuperclass();
         return genericSuperclass instanceof ParameterizedType ? resolveSingleTypeArgument(genericSuperclass) : Optional.empty();
      } catch (NoClassDefFoundError var2) {
         return Optional.empty();
      }
   }

   public static Class[] resolveTypeArguments(Type genericType) {
      Class[] typeArguments = ReflectionUtils.EMPTY_CLASS_ARRAY;
      if (genericType instanceof ParameterizedType) {
         ParameterizedType pt = (ParameterizedType)genericType;
         typeArguments = resolveParameterizedType(pt);
      }

      return typeArguments;
   }

   public static Optional<Class> resolveInterfaceTypeArgument(Class type, Class interfaceType) {
      Type[] genericInterfaces = type.getGenericInterfaces();

      for(Type genericInterface : genericInterfaces) {
         if (genericInterface instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericInterface;
            if (pt.getRawType() == interfaceType) {
               return resolveSingleTypeArgument(genericInterface);
            }
         }
      }

      Class superClass = type.getSuperclass();
      return superClass != null && superClass != Object.class ? resolveInterfaceTypeArgument(superClass, interfaceType) : Optional.empty();
   }

   private static Optional<Class> resolveSingleTypeArgument(Type genericType) {
      if (genericType instanceof ParameterizedType) {
         ParameterizedType pt = (ParameterizedType)genericType;
         Type[] actualTypeArguments = pt.getActualTypeArguments();
         if (actualTypeArguments.length == 1) {
            Type actualTypeArgument = actualTypeArguments[0];
            return resolveParameterizedTypeArgument(actualTypeArgument);
         }
      }

      return Optional.empty();
   }

   private static Optional<Class> resolveParameterizedTypeArgument(Type actualTypeArgument) {
      if (actualTypeArgument instanceof Class) {
         return Optional.of((Class)actualTypeArgument);
      } else {
         if (actualTypeArgument instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)actualTypeArgument;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class) {
               return Optional.of((Class)rawType);
            }
         }

         return Optional.empty();
      }
   }

   private static Set<Type> getAllGenericInterfaces(Class<?> aClass) {
      Set<Type> interfaces = new LinkedHashSet();
      return populateInterfaces(aClass, interfaces);
   }

   private static Set<Type> populateInterfaces(Class<?> aClass, Set<Type> interfaces) {
      Type[] theInterfaces = aClass.getGenericInterfaces();
      interfaces.addAll(Arrays.asList(theInterfaces));

      for(Type theInterface : theInterfaces) {
         if (theInterface instanceof Class) {
            Class<?> i = (Class)theInterface;
            if (ArrayUtils.isNotEmpty(i.getGenericInterfaces())) {
               populateInterfaces(i, interfaces);
            }
         }
      }

      if (!aClass.isInterface()) {
         for(Class<?> superclass = aClass.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            populateInterfaces(superclass, interfaces);
         }
      }

      return interfaces;
   }

   private static Class[] resolveParameterizedType(ParameterizedType pt) {
      Class[] typeArguments = ReflectionUtils.EMPTY_CLASS_ARRAY;
      Type[] actualTypeArguments = pt.getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
         typeArguments = new Class[actualTypeArguments.length];

         for(int i = 0; i < actualTypeArguments.length; ++i) {
            Type actualTypeArgument = actualTypeArguments[i];
            Optional<Class> opt = resolveParameterizedTypeArgument(actualTypeArgument);
            if (!opt.isPresent()) {
               typeArguments = ReflectionUtils.EMPTY_CLASS_ARRAY;
               break;
            }

            typeArguments[i] = (Class)opt.get();
         }
      }

      return typeArguments;
   }
}
