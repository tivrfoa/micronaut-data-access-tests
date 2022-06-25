package reactor.core.scheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;

public final class ReactorBlockHoundIntegration implements BlockHoundIntegration {
   public void applyTo(Builder builder) {
      builder.nonBlockingThreadPredicate(current -> current.or(NonBlocking.class::isInstance));
      builder.allowBlockingCallsInside(ScheduledThreadPoolExecutor.class.getName() + "$DelayedWorkQueue", "offer");
      builder.allowBlockingCallsInside(ScheduledThreadPoolExecutor.class.getName() + "$DelayedWorkQueue", "take");
      builder.allowBlockingCallsInside(BoundedElasticScheduler.class.getName() + "$BoundedScheduledExecutorService", "ensureQueueCapacity");
      builder.allowBlockingCallsInside(SchedulerTask.class.getName(), "dispose");
      builder.allowBlockingCallsInside(ThreadPoolExecutor.class.getName(), "processWorkerExit");
   }
}
