package io.micronaut.core.graal;

import com.oracle.svm.core.configure.ResourcesRegistry;
import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import io.micronaut.core.util.ArrayUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;

public final class AutomaticFeatureUtils {
   public static void initializeAtBuildTime(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(xva$0 -> RuntimeClassInitialization.initializeAtBuildTime(new Class[]{xva$0}));
   }

   public static void initializeAtBuildTime(BeforeAnalysisAccess access, String... classNames) {
      for(String className : classNames) {
         initializeAtBuildTime(access, className);
      }

   }

   public static void initializeAtRunTime(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(xva$0 -> RuntimeClassInitialization.initializeAtRunTime(new Class[]{xva$0}));
   }

   public static void initializeAtRunTime(BeforeAnalysisAccess access, String... classNames) {
      for(String className : classNames) {
         initializeAtRunTime(access, className);
      }

   }

   public static void initializePackagesAtBuildTime(String... packages) {
      RuntimeClassInitialization.initializeAtBuildTime(packages);
   }

   public static void initializePackagesAtRunTime(String... packages) {
      RuntimeClassInitialization.initializeAtRunTime(packages);
   }

   public static void registerConstructorsForRuntimeReflection(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerConstructorsForRuntimeReflection);
   }

   public static void registerClassForRuntimeReflection(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerForRuntimeReflection);
   }

   public static void registerClassForRuntimeReflectiveInstantiation(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerForReflectiveInstantiation);
   }

   public static void registerClassForRuntimeReflectionAndReflectiveInstantiation(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerForRuntimeReflectionAndReflectiveInstantiation);
   }

   public static void registerMethodsForRuntimeReflection(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerMethodsForRuntimeReflection);
   }

   public static void registerFieldsForRuntimeReflection(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerFieldsForRuntimeReflection);
   }

   public static void addProxyClass(BeforeAnalysisAccess access, String... interfaces) {
      List<Class<?>> classList = new ArrayList();

      for(String anInterface : interfaces) {
         Class<?> clazz = access.findClassByName(anInterface);
         if (clazz != null) {
            classList.add(clazz);
         }
      }

      if (classList.size() == interfaces.length) {
         ((DynamicProxyRegistry)ImageSingletons.lookup(DynamicProxyRegistry.class)).addProxyClass((Class[])classList.toArray(new Class[interfaces.length]));
      }

   }

   public static void addResourcePatterns(String... patterns) {
      if (ArrayUtils.isNotEmpty(patterns)) {
         ResourcesRegistry resourcesRegistry = (ResourcesRegistry)ImageSingletons.lookup(ResourcesRegistry.class);
         if (resourcesRegistry != null) {
            for(String resource : patterns) {
               resourcesRegistry.addResources(resource);
            }
         }
      }

   }

   public static void addResourceBundles(String... bundles) {
      if (ArrayUtils.isNotEmpty(bundles)) {
         ResourcesRegistry resourcesRegistry = (ResourcesRegistry)ImageSingletons.lookup(ResourcesRegistry.class);
         if (resourcesRegistry != null) {
            for(String resource : bundles) {
               resourcesRegistry.addResourceBundles(resource);
            }
         }
      }

   }

   public static void registerAllForRuntimeReflectionAndReflectiveInstantiation(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerAllForRuntimeReflectionAndReflectiveInstantiation);
   }

   public static void registerAllForRuntimeReflection(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerAllForRuntimeReflection);
   }

   public static void registerFieldsAndMethodsWithReflectiveAccess(BeforeAnalysisAccess access, String className) {
      findClass(access, className).ifPresent(AutomaticFeatureUtils::registerFieldsAndMethodsWithReflectiveAccess);
   }

   private static void registerAllForRuntimeReflectionAndReflectiveInstantiation(Class<?> clazz) {
      registerForRuntimeReflection(clazz);
      registerForReflectiveInstantiation(clazz);
      registerFieldsForRuntimeReflection(clazz);
      registerMethodsForRuntimeReflection(clazz);
      registerConstructorsForRuntimeReflection(clazz);
   }

   private static void registerAllForRuntimeReflection(Class<?> clazz) {
      registerForRuntimeReflection(clazz);
      registerFieldsForRuntimeReflection(clazz);
      registerMethodsForRuntimeReflection(clazz);
      registerConstructorsForRuntimeReflection(clazz);
   }

   private static void registerFieldsAndMethodsWithReflectiveAccess(Class<?> clazz) {
      registerForRuntimeReflectionAndReflectiveInstantiation(clazz);
      registerMethodsForRuntimeReflection(clazz);
      registerFieldsForRuntimeReflection(clazz);
   }

   private static void registerForRuntimeReflection(Class<?> clazz) {
      RuntimeReflection.register(new Class[]{clazz});
   }

   private static void registerForReflectiveInstantiation(Class<?> clazz) {
      RuntimeReflection.registerForReflectiveInstantiation(new Class[]{clazz});
   }

   private static void registerForRuntimeReflectionAndReflectiveInstantiation(Class<?> clazz) {
      RuntimeReflection.register(new Class[]{clazz});
      RuntimeReflection.registerForReflectiveInstantiation(new Class[]{clazz});
   }

   private static void registerMethodsForRuntimeReflection(Class<?> clazz) {
      for(Method method : clazz.getMethods()) {
         RuntimeReflection.register(new Executable[]{method});
      }

   }

   private static void registerFieldsForRuntimeReflection(Class<?> clazz) {
      for(Field field : clazz.getFields()) {
         RuntimeReflection.register(new Field[]{field});
      }

   }

   private static void registerConstructorsForRuntimeReflection(Class<?> clazz) {
      for(Constructor<?> constructor : clazz.getConstructors()) {
         RuntimeReflection.register(new Executable[]{constructor});
      }

   }

   private static Optional<Class<?>> findClass(BeforeAnalysisAccess access, String className) {
      return Optional.ofNullable(access.findClassByName(className));
   }
}
