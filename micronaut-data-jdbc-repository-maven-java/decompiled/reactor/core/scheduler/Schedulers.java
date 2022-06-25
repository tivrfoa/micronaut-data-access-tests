package reactor.core.scheduler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.Metrics;
import reactor.util.annotation.Nullable;

public abstract class Schedulers {
   public static final int DEFAULT_POOL_SIZE = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultPoolSize"))
      .map(Integer::parseInt)
      .orElseGet(() -> Runtime.getRuntime().availableProcessors());
   public static final int DEFAULT_BOUNDED_ELASTIC_SIZE = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultBoundedElasticSize"))
      .map(Integer::parseInt)
      .orElseGet(() -> 10 * Runtime.getRuntime().availableProcessors());
   public static final int DEFAULT_BOUNDED_ELASTIC_QUEUESIZE = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultBoundedElasticQueueSize"))
      .map(Integer::parseInt)
      .orElse(100000);
   @Nullable
   static volatile BiConsumer<Thread, ? super Throwable> onHandleErrorHook;
   static final String ELASTIC = "elastic";
   static final String BOUNDED_ELASTIC = "boundedElastic";
   static final String PARALLEL = "parallel";
   static final String SINGLE = "single";
   static final String IMMEDIATE = "immediate";
   static final String FROM_EXECUTOR = "fromExecutor";
   static final String FROM_EXECUTOR_SERVICE = "fromExecutorService";
   static AtomicReference<Schedulers.CachedScheduler> CACHED_ELASTIC = new AtomicReference();
   static AtomicReference<Schedulers.CachedScheduler> CACHED_BOUNDED_ELASTIC = new AtomicReference();
   static AtomicReference<Schedulers.CachedScheduler> CACHED_PARALLEL = new AtomicReference();
   static AtomicReference<Schedulers.CachedScheduler> CACHED_SINGLE = new AtomicReference();
   static final Supplier<Scheduler> ELASTIC_SUPPLIER = () -> newElastic("elastic", 60, true);
   static final Supplier<Scheduler> BOUNDED_ELASTIC_SUPPLIER = () -> newBoundedElastic(
         DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "boundedElastic", 60, true
      );
   static final Supplier<Scheduler> PARALLEL_SUPPLIER = () -> newParallel("parallel", DEFAULT_POOL_SIZE, true);
   static final Supplier<Scheduler> SINGLE_SUPPLIER = () -> newSingle("single", true);
   static final Schedulers.Factory DEFAULT = new Schedulers.Factory() {
   };
   static final Map<String, BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService>> DECORATORS = new LinkedHashMap();
   static volatile Schedulers.Factory factory = DEFAULT;
   private static final LinkedHashMap<String, Function<Runnable, Runnable>> onScheduleHooks = new LinkedHashMap(1);
   @Nullable
   private static Function<Runnable, Runnable> onScheduleHook;
   static final Logger LOGGER = Loggers.getLogger(Schedulers.class);

   public static Scheduler fromExecutor(Executor executor) {
      return fromExecutor(executor, false);
   }

   public static Scheduler fromExecutor(Executor executor, boolean trampoline) {
      if (!trampoline && executor instanceof ExecutorService) {
         return fromExecutorService((ExecutorService)executor);
      } else {
         ExecutorScheduler scheduler = new ExecutorScheduler(executor, trampoline);
         scheduler.start();
         return scheduler;
      }
   }

   public static Scheduler fromExecutorService(ExecutorService executorService) {
      String executorServiceHashcode = Integer.toHexString(System.identityHashCode(executorService));
      return fromExecutorService(executorService, "anonymousExecutor@" + executorServiceHashcode);
   }

   public static Scheduler fromExecutorService(ExecutorService executorService, String executorName) {
      DelegateServiceScheduler scheduler = new DelegateServiceScheduler(executorName, executorService);
      scheduler.start();
      return scheduler;
   }

   @Deprecated
   public static Scheduler elastic() {
      return cache(CACHED_ELASTIC, "elastic", ELASTIC_SUPPLIER);
   }

   public static Scheduler boundedElastic() {
      return cache(CACHED_BOUNDED_ELASTIC, "boundedElastic", BOUNDED_ELASTIC_SUPPLIER);
   }

   public static Scheduler parallel() {
      return cache(CACHED_PARALLEL, "parallel", PARALLEL_SUPPLIER);
   }

   public static Scheduler immediate() {
      return ImmediateScheduler.instance();
   }

   @Deprecated
   public static Scheduler newElastic(String name) {
      return newElastic(name, 60);
   }

   @Deprecated
   public static Scheduler newElastic(String name, int ttlSeconds) {
      return newElastic(name, ttlSeconds, false);
   }

   @Deprecated
   public static Scheduler newElastic(String name, int ttlSeconds, boolean daemon) {
      return newElastic(ttlSeconds, new ReactorThreadFactory(name, ElasticScheduler.COUNTER, daemon, false, Schedulers::defaultUncaughtException));
   }

   @Deprecated
   public static Scheduler newElastic(int ttlSeconds, ThreadFactory threadFactory) {
      Scheduler fromFactory = factory.newElastic(ttlSeconds, threadFactory);
      fromFactory.start();
      return fromFactory;
   }

   public static Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, String name) {
      return newBoundedElastic(threadCap, queuedTaskCap, name, 60, false);
   }

   public static Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, String name, int ttlSeconds) {
      return newBoundedElastic(threadCap, queuedTaskCap, name, ttlSeconds, false);
   }

   public static Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, String name, int ttlSeconds, boolean daemon) {
      return newBoundedElastic(
         threadCap, queuedTaskCap, new ReactorThreadFactory(name, ElasticScheduler.COUNTER, daemon, false, Schedulers::defaultUncaughtException), ttlSeconds
      );
   }

   public static Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory, int ttlSeconds) {
      Scheduler fromFactory = factory.newBoundedElastic(threadCap, queuedTaskCap, threadFactory, ttlSeconds);
      fromFactory.start();
      return fromFactory;
   }

   public static Scheduler newParallel(String name) {
      return newParallel(name, DEFAULT_POOL_SIZE);
   }

   public static Scheduler newParallel(String name, int parallelism) {
      return newParallel(name, parallelism, false);
   }

   public static Scheduler newParallel(String name, int parallelism, boolean daemon) {
      return newParallel(parallelism, new ReactorThreadFactory(name, ParallelScheduler.COUNTER, daemon, true, Schedulers::defaultUncaughtException));
   }

   public static Scheduler newParallel(int parallelism, ThreadFactory threadFactory) {
      Scheduler fromFactory = factory.newParallel(parallelism, threadFactory);
      fromFactory.start();
      return fromFactory;
   }

   public static Scheduler newSingle(String name) {
      return newSingle(name, false);
   }

   public static Scheduler newSingle(String name, boolean daemon) {
      return newSingle(new ReactorThreadFactory(name, SingleScheduler.COUNTER, daemon, true, Schedulers::defaultUncaughtException));
   }

   public static Scheduler newSingle(ThreadFactory threadFactory) {
      Scheduler fromFactory = factory.newSingle(threadFactory);
      fromFactory.start();
      return fromFactory;
   }

   public static void onHandleError(BiConsumer<Thread, ? super Throwable> c) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Hooking new default: onHandleError");
      }

      onHandleErrorHook = (BiConsumer)Objects.requireNonNull(c, "onHandleError");
   }

   public static boolean isInNonBlockingThread() {
      return Thread.currentThread() instanceof NonBlocking;
   }

   public static boolean isNonBlockingThread(Thread t) {
      return t instanceof NonBlocking;
   }

   public static void enableMetrics() {
      if (Metrics.isInstrumentationAvailable()) {
         addExecutorServiceDecorator("reactor.metrics.decorator", new SchedulerMetricDecorator());
      }

   }

   public static void disableMetrics() {
      removeExecutorServiceDecorator("reactor.metrics.decorator");
   }

   public static void resetFactory() {
      setFactory(DEFAULT);
   }

   public static Schedulers.Snapshot setFactoryWithSnapshot(Schedulers.Factory newFactory) {
      Schedulers.Snapshot snapshot = new Schedulers.Snapshot(
         (Schedulers.CachedScheduler)CACHED_ELASTIC.getAndSet(null),
         (Schedulers.CachedScheduler)CACHED_BOUNDED_ELASTIC.getAndSet(null),
         (Schedulers.CachedScheduler)CACHED_PARALLEL.getAndSet(null),
         (Schedulers.CachedScheduler)CACHED_SINGLE.getAndSet(null),
         factory
      );
      setFactory(newFactory);
      return snapshot;
   }

   public static void resetFrom(@Nullable Schedulers.Snapshot snapshot) {
      if (snapshot == null) {
         resetFactory();
      } else {
         Schedulers.CachedScheduler oldElastic = (Schedulers.CachedScheduler)CACHED_ELASTIC.getAndSet(snapshot.oldElasticScheduler);
         Schedulers.CachedScheduler oldBoundedElastic = (Schedulers.CachedScheduler)CACHED_BOUNDED_ELASTIC.getAndSet(snapshot.oldBoundedElasticScheduler);
         Schedulers.CachedScheduler oldParallel = (Schedulers.CachedScheduler)CACHED_PARALLEL.getAndSet(snapshot.oldParallelScheduler);
         Schedulers.CachedScheduler oldSingle = (Schedulers.CachedScheduler)CACHED_SINGLE.getAndSet(snapshot.oldSingleScheduler);
         factory = snapshot.oldFactory;
         if (oldElastic != null) {
            oldElastic._dispose();
         }

         if (oldBoundedElastic != null) {
            oldBoundedElastic._dispose();
         }

         if (oldParallel != null) {
            oldParallel._dispose();
         }

         if (oldSingle != null) {
            oldSingle._dispose();
         }

      }
   }

   public static void resetOnHandleError() {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Reset to factory defaults: onHandleError");
      }

      onHandleErrorHook = null;
   }

   public static void setFactory(Schedulers.Factory factoryInstance) {
      Objects.requireNonNull(factoryInstance, "factoryInstance");
      shutdownNow();
      factory = factoryInstance;
   }

   public static boolean addExecutorServiceDecorator(String key, BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> decorator) {
      synchronized(DECORATORS) {
         return DECORATORS.putIfAbsent(key, decorator) == null;
      }
   }

   public static void setExecutorServiceDecorator(String key, BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> decorator) {
      synchronized(DECORATORS) {
         DECORATORS.put(key, decorator);
      }
   }

   public static BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> removeExecutorServiceDecorator(String key) {
      BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> removed;
      synchronized(DECORATORS) {
         removed = (BiFunction)DECORATORS.remove(key);
      }

      if (removed instanceof Disposable) {
         ((Disposable)removed).dispose();
      }

      return removed;
   }

   public static ScheduledExecutorService decorateExecutorService(Scheduler owner, ScheduledExecutorService original) {
      synchronized(DECORATORS) {
         for(BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService> decorator : DECORATORS.values()) {
            original = (ScheduledExecutorService)decorator.apply(owner, original);
         }

         return original;
      }
   }

   public static void onScheduleHook(String key, Function<Runnable, Runnable> decorator) {
      synchronized(onScheduleHooks) {
         onScheduleHooks.put(key, decorator);
         Function<Runnable, Runnable> newHook = null;

         for(Function<Runnable, Runnable> function : onScheduleHooks.values()) {
            if (newHook == null) {
               newHook = function;
            } else {
               newHook = newHook.andThen(function);
            }
         }

         onScheduleHook = newHook;
      }
   }

   public static void resetOnScheduleHook(String key) {
      synchronized(onScheduleHooks) {
         onScheduleHooks.remove(key);
         if (onScheduleHooks.isEmpty()) {
            onScheduleHook = Function.identity();
         } else {
            Function<Runnable, Runnable> newHook = null;

            for(Function<Runnable, Runnable> function : onScheduleHooks.values()) {
               if (newHook == null) {
                  newHook = function;
               } else {
                  newHook = newHook.andThen(function);
               }
            }

            onScheduleHook = newHook;
         }

      }
   }

   public static void resetOnScheduleHooks() {
      synchronized(onScheduleHooks) {
         onScheduleHooks.clear();
         onScheduleHook = null;
      }
   }

   public static Runnable onSchedule(Runnable runnable) {
      Function<Runnable, Runnable> hook = onScheduleHook;
      return hook != null ? (Runnable)hook.apply(runnable) : runnable;
   }

   public static void shutdownNow() {
      Schedulers.CachedScheduler oldElastic = (Schedulers.CachedScheduler)CACHED_ELASTIC.getAndSet(null);
      Schedulers.CachedScheduler oldBoundedElastic = (Schedulers.CachedScheduler)CACHED_BOUNDED_ELASTIC.getAndSet(null);
      Schedulers.CachedScheduler oldParallel = (Schedulers.CachedScheduler)CACHED_PARALLEL.getAndSet(null);
      Schedulers.CachedScheduler oldSingle = (Schedulers.CachedScheduler)CACHED_SINGLE.getAndSet(null);
      if (oldElastic != null) {
         oldElastic._dispose();
      }

      if (oldBoundedElastic != null) {
         oldBoundedElastic._dispose();
      }

      if (oldParallel != null) {
         oldParallel._dispose();
      }

      if (oldSingle != null) {
         oldSingle._dispose();
      }

   }

   public static Scheduler single() {
      return cache(CACHED_SINGLE, "single", SINGLE_SUPPLIER);
   }

   public static Scheduler single(Scheduler original) {
      return new SingleWorkerScheduler(original);
   }

   static Schedulers.CachedScheduler cache(AtomicReference<Schedulers.CachedScheduler> reference, String key, Supplier<Scheduler> supplier) {
      Schedulers.CachedScheduler s = (Schedulers.CachedScheduler)reference.get();
      if (s != null) {
         return s;
      } else {
         s = new Schedulers.CachedScheduler(key, (Scheduler)supplier.get());
         if (reference.compareAndSet(null, s)) {
            return s;
         } else {
            s._dispose();
            return (Schedulers.CachedScheduler)reference.get();
         }
      }
   }

   static final void defaultUncaughtException(Thread t, Throwable e) {
      LOGGER.error("Scheduler worker in group " + t.getThreadGroup().getName() + " failed with an uncaught exception", e);
   }

   static void handleError(Throwable ex) {
      Thread thread = Thread.currentThread();
      Throwable t = Exceptions.unwrap(ex);
      UncaughtExceptionHandler x = thread.getUncaughtExceptionHandler();
      if (x != null) {
         x.uncaughtException(thread, t);
      } else {
         LOGGER.error("Scheduler worker failed with an uncaught exception", t);
      }

      BiConsumer<Thread, ? super Throwable> hook = onHandleErrorHook;
      if (hook != null) {
         hook.accept(thread, t);
      }

   }

   static Disposable directSchedule(ScheduledExecutorService exec, Runnable task, @Nullable Disposable parent, long delay, TimeUnit unit) {
      task = onSchedule(task);
      SchedulerTask sr = new SchedulerTask(task, parent);
      Future<?> f;
      if (delay <= 0L) {
         f = exec.submit(sr);
      } else {
         f = exec.schedule(sr, delay, unit);
      }

      sr.setFuture(f);
      return sr;
   }

   static Disposable directSchedulePeriodically(ScheduledExecutorService exec, Runnable task, long initialDelay, long period, TimeUnit unit) {
      task = onSchedule(task);
      if (period <= 0L) {
         InstantPeriodicWorkerTask isr = new InstantPeriodicWorkerTask(task, exec);
         Future<?> f;
         if (initialDelay <= 0L) {
            f = exec.submit(isr);
         } else {
            f = exec.schedule(isr, initialDelay, unit);
         }

         isr.setFirst(f);
         return isr;
      } else {
         PeriodicSchedulerTask sr = new PeriodicSchedulerTask(task);
         Future<?> f = exec.scheduleAtFixedRate(sr, initialDelay, period, unit);
         sr.setFuture(f);
         return sr;
      }
   }

   static Disposable workerSchedule(ScheduledExecutorService exec, Disposable.Composite tasks, Runnable task, long delay, TimeUnit unit) {
      task = onSchedule(task);
      WorkerTask sr = new WorkerTask(task, tasks);
      if (!tasks.add(sr)) {
         throw Exceptions.failWithRejected();
      } else {
         try {
            Future<?> f;
            if (delay <= 0L) {
               f = exec.submit(sr);
            } else {
               f = exec.schedule(sr, delay, unit);
            }

            sr.setFuture(f);
            return sr;
         } catch (RejectedExecutionException var8) {
            sr.dispose();
            throw var8;
         }
      }
   }

   static Disposable workerSchedulePeriodically(
      ScheduledExecutorService exec, Disposable.Composite tasks, Runnable task, long initialDelay, long period, TimeUnit unit
   ) {
      task = onSchedule(task);
      if (period <= 0L) {
         InstantPeriodicWorkerTask isr = new InstantPeriodicWorkerTask(task, exec, tasks);
         if (!tasks.add(isr)) {
            throw Exceptions.failWithRejected();
         } else {
            try {
               Future<?> f;
               if (initialDelay <= 0L) {
                  f = exec.submit(isr);
               } else {
                  f = exec.schedule(isr, initialDelay, unit);
               }

               isr.setFirst(f);
               return isr;
            } catch (RejectedExecutionException var10) {
               isr.dispose();
               throw var10;
            } catch (NullPointerException | IllegalArgumentException var11) {
               isr.dispose();
               throw new RejectedExecutionException(var11);
            }
         }
      } else {
         PeriodicWorkerTask sr = new PeriodicWorkerTask(task, tasks);
         if (!tasks.add(sr)) {
            throw Exceptions.failWithRejected();
         } else {
            try {
               Future<?> f = exec.scheduleAtFixedRate(sr, initialDelay, period, unit);
               sr.setFuture(f);
               return sr;
            } catch (RejectedExecutionException var12) {
               sr.dispose();
               throw var12;
            } catch (NullPointerException | IllegalArgumentException var13) {
               sr.dispose();
               throw new RejectedExecutionException(var13);
            }
         }
      }
   }

   @Nullable
   static final Object scanExecutor(Executor executor, Scannable.Attr key) {
      if (executor instanceof DelegateServiceScheduler.UnsupportedScheduledExecutorService) {
         executor = ((DelegateServiceScheduler.UnsupportedScheduledExecutorService)executor).get();
      }

      if (executor instanceof Scannable) {
         return ((Scannable)executor).scanUnsafe(key);
      } else {
         if (executor instanceof ExecutorService) {
            ExecutorService service = (ExecutorService)executor;
            if (key == Scannable.Attr.TERMINATED) {
               return service.isTerminated();
            }

            if (key == Scannable.Attr.CANCELLED) {
               return service.isShutdown();
            }
         }

         if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor)executor;
            if (key == Scannable.Attr.CAPACITY) {
               return poolExecutor.getMaximumPoolSize();
            }

            if (key == Scannable.Attr.BUFFERED) {
               return Long.valueOf(poolExecutor.getTaskCount() - poolExecutor.getCompletedTaskCount()).intValue();
            }

            if (key == Scannable.Attr.LARGE_BUFFERED) {
               return poolExecutor.getTaskCount() - poolExecutor.getCompletedTaskCount();
            }
         }

         return null;
      }
   }

   static class CachedScheduler implements Scheduler, Supplier<Scheduler>, Scannable {
      final Scheduler cached;
      final String stringRepresentation;

      CachedScheduler(String key, Scheduler cached) {
         this.cached = cached;
         this.stringRepresentation = "Schedulers." + key + "()";
      }

      @Override
      public Disposable schedule(Runnable task) {
         return this.cached.schedule(task);
      }

      @Override
      public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
         return this.cached.schedule(task, delay, unit);
      }

      @Override
      public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
         return this.cached.schedulePeriodically(task, initialDelay, period, unit);
      }

      @Override
      public Scheduler.Worker createWorker() {
         return this.cached.createWorker();
      }

      @Override
      public long now(TimeUnit unit) {
         return this.cached.now(unit);
      }

      @Override
      public void start() {
         this.cached.start();
      }

      @Override
      public void dispose() {
      }

      @Override
      public boolean isDisposed() {
         return this.cached.isDisposed();
      }

      public String toString() {
         return this.stringRepresentation;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return Scannable.Attr.NAME == key ? this.stringRepresentation : Scannable.from(this.cached).scanUnsafe(key);
      }

      public Scheduler get() {
         return this.cached;
      }

      void _dispose() {
         this.cached.dispose();
      }
   }

   public interface Factory {
      @Deprecated
      default Scheduler newElastic(int ttlSeconds, ThreadFactory threadFactory) {
         return new ElasticScheduler(threadFactory, ttlSeconds);
      }

      default Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory, int ttlSeconds) {
         return new BoundedElasticScheduler(threadCap, queuedTaskCap, threadFactory, ttlSeconds);
      }

      default Scheduler newParallel(int parallelism, ThreadFactory threadFactory) {
         return new ParallelScheduler(parallelism, threadFactory);
      }

      default Scheduler newSingle(ThreadFactory threadFactory) {
         return new SingleScheduler(threadFactory);
      }
   }

   public static final class Snapshot implements Disposable {
      @Nullable
      final Schedulers.CachedScheduler oldElasticScheduler;
      @Nullable
      final Schedulers.CachedScheduler oldBoundedElasticScheduler;
      @Nullable
      final Schedulers.CachedScheduler oldParallelScheduler;
      @Nullable
      final Schedulers.CachedScheduler oldSingleScheduler;
      final Schedulers.Factory oldFactory;

      private Snapshot(
         @Nullable Schedulers.CachedScheduler oldElasticScheduler,
         @Nullable Schedulers.CachedScheduler oldBoundedElasticScheduler,
         @Nullable Schedulers.CachedScheduler oldParallelScheduler,
         @Nullable Schedulers.CachedScheduler oldSingleScheduler,
         Schedulers.Factory factory
      ) {
         this.oldElasticScheduler = oldElasticScheduler;
         this.oldBoundedElasticScheduler = oldBoundedElasticScheduler;
         this.oldParallelScheduler = oldParallelScheduler;
         this.oldSingleScheduler = oldSingleScheduler;
         this.oldFactory = factory;
      }

      @Override
      public boolean isDisposed() {
         return (this.oldElasticScheduler == null || this.oldElasticScheduler.isDisposed())
            && (this.oldBoundedElasticScheduler == null || this.oldBoundedElasticScheduler.isDisposed())
            && (this.oldParallelScheduler == null || this.oldParallelScheduler.isDisposed())
            && (this.oldSingleScheduler == null || this.oldSingleScheduler.isDisposed());
      }

      @Override
      public void dispose() {
         if (this.oldElasticScheduler != null) {
            this.oldElasticScheduler._dispose();
         }

         if (this.oldBoundedElasticScheduler != null) {
            this.oldBoundedElasticScheduler._dispose();
         }

         if (this.oldParallelScheduler != null) {
            this.oldParallelScheduler._dispose();
         }

         if (this.oldSingleScheduler != null) {
            this.oldSingleScheduler._dispose();
         }

      }
   }
}
