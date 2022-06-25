package io.micronaut.caffeine.cache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

enum DisabledFuture implements Future<Void> {
   INSTANCE;

   public boolean isDone() {
      return true;
   }

   public boolean isCancelled() {
      return false;
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   public Void get() throws InterruptedException, ExecutionException {
      return null;
   }

   public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return null;
   }
}
