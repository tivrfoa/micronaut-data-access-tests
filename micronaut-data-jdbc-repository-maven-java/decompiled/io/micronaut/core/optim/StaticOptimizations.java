package io.micronaut.core.optim;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public abstract class StaticOptimizations {
   private static final boolean CAPTURE_STACKTRACE_ON_READ = Boolean.getBoolean("micronaut.optimizations.capture.read.trace");
   private static final Map<Class<?>, Object> OPTIMIZATIONS = new ConcurrentHashMap();
   private static final Map<Class<?>, StackTraceElement[]> CHECKED = new ConcurrentHashMap();
   private static boolean cacheEnvironment = false;

   static void reset() {
      OPTIMIZATIONS.clear();
      CHECKED.clear();
      ServiceLoader.load(StaticOptimizations.Loader.class).forEach(loader -> set(loader.load()));
   }

   public static void cacheEnvironment() {
      cacheEnvironment = true;
   }

   @NonNull
   public static <T> Optional<T> get(@NonNull Class<T> optimizationClass) {
      CHECKED.put(optimizationClass, maybeCaptureStackTrace());
      T value = (T)OPTIMIZATIONS.get(optimizationClass);
      return Optional.ofNullable(value);
   }

   private static StackTraceElement[] maybeCaptureStackTrace() {
      return CAPTURE_STACKTRACE_ON_READ ? new Exception().getStackTrace() : new StackTraceElement[0];
   }

   public static <T> void set(@NonNull T value) {
      Class<?> optimizationClass = value.getClass();
      if (!CHECKED.containsKey(optimizationClass)) {
         OPTIMIZATIONS.put(optimizationClass, value);
      } else if (!CAPTURE_STACKTRACE_ON_READ) {
         throw new IllegalStateException(
            "Optimization state for "
               + optimizationClass
               + " was read before it was set. Run with -Dmicronaut.optimizations.capture.read.trace=true to enable stack trace capture."
         );
      } else {
         StringBuilder sb = new StringBuilder("Optimization state for " + optimizationClass + " was read before it was set. Stack trace:\n");
         StackTraceElement[] stackTrace = (StackTraceElement[])CHECKED.get(optimizationClass);

         for(StackTraceElement element : stackTrace) {
            sb.append("\t").append(element.toString()).append("\n");
         }

         throw new IllegalStateException(sb.toString());
      }
   }

   public static boolean isEnvironmentCached() {
      return cacheEnvironment;
   }

   static {
      reset();
   }

   @FunctionalInterface
   public interface Loader<T> {
      T load();
   }
}
