package io.micronaut.core.reflect;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantiationUtils {
   public static Optional<?> tryInstantiate(String name, ClassLoader classLoader) {
      try {
         return ClassUtils.forName(name, classLoader).flatMap(InstantiationUtils::tryInstantiate);
      } catch (Throwable var4) {
         Logger log = LoggerFactory.getLogger(InstantiationUtils.class);
         if (log.isDebugEnabled()) {
            log.debug("Tried, but could not instantiate type: " + name, var4);
         }

         return Optional.empty();
      }
   }

   @NonNull
   public static <T> Optional<T> tryInstantiate(@NonNull Class<T> type, Map propertiesMap, ConversionContext context) {
      ArgumentUtils.requireNonNull("type", (T)type);
      if (propertiesMap.isEmpty()) {
         return tryInstantiate(type);
      } else {
         Supplier<T> reflectionFallback = () -> {
            Logger log = LoggerFactory.getLogger(InstantiationUtils.class);
            if (log.isDebugEnabled()) {
               log.debug("Tried, but could not instantiate type: " + type);
            }

            return null;
         };
         T result = (T)BeanIntrospector.SHARED
            .findIntrospection(type)
            .map(
               introspection -> {
                  Argument[] constructorArguments = introspection.getConstructorArguments();
                  List<Object> arguments = new ArrayList(constructorArguments.length);
      
                  try {
                     T instance;
                     if (constructorArguments.length > 0) {
                        Map bindMap = new LinkedHashMap(propertiesMap.size());
      
                        for(Entry<?, ?> entry : propertiesMap.entrySet()) {
                           Object key = entry.getKey();
                           bindMap.put(NameUtils.decapitalize(NameUtils.dehyphenate(key.toString())), entry.getValue());
                        }
      
                        for(Argument<?> argument : constructorArguments) {
                           if (bindMap.containsKey(argument.getName())) {
                              Object converted = ConversionService.SHARED
                                 .convert(bindMap.get(argument.getName()), argument.getType(), ConversionContext.of(argument))
                                 .orElseThrow(
                                    () -> new ConversionErrorException(
                                          argument,
                                          (ConversionError)context.getLastError()
                                             .orElse(
                                                (ConversionError)() -> new IllegalArgumentException(
                                                      "Value [" + bindMap.get(argument.getName()) + "] cannot be converted to type : " + argument.getType()
                                                   )
                                             )
                                       )
                                 );
                              arguments.add(converted);
                           } else if (argument.isDeclaredNullable()) {
                              arguments.add(null);
                           } else {
                              context.reject(
                                 new ConversionErrorException(
                                    argument, (ConversionError)(() -> new IllegalArgumentException("No Value found for argument " + argument.getName()))
                                 )
                              );
                           }
                        }
      
                        instance = (T)introspection.instantiate(arguments.toArray());
                     } else {
                        instance = (T)introspection.instantiate();
                     }
      
                     return instance;
                  } catch (InstantiationException var14) {
                     return reflectionFallback.get();
                  }
               }
            )
            .orElseGet(reflectionFallback);
         return Optional.ofNullable(result);
      }
   }

   @NonNull
   public static <T> Optional<T> tryInstantiate(@NonNull Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      Supplier<T> reflectionFallback = () -> {
         Logger logger = ClassUtils.REFLECTION_LOGGER;
         if (logger.isDebugEnabled()) {
            logger.debug("Cannot instantiate type [{}] without reflection. Attempting reflective instantiation", type);
         }

         try {
            T bean = (T)type.getDeclaredConstructor().newInstance();
            return type.isInstance(bean) ? bean : null;
         } catch (Throwable var6) {
            try {
               Constructor<T> defaultConstructor = type.getDeclaredConstructor();
               defaultConstructor.setAccessible(true);
               return tryInstantiate(defaultConstructor).orElse(null);
            } catch (Throwable var5) {
               Logger log = LoggerFactory.getLogger(InstantiationUtils.class);
               if (log.isDebugEnabled()) {
                  log.debug("Tried, but could not instantiate type: " + type, var6);
               }

               return null;
            }
         }
      };
      T result = (T)BeanIntrospector.SHARED.findIntrospection(type).map(introspection -> {
         try {
            return introspection.instantiate();
         } catch (InstantiationException var3) {
            return reflectionFallback.get();
         }
      }).orElseGet(reflectionFallback);
      return Optional.ofNullable(result);
   }

   @NonNull
   public static <T> Optional<T> tryInstantiate(@NonNull Constructor<T> type, Object... args) {
      try {
         return Optional.of(type.newInstance(args));
      } catch (Throwable var4) {
         Logger log = ClassUtils.REFLECTION_LOGGER;
         if (log.isDebugEnabled()) {
            log.debug("Tried, but could not instantiate type: " + type, var4);
         }

         return Optional.empty();
      }
   }

   public static <T> T instantiate(Class<T> type) {
      try {
         return (T)BeanIntrospector.SHARED.findIntrospection(type).map(BeanIntrospection::instantiate).orElseGet(() -> {
            try {
               Logger log = ClassUtils.REFLECTION_LOGGER;
               if (log.isDebugEnabled()) {
                  log.debug("Reflectively instantiating type: " + type);
               }

               return type.getDeclaredConstructor().newInstance();
            } catch (Throwable var2x) {
               throw new InstantiationException("Could not instantiate type [" + type.getName() + "]: " + var2x.getMessage(), var2x);
            }
         });
      } catch (Throwable var2) {
         throw new InstantiationException("Could not instantiate type [" + type.getName() + "]: " + var2.getMessage(), var2);
      }
   }

   public static <T> T instantiate(Class<T> type, Class<?>[] argTypes, Object... args) {
      try {
         return (T)BeanIntrospector.SHARED.findIntrospection(type).map(bi -> bi.instantiate(args)).orElseGet(() -> {
            try {
               Logger log = ClassUtils.REFLECTION_LOGGER;
               if (log.isDebugEnabled()) {
                  log.debug("Reflectively instantiating type: " + type);
               }

               Constructor<T> declaredConstructor = type.getDeclaredConstructor(argTypes);
               declaredConstructor.setAccessible(true);
               return declaredConstructor.newInstance(args);
            } catch (Throwable var5) {
               throw new InstantiationException("Could not instantiate type [" + type.getName() + "]: " + var5.getMessage(), var5);
            }
         });
      } catch (Throwable var4) {
         throw new InstantiationException("Could not instantiate type [" + type.getName() + "]: " + var4.getMessage(), var4);
      }
   }

   public static Object instantiate(String type, ClassLoader classLoader) {
      try {
         return ClassUtils.forName(type, classLoader)
            .flatMap(InstantiationUtils::tryInstantiate)
            .orElseThrow(() -> new InstantiationException("No class found for name: " + type));
      } catch (Throwable var3) {
         throw new InstantiationException("Could not instantiate type [" + type + "]: " + var3.getMessage(), var3);
      }
   }

   public static <T> T instantiate(String type, Class<T> requiredType) {
      try {
         return (T)ClassUtils.forName(type, requiredType.getClassLoader())
            .flatMap(aClass -> requiredType != aClass && !requiredType.isAssignableFrom(aClass) ? Optional.empty() : tryInstantiate(aClass))
            .orElseThrow(() -> new InstantiationException("No compatible class found for name: " + type));
      } catch (Throwable var3) {
         throw new InstantiationException("Could not instantiate type [" + type + "]: " + var3.getMessage(), var3);
      }
   }
}
