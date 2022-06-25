package org.flywaydb.core.internal.util;

import java.beans.Expression;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.flywaydb.core.api.FlywayException;

public class ClassUtils {
   public static <T> T instantiate(String className, ClassLoader classLoader) {
      try {
         return (T)Class.forName(className, true, classLoader).getDeclaredConstructor().newInstance();
      } catch (Exception var3) {
         throw new FlywayException("Unable to instantiate class " + className + " : " + var3.getMessage(), var3);
      }
   }

   public static <T> T instantiate(String className, ClassLoader classLoader, Object... params) {
      try {
         return (T)new Expression(Class.forName(className, false, classLoader), "new", params).getValue();
      } catch (Exception var4) {
         throw new FlywayException("Unable to instantiate class " + className + " : " + var4.getMessage(), var4);
      }
   }

   public static <T> T instantiate(Class<T> clazz) {
      try {
         return (T)clazz.getDeclaredConstructor().newInstance();
      } catch (Exception var2) {
         throw new FlywayException("Unable to instantiate class " + clazz.getName() + " : " + var2.getMessage(), var2);
      }
   }

   public static <T> List<T> instantiateAll(String[] classes, ClassLoader classLoader) {
      List<T> clazzes = new ArrayList();

      for(String clazz : classes) {
         if (StringUtils.hasLength(clazz)) {
            clazzes.add(instantiate(clazz, classLoader));
         }
      }

      return clazzes;
   }

   public static boolean isPresent(String className, ClassLoader classLoader) {
      try {
         classLoader.loadClass(className);
         return true;
      } catch (Throwable var3) {
         return false;
      }
   }

   public static boolean isImplementationPresent(String serviceName, ClassLoader classLoader) {
      try {
         Class service = classLoader.loadClass(serviceName);
         return ServiceLoader.load(service).iterator().hasNext();
      } catch (Throwable var3) {
         return false;
      }
   }

   public static <I> Class<? extends I> loadClass(Class<I> implementedInterface, String className, ClassLoader classLoader) throws Exception {
      Class<?> clazz = classLoader.loadClass(className);
      if (!implementedInterface.isAssignableFrom(clazz)) {
         return null;
      } else if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isEnum() && !clazz.isAnonymousClass()) {
         clazz.getDeclaredConstructor().newInstance();
         return clazz;
      } else {
         return null;
      }
   }

   public static String formatThrowable(Throwable e) {
      return "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
   }

   public static String getLocationOnDisk(Class<?> aClass) {
      ProtectionDomain protectionDomain = aClass.getProtectionDomain();
      if (protectionDomain == null) {
         return null;
      } else {
         CodeSource codeSource = protectionDomain.getCodeSource();
         return codeSource != null && codeSource.getLocation() != null ? UrlUtils.decodeURL(codeSource.getLocation().getPath()) : null;
      }
   }

   public static ClassLoader addJarsOrDirectoriesToClasspath(ClassLoader classLoader, List<File> jarFiles) {
      List<URL> urls = new ArrayList();

      for(File jarFile : jarFiles) {
         try {
            urls.add(jarFile.toURI().toURL());
         } catch (Exception var6) {
            throw new FlywayException("Unable to load " + jarFile.getPath(), var6);
         }
      }

      return new URLClassLoader((URL[])urls.toArray(new URL[0]), classLoader);
   }

   public static String getStaticFieldValue(String className, String fieldName, ClassLoader classLoader) {
      try {
         Class clazz = Class.forName(className, true, classLoader);
         Field field = clazz.getField(fieldName);
         return (String)field.get(null);
      } catch (Exception var5) {
         throw new FlywayException("Unable to obtain field value " + className + "." + fieldName + " : " + var5.getMessage(), var5);
      }
   }

   private ClassUtils() {
   }
}
