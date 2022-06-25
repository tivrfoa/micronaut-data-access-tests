package reactor.core.publisher;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

final class SinksSpecs {
   static final Sinks.RootSpec UNSAFE_ROOT_SPEC = new SinksSpecs.RootSpecImpl(false);
   static final Sinks.RootSpec DEFAULT_ROOT_SPEC = new SinksSpecs.RootSpecImpl(true);

   abstract static class AbstractSerializedSink {
      volatile int wip;
      static final AtomicIntegerFieldUpdater<SinksSpecs.AbstractSerializedSink> WIP = AtomicIntegerFieldUpdater.newUpdater(
         SinksSpecs.AbstractSerializedSink.class, "wip"
      );
      volatile Thread lockedAt;
      static final AtomicReferenceFieldUpdater<SinksSpecs.AbstractSerializedSink, Thread> LOCKED_AT = AtomicReferenceFieldUpdater.newUpdater(
         SinksSpecs.AbstractSerializedSink.class, Thread.class, "lockedAt"
      );

      boolean tryAcquire(Thread currentThread) {
         if (WIP.get(this) == 0 && WIP.compareAndSet(this, 0, 1)) {
            LOCKED_AT.lazySet(this, currentThread);
         } else {
            if (LOCKED_AT.get(this) != currentThread) {
               return false;
            }

            WIP.incrementAndGet(this);
         }

         return true;
      }
   }

   static final class RootSpecImpl implements Sinks.RootSpec, Sinks.ManySpec, Sinks.MulticastSpec, Sinks.MulticastReplaySpec {
      final boolean serialized;
      final Sinks.UnicastSpec unicastSpec;

      RootSpecImpl(boolean serialized) {
         this.serialized = serialized;
         this.unicastSpec = new SinksSpecs.UnicastSpecImpl(serialized);
      }

      <T, EMPTY extends Sinks.Empty<T> & ContextHolder> Sinks.Empty<T> wrapEmpty(EMPTY original) {
         return (Sinks.Empty<T>)(this.serialized ? new SinkEmptySerialized<>(original, original) : original);
      }

      <T, ONE extends Sinks.One<T> & ContextHolder> Sinks.One<T> wrapOne(ONE original) {
         return (Sinks.One<T>)(this.serialized ? new SinkOneSerialized<>(original, original) : original);
      }

      <T, MANY extends Sinks.Many<T> & ContextHolder> Sinks.Many<T> wrapMany(MANY original) {
         return (Sinks.Many<T>)(this.serialized ? new SinkManySerialized<>(original, original) : original);
      }

      @Override
      public Sinks.ManySpec many() {
         return this;
      }

      @Override
      public <T> Sinks.Empty<T> empty() {
         return this.wrapEmpty(new SinkEmptyMulticast());
      }

      @Override
      public <T> Sinks.One<T> one() {
         return this.wrapOne(new SinkOneMulticast());
      }

      @Override
      public Sinks.UnicastSpec unicast() {
         return this.unicastSpec;
      }

      @Override
      public Sinks.MulticastSpec multicast() {
         return this;
      }

      @Override
      public Sinks.MulticastReplaySpec replay() {
         return this;
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer() {
         EmitterProcessor<T> original = EmitterProcessor.create();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer(int bufferSize) {
         EmitterProcessor<T> original = EmitterProcessor.create(bufferSize);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer(int bufferSize, boolean autoCancel) {
         EmitterProcessor<T> original = EmitterProcessor.create(bufferSize, autoCancel);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> directAllOrNothing() {
         SinkManyBestEffort<T> original = SinkManyBestEffort.createAllOrNothing();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> directBestEffort() {
         SinkManyBestEffort<T> original = SinkManyBestEffort.createBestEffort();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> all() {
         ReplayProcessor<T> original = ReplayProcessor.create();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> all(int batchSize) {
         ReplayProcessor<T> original = ReplayProcessor.create(batchSize, true);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> latest() {
         ReplayProcessor<T> original = ReplayProcessor.cacheLast();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> latestOrDefault(T value) {
         ReplayProcessor<T> original = ReplayProcessor.cacheLastOrDefault(value);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> limit(int historySize) {
         if (historySize <= 0) {
            throw new IllegalArgumentException("historySize must be > 0");
         } else {
            ReplayProcessor<T> original = ReplayProcessor.create(historySize);
            return this.wrapMany(original);
         }
      }

      @Override
      public <T> Sinks.Many<T> limit(Duration maxAge) {
         ReplayProcessor<T> original = ReplayProcessor.createTimeout(maxAge);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> limit(Duration maxAge, Scheduler scheduler) {
         ReplayProcessor<T> original = ReplayProcessor.createTimeout(maxAge, scheduler);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> limit(int historySize, Duration maxAge) {
         if (historySize <= 0) {
            throw new IllegalArgumentException("historySize must be > 0");
         } else {
            ReplayProcessor<T> original = ReplayProcessor.createSizeAndTimeout(historySize, maxAge);
            return this.wrapMany(original);
         }
      }

      @Override
      public <T> Sinks.Many<T> limit(int historySize, Duration maxAge, Scheduler scheduler) {
         if (historySize <= 0) {
            throw new IllegalArgumentException("historySize must be > 0");
         } else {
            ReplayProcessor<T> original = ReplayProcessor.createSizeAndTimeout(historySize, maxAge, scheduler);
            return this.wrapMany(original);
         }
      }
   }

   static final class UnicastSpecImpl implements Sinks.UnicastSpec {
      final boolean serialized;

      UnicastSpecImpl(boolean serialized) {
         this.serialized = serialized;
      }

      <T, MANY extends Sinks.Many<T> & ContextHolder> Sinks.Many<T> wrapMany(MANY original) {
         return (Sinks.Many<T>)(this.serialized ? new SinkManySerialized<>(original, original) : original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer() {
         UnicastProcessor<T> original = UnicastProcessor.create();
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer(Queue<T> queue) {
         UnicastProcessor<T> original = UnicastProcessor.create(queue);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureBuffer(Queue<T> queue, Disposable endCallback) {
         UnicastProcessor<T> original = UnicastProcessor.create(queue, endCallback);
         return this.wrapMany(original);
      }

      @Override
      public <T> Sinks.Many<T> onBackpressureError() {
         UnicastManySinkNoBackpressure<T> original = UnicastManySinkNoBackpressure.create();
         return this.wrapMany(original);
      }
   }
}
