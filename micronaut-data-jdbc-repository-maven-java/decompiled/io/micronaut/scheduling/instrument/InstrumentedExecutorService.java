package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public interface InstrumentedExecutorService extends ExecutorService, InstrumentedExecutor {
   ExecutorService getTarget();

   default <T> Callable<T> instrument(Callable<T> task) {
      return task;
   }

   default void shutdown() {
      this.getTarget().shutdown();
   }

   default List<Runnable> shutdownNow() {
      return this.getTarget().shutdownNow();
   }

   default boolean isShutdown() {
      return this.getTarget().isShutdown();
   }

   default boolean isTerminated() {
      return this.getTarget().isTerminated();
   }

   default boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
      return this.getTarget().awaitTermination(timeout, unit);
   }

   @NonNull
   default <T> Future<T> submit(@NonNull Callable<T> task) {
      return this.getTarget().submit(this.instrument(task));
   }

   @NonNull
   default <T> Future<T> submit(@NonNull Runnable task, T result) {
      return this.getTarget().submit(this.instrument(task), result);
   }

   @NonNull
   default Future<?> submit(@NonNull Runnable task) {
      return this.getTarget().submit(this.instrument(task));
   }

   @NonNull
   default <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return this.getTarget().invokeAll((Collection)tasks.stream().map(this::instrument).collect(Collectors.toList()));
   }

   @NonNull
   default <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws InterruptedException {
      return this.getTarget().invokeAll((Collection)tasks.stream().map(this::instrument).collect(Collectors.toList()), timeout, unit);
   }

   @NonNull
   default <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return (T)this.getTarget().invokeAny((Collection)tasks.stream().map(this::instrument).collect(Collectors.toList()));
   }

   @NonNull
   default <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return (T)this.getTarget().invokeAny((Collection)tasks.stream().map(this::instrument).collect(Collectors.toList()), timeout, unit);
   }
}
