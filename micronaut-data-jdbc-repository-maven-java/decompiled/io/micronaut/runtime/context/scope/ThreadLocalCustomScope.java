package io.micronaut.runtime.context.scope;

import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanIdentifier;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
final class ThreadLocalCustomScope extends AbstractConcurrentCustomScope<ThreadLocal> {
   private final java.lang.ThreadLocal<Map<BeanIdentifier, CreatedBean<?>>> threadScope = java.lang.ThreadLocal.withInitial(HashMap::new);

   protected ThreadLocalCustomScope() {
      super(ThreadLocal.class);
   }

   @NonNull
   @Override
   protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
      return (Map<BeanIdentifier, CreatedBean<?>>)this.threadScope.get();
   }

   @Override
   public boolean isRunning() {
      return true;
   }

   public ThreadLocalCustomScope start() {
      return this;
   }

   @Override
   public void close() {
      this.threadScope.remove();
   }
}
