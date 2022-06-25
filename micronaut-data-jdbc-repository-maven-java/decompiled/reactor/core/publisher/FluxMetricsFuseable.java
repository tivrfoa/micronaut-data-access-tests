package reactor.core.publisher;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.Metrics;
import reactor.util.annotation.Nullable;

final class FluxMetricsFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final String name;
   final Tags tags;
   final MeterRegistry registryCandidate;

   FluxMetricsFuseable(Flux<? extends T> flux) {
      super(flux);
      this.name = FluxMetrics.resolveName(flux);
      this.tags = FluxMetrics.resolveTags(flux, FluxMetrics.DEFAULT_TAGS_FLUX);
      this.registryCandidate = Metrics.MicrometerConfiguration.getRegistry();
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxMetricsFuseable.MetricsFuseableSubscriber<>(actual, this.registryCandidate, Clock.SYSTEM, this.name, this.tags);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MetricsFuseableSubscriber<T> extends FluxMetrics.MetricsSubscriber<T> implements Fuseable, Fuseable.QueueSubscription<T> {
      int mode;
      @Nullable
      Fuseable.QueueSubscription<T> qs;

      MetricsFuseableSubscriber(CoreSubscriber<? super T> actual, MeterRegistry registry, Clock clock, String sequenceName, Tags sequenceTags) {
         super(actual, registry, clock, sequenceName, sequenceTags);
      }

      public void clear() {
         if (this.qs != null) {
            this.qs.clear();
         }

      }

      public boolean isEmpty() {
         return this.qs == null || this.qs.isEmpty();
      }

      @Override
      public void onNext(T t) {
         if (this.mode == 2) {
            this.actual.onNext((T)null);
         } else if (this.done) {
            FluxMetrics.recordMalformed(this.sequenceName, this.commonTags, this.registry);
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long last = this.lastNextEventNanos;
            this.lastNextEventNanos = this.clock.monotonicTime();
            this.onNextIntervalTimer.record(this.lastNextEventNanos - last, TimeUnit.NANOSECONDS);
            this.actual.onNext(t);
         }
      }

      @Nullable
      public T poll() {
         if (this.qs == null) {
            return null;
         } else {
            try {
               T v = (T)this.qs.poll();
               if (v == null && this.mode == 1) {
                  if (this.onNextIntervalTimer.count() == 0L) {
                     FluxMetrics.recordOnCompleteEmpty(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample);
                  } else {
                     FluxMetrics.recordOnComplete(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample);
                  }
               }

               if (v != null) {
                  long last = this.lastNextEventNanos;
                  this.lastNextEventNanos = this.clock.monotonicTime();
                  this.onNextIntervalTimer.record(this.lastNextEventNanos - last, TimeUnit.NANOSECONDS);
               }

               return v;
            } catch (Throwable var4) {
               FluxMetrics.recordOnError(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample, var4);
               throw var4;
            }
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            FluxMetrics.recordOnSubscribe(this.sequenceName, this.commonTags, this.registry);
            this.subscribeToTerminateSample = Timer.start(this.clock);
            this.lastNextEventNanos = this.clock.monotonicTime();
            this.qs = Operators.as(s);
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public int requestFusion(int mode) {
         if (this.qs != null) {
            this.mode = this.qs.requestFusion(mode);
            return this.mode;
         } else {
            return 0;
         }
      }

      public int size() {
         return this.qs == null ? 0 : this.qs.size();
      }
   }
}
