package reactor.core.publisher;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.Metrics;
import reactor.util.function.Tuple2;

final class FluxMetrics<T> extends InternalFluxOperator<T, T> {
   final String name;
   final Tags tags;
   final MeterRegistry registryCandidate;
   static final String REACTOR_DEFAULT_NAME = "reactor";
   static final String METER_MALFORMED = ".malformed.source";
   static final String METER_SUBSCRIBED = ".subscribed";
   static final String METER_FLOW_DURATION = ".flow.duration";
   static final String METER_ON_NEXT_DELAY = ".onNext.delay";
   static final String METER_REQUESTED = ".requested";
   static final String TAG_KEY_EXCEPTION = "exception";
   static final Tags DEFAULT_TAGS_FLUX = Tags.of("type", "Flux");
   static final Tags DEFAULT_TAGS_MONO = Tags.of("type", "Mono");
   static final Tag TAG_ON_ERROR = Tag.of("status", "error");
   static final Tags TAG_ON_COMPLETE = Tags.of(new String[]{"status", "completed", "exception", ""});
   static final Tags TAG_ON_COMPLETE_EMPTY = Tags.of(new String[]{"status", "completedEmpty", "exception", ""});
   static final Tags TAG_CANCEL = Tags.of(new String[]{"status", "cancelled", "exception", ""});
   static final Logger log = Loggers.getLogger(FluxMetrics.class);
   static final BiFunction<Tags, Tuple2<String, String>, Tags> TAG_ACCUMULATOR = (prev, tuple) -> prev.and(
         new Tag[]{Tag.of((String)tuple.getT1(), (String)tuple.getT2())}
      );
   static final BinaryOperator<Tags> TAG_COMBINER = Tags::and;

   FluxMetrics(Flux<? extends T> flux) {
      super(flux);
      this.name = resolveName(flux);
      this.tags = resolveTags(flux, DEFAULT_TAGS_FLUX);
      this.registryCandidate = Metrics.MicrometerConfiguration.getRegistry();
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxMetrics.MetricsSubscriber<>(actual, this.registryCandidate, Clock.SYSTEM, this.name, this.tags);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static String resolveName(Publisher<?> source) {
      Scannable scannable = Scannable.from(source);
      if (scannable.isScanAvailable()) {
         String nameOrDefault = scannable.name();
         return scannable.stepName().equals(nameOrDefault) ? "reactor" : nameOrDefault;
      } else {
         log.warn(
            "Attempting to activate metrics but the upstream is not Scannable. You might want to use `name()` (and optionally `tags()`) right before `metrics()`"
         );
         return "reactor";
      }
   }

   static Tags resolveTags(Publisher<?> source, Tags tags) {
      Scannable scannable = Scannable.from(source);
      if (scannable.isScanAvailable()) {
         LinkedList<Tuple2<String, String>> scannableTags = new LinkedList();
         scannable.tags().forEach(scannableTags::push);
         return (Tags)scannableTags.stream().reduce(tags, TAG_ACCUMULATOR, TAG_COMBINER);
      } else {
         return tags;
      }
   }

   static void recordCancel(String name, Tags commonTags, MeterRegistry registry, Sample flowDuration) {
      Timer timer = Timer.builder(name + ".flow.duration")
         .tags(commonTags.and(TAG_CANCEL))
         .description("Times the duration elapsed between a subscription and the cancellation of the sequence")
         .register(registry);
      flowDuration.stop(timer);
   }

   static void recordMalformed(String name, Tags commonTags, MeterRegistry registry) {
      registry.counter(name + ".malformed.source", commonTags).increment();
   }

   static void recordOnError(String name, Tags commonTags, MeterRegistry registry, Sample flowDuration, Throwable e) {
      Timer timer = Timer.builder(name + ".flow.duration")
         .tags(commonTags.and(new Tag[]{TAG_ON_ERROR}))
         .tag("exception", e.getClass().getName())
         .description("Times the duration elapsed between a subscription and the onError termination of the sequence, with the exception name as a tag.")
         .register(registry);
      flowDuration.stop(timer);
   }

   static void recordOnComplete(String name, Tags commonTags, MeterRegistry registry, Sample flowDuration) {
      Timer timer = Timer.builder(name + ".flow.duration")
         .tags(commonTags.and(TAG_ON_COMPLETE))
         .description("Times the duration elapsed between a subscription and the onComplete termination of a sequence that did emit some elements")
         .register(registry);
      flowDuration.stop(timer);
   }

   static void recordOnCompleteEmpty(String name, Tags commonTags, MeterRegistry registry, Sample flowDuration) {
      Timer timer = Timer.builder(name + ".flow.duration")
         .tags(commonTags.and(TAG_ON_COMPLETE_EMPTY))
         .description("Times the duration elapsed between a subscription and the onComplete termination of a sequence that didn't emit any element")
         .register(registry);
      flowDuration.stop(timer);
   }

   static void recordOnSubscribe(String name, Tags commonTags, MeterRegistry registry) {
      Counter.builder(name + ".subscribed")
         .tags(commonTags)
         .description("Counts how many Reactor sequences have been subscribed to")
         .register(registry)
         .increment();
   }

   static class MetricsSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Clock clock;
      final String sequenceName;
      final Tags commonTags;
      final MeterRegistry registry;
      final DistributionSummary requestedCounter;
      final Timer onNextIntervalTimer;
      Sample subscribeToTerminateSample;
      long lastNextEventNanos = -1L;
      boolean done;
      Subscription s;

      MetricsSubscriber(CoreSubscriber<? super T> actual, MeterRegistry registry, Clock clock, String sequenceName, Tags commonTags) {
         this.actual = actual;
         this.clock = clock;
         this.sequenceName = sequenceName;
         this.commonTags = commonTags;
         this.registry = registry;
         this.onNextIntervalTimer = Timer.builder(sequenceName + ".onNext.delay")
            .tags(commonTags)
            .description("Measures delays between onNext signals (or between onSubscribe and first onNext)")
            .register(registry);
         if (!"reactor".equals(sequenceName)) {
            this.requestedCounter = DistributionSummary.builder(sequenceName + ".requested")
               .tags(commonTags)
               .description("Counts the amount requested to a named Flux by all subscribers, until at least one requests an unbounded amount")
               .register(registry);
         } else {
            this.requestedCounter = null;
         }

      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public final void cancel() {
         FluxMetrics.recordCancel(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample);
         this.s.cancel();
      }

      @Override
      public final void onComplete() {
         if (!this.done) {
            this.done = true;
            if (this.onNextIntervalTimer.count() == 0L) {
               FluxMetrics.recordOnCompleteEmpty(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample);
            } else {
               FluxMetrics.recordOnComplete(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample);
            }

            this.actual.onComplete();
         }
      }

      @Override
      public final void onError(Throwable e) {
         if (this.done) {
            FluxMetrics.recordMalformed(this.sequenceName, this.commonTags, this.registry);
            Operators.onErrorDropped(e, this.actual.currentContext());
         } else {
            this.done = true;
            FluxMetrics.recordOnError(this.sequenceName, this.commonTags, this.registry, this.subscribeToTerminateSample, e);
            this.actual.onError(e);
         }
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            FluxMetrics.recordMalformed(this.sequenceName, this.commonTags, this.registry);
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long last = this.lastNextEventNanos;
            this.lastNextEventNanos = this.clock.monotonicTime();
            this.onNextIntervalTimer.record(this.lastNextEventNanos - last, TimeUnit.NANOSECONDS);
            this.actual.onNext(t);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            FluxMetrics.recordOnSubscribe(this.sequenceName, this.commonTags, this.registry);
            this.subscribeToTerminateSample = Timer.start(this.clock);
            this.lastNextEventNanos = this.clock.monotonicTime();
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public final void request(long l) {
         if (Operators.validate(l)) {
            if (this.requestedCounter != null) {
               this.requestedCounter.record((double)l);
            }

            this.s.request(l);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
      }
   }
}
