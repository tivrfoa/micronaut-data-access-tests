package reactor.core.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.search.Search;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.util.Metrics;

final class SchedulerMetricDecorator implements BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService>, Disposable {
   static final String TAG_SCHEDULER_ID = "reactor.scheduler.id";
   static final String METRICS_DECORATOR_KEY = "reactor.metrics.decorator";
   final WeakHashMap<Scheduler, String> seenSchedulers = new WeakHashMap();
   final Map<String, AtomicInteger> schedulerDifferentiator = new HashMap();
   final WeakHashMap<Scheduler, AtomicInteger> executorDifferentiator = new WeakHashMap();
   final MeterRegistry registry = Metrics.MicrometerConfiguration.getRegistry();

   public synchronized ScheduledExecutorService apply(Scheduler scheduler, final ScheduledExecutorService service) {
      String schedulerName = Scannable.from(scheduler).scanOrDefault(Scannable.Attr.NAME, scheduler.getClass().getName());
      String schedulerId = (String)this.seenSchedulers
         .computeIfAbsent(
            scheduler,
            s -> {
               int schedulerDifferentiator = ((AtomicInteger)this.schedulerDifferentiator.computeIfAbsent(schedulerName, k -> new AtomicInteger(0)))
                  .getAndIncrement();
               return schedulerDifferentiator == 0 ? schedulerName : schedulerName + "#" + schedulerDifferentiator;
            }
         );
      final String executorId = schedulerId
         + "-"
         + ((AtomicInteger)this.executorDifferentiator.computeIfAbsent(scheduler, key -> new AtomicInteger(0))).getAndIncrement();
      final Tags tags = Tags.of("reactor.scheduler.id", schedulerId);

      class MetricsRemovingScheduledExecutorService extends DelegatingScheduledExecutorService {
         MetricsRemovingScheduledExecutorService() {
            super(ExecutorServiceMetrics.monitor(SchedulerMetricDecorator.this.registry, service, executorId, tags));
         }

         @Override
         public List<Runnable> shutdownNow() {
            this.removeMetrics();
            return super.shutdownNow();
         }

         @Override
         public void shutdown() {
            this.removeMetrics();
            super.shutdown();
         }

         void removeMetrics() {
            Search.in(SchedulerMetricDecorator.this.registry).tag("name", executorId).meters().forEach(SchedulerMetricDecorator.this.registry::remove);
         }
      }

      return new MetricsRemovingScheduledExecutorService();
   }

   @Override
   public void dispose() {
      Search.in(this.registry).tagKeys(new String[]{"reactor.scheduler.id"}).meters().forEach(this.registry::remove);
      this.seenSchedulers.clear();
      this.schedulerDifferentiator.clear();
      this.executorDifferentiator.clear();
   }
}
