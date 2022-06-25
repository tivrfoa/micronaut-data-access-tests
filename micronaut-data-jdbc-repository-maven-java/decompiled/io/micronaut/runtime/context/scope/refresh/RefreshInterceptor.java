package io.micronaut.runtime.context.scope.refresh;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import java.util.concurrent.locks.Lock;

@Singleton
@Requires(
   notEnv = {"function", "android"}
)
public class RefreshInterceptor implements MethodInterceptor {
   private final RefreshScope refreshScope;

   public RefreshInterceptor(RefreshScope refreshScope) {
      this.refreshScope = refreshScope;
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext context) {
      Object target = context.getTarget();
      Lock lock = this.refreshScope.getLock(target).readLock();

      Object var4;
      try {
         lock.lock();
         var4 = context.proceed();
      } finally {
         lock.unlock();
      }

      return var4;
   }
}
