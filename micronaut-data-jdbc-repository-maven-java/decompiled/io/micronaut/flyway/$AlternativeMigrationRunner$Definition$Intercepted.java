package io.micronaut.flyway;

import io.micronaut.aop.Intercepted;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;
import org.flywaydb.core.Flyway;

// $FF: synthetic class
@Generated
class $AlternativeMigrationRunner$Definition$Intercepted extends AlternativeMigrationRunner implements Intercepted {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[1];

   @Override
   public void runAsync(FlywayConfigurationProperties var1, Flyway var2) {
      ExecutableMethod var3 = this.$proxyMethods[0];
      Interceptor[] var4 = this.$interceptors[0];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      var5.proceed();
   }

   public $AlternativeMigrationRunner$Definition$Intercepted(
      ApplicationContext var1, ApplicationEventPublisher var2, BeanResolutionContext var3, BeanContext var4, Qualifier var5, List var6
   ) {
      super(var1, var2);
      this.$interceptors = new Interceptor[1][];
      $AlternativeMigrationRunner$Definition$Exec var7 = new $AlternativeMigrationRunner$Definition$Exec(true);
      this.$proxyMethods[0] = var7.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[0], var6);
   }
}
