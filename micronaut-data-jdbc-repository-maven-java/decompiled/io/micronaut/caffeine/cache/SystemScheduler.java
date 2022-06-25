package io.micronaut.caffeine.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.Nullable;

enum SystemScheduler implements Scheduler {
   INSTANCE;

   @Nullable
   static final Method delayedExecutor = getDelayedExecutorMethod();

   @Override
   public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Objects.requireNonNull(executor);
      Objects.requireNonNull(command);
      Objects.requireNonNull(unit);

      try {
         Executor scheduler = (Executor)delayedExecutor.invoke(CompletableFuture.class, delay, unit, executor);
         return CompletableFuture.runAsync(command, scheduler);
      } catch (InvocationTargetException | IllegalAccessException var7) {
         throw new RuntimeException(var7);
      }
   }

   @Nullable
   static Method getDelayedExecutorMethod() {
      try {
         return CompletableFuture.class.getMethod("delayedExecutor", Long.TYPE, TimeUnit.class, Executor.class);
      } catch (SecurityException | NoSuchMethodException var1) {
         return null;
      }
   }

   static boolean isPresent() {
      return delayedExecutor != null;
   }
}
