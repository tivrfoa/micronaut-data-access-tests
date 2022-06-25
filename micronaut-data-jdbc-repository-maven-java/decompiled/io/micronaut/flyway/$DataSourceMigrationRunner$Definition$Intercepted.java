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
import io.micronaut.jdbc.DataSourceResolver;
import java.util.List;
import org.flywaydb.core.Flyway;

// $FF: synthetic class
@Generated
class $DataSourceMigrationRunner$Definition$Intercepted extends DataSourceMigrationRunner implements Intercepted {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[1];

   @Override
   public void runAsync(FlywayConfigurationProperties var1, Flyway var2) {
      ExecutableMethod var3 = this.$proxyMethods[0];
      Interceptor[] var4 = this.$interceptors[0];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      var5.proceed();
   }

   public $DataSourceMigrationRunner$Definition$Intercepted(
      ApplicationContext var1, ApplicationEventPublisher var2, DataSourceResolver var3, BeanResolutionContext var4, BeanContext var5, Qualifier var6, List var7
   ) {
      super(var1, var2, var3);
      this.$interceptors = new Interceptor[1][];
      $DataSourceMigrationRunner$Definition$Exec var8 = new $DataSourceMigrationRunner$Definition$Exec(true);
      this.$proxyMethods[0] = var8.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var5, this.$proxyMethods[0], var7);
   }
}
