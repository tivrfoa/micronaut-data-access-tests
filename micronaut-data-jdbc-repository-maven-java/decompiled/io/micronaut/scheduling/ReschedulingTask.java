package io.micronaut.scheduling;

import io.micronaut.scheduling.exceptions.TaskExecutionException;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

class ReschedulingTask<V> implements ScheduledFuture<V>, Runnable, Callable<V> {
   private final Callable<V> task;
   private final TaskScheduler taskScheduler;
   private final NextFireTime nextTime;
   private ScheduledFuture<?> currentFuture;
   private AtomicBoolean cancelled = new AtomicBoolean(false);

   ReschedulingTask(Callable<V> task, TaskScheduler taskScheduler, NextFireTime nextTime) {
      this.task = task;
      this.taskScheduler = taskScheduler;
      this.nextTime = nextTime;
      this.currentFuture = taskScheduler.schedule(nextTime.get(), (Callable<V>)this);
   }

   public V call() throws Exception {
      Object var1;
      try {
         var1 = this.task.call();
      } finally {
         synchronized(this) {
            if (!this.cancelled.get()) {
               this.currentFuture = this.taskScheduler.schedule(this.nextTime.get(), (Callable<V>)this);
            }

         }
      }

      return (V)var1;
   }

   public void run() {
      try {
         this.call();
      } catch (Exception var2) {
         throw new TaskExecutionException("Error executing task: " + var2.getMessage(), var2);
      }
   }

   public long getDelay(TimeUnit unit) {
      ScheduledFuture current;
      synchronized(this) {
         current = this.currentFuture;
      }

      return current.getDelay(unit);
   }

   public int compareTo(Delayed o) {
      ScheduledFuture current;
      synchronized(this) {
         current = this.currentFuture;
      }

      return current.compareTo(o);
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      ScheduledFuture current;
      synchronized(this) {
         this.cancelled.set(true);
         current = this.currentFuture;
      }

      return current.cancel(mayInterruptIfRunning);
   }

   public boolean isCancelled() {
      return this.cancelled.get();
   }

   public boolean isDone() {
      synchronized(this) {
         return this.currentFuture.isDone();
      }
   }

   public V get() throws InterruptedException, ExecutionException {
      ScheduledFuture current;
      synchronized(this) {
         this.cancelled.set(true);
         current = this.currentFuture;
      }

      return (V)current.get();
   }

   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      ScheduledFuture current;
      synchronized(this) {
         this.cancelled.set(true);
         current = this.currentFuture;
      }

      return (V)current.get(timeout, unit);
   }
}
