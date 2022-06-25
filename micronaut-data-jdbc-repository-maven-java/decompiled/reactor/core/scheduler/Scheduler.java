package reactor.core.scheduler;

import java.util.concurrent.TimeUnit;
import reactor.core.Disposable;
import reactor.core.Exceptions;

public interface Scheduler extends Disposable {
   Disposable schedule(Runnable var1);

   default Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      throw Exceptions.failWithRejectedNotTimeCapable();
   }

   default Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      throw Exceptions.failWithRejectedNotTimeCapable();
   }

   default long now(TimeUnit unit) {
      return unit.compareTo(TimeUnit.MILLISECONDS) >= 0
         ? unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
         : unit.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
   }

   Scheduler.Worker createWorker();

   @Override
   default void dispose() {
   }

   default void start() {
   }

   public interface Worker extends Disposable {
      Disposable schedule(Runnable var1);

      default Disposable schedule(Runnable task, long delay, TimeUnit unit) {
         throw Exceptions.failWithRejectedNotTimeCapable();
      }

      default Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
         throw Exceptions.failWithRejectedNotTimeCapable();
      }
   }
}
