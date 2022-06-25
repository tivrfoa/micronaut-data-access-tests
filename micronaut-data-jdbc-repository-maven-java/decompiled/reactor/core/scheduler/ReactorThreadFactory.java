package reactor.core.scheduler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

class ReactorThreadFactory implements ThreadFactory, Supplier<String>, UncaughtExceptionHandler {
   private final String name;
   private final AtomicLong counterReference;
   private final boolean daemon;
   private final boolean rejectBlocking;
   @Nullable
   private final BiConsumer<Thread, Throwable> uncaughtExceptionHandler;

   ReactorThreadFactory(
      String name, AtomicLong counterReference, boolean daemon, boolean rejectBlocking, @Nullable BiConsumer<Thread, Throwable> uncaughtExceptionHandler
   ) {
      this.name = name;
      this.counterReference = counterReference;
      this.daemon = daemon;
      this.rejectBlocking = rejectBlocking;
      this.uncaughtExceptionHandler = uncaughtExceptionHandler;
   }

   public final Thread newThread(@NonNull Runnable runnable) {
      String newThreadName = this.name + "-" + this.counterReference.incrementAndGet();
      Thread t = (Thread)(this.rejectBlocking ? new ReactorThreadFactory.NonBlockingThread(runnable, newThreadName) : new Thread(runnable, newThreadName));
      if (this.daemon) {
         t.setDaemon(true);
      }

      if (this.uncaughtExceptionHandler != null) {
         t.setUncaughtExceptionHandler(this);
      }

      return t;
   }

   public void uncaughtException(Thread t, Throwable e) {
      if (this.uncaughtExceptionHandler != null) {
         this.uncaughtExceptionHandler.accept(t, e);
      }
   }

   public final String get() {
      return this.name;
   }

   static final class NonBlockingThread extends Thread implements NonBlocking {
      public NonBlockingThread(Runnable target, String name) {
         super(target, name);
      }
   }
}
