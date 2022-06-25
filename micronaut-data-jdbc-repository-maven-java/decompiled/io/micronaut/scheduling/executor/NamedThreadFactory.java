package io.micronaut.scheduling.executor;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.ArgumentUtils;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Internal
class NamedThreadFactory implements ThreadFactory {
   private final ThreadGroup group;
   private final AtomicInteger threadNumber = new AtomicInteger(1);
   private final String namePrefix;

   NamedThreadFactory(String name) {
      ArgumentUtils.check("name", name).notNull();
      SecurityManager s = System.getSecurityManager();
      this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      this.namePrefix = name + "-thread-";
   }

   public Thread newThread(Runnable runnable) {
      Thread newThread = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      if (newThread.isDaemon()) {
         newThread.setDaemon(false);
      }

      if (newThread.getPriority() != 5) {
         newThread.setPriority(5);
      }

      return newThread;
   }
}
