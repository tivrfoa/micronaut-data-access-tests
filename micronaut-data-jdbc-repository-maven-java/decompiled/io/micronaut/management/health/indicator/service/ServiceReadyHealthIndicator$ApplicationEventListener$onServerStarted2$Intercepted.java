package io.micronaut.management.health.indicator.service;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;

// $FF: synthetic class
@Generated
class ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted implements ApplicationEventListener, Introduced {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[1];

   @Override
   public void onApplicationEvent(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[0];
      Interceptor[] var3 = this.$interceptors[0];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted(
      BeanResolutionContext var1, BeanContext var2, Qualifier var3, List var4
   ) {
      this.$interceptors = new Interceptor[1][];
      $ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted$Definition$Exec var5 = new $ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted$Definition$Exec(
         
      );
      this.$proxyMethods[0] = var5.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[0], var4);
   }
}
