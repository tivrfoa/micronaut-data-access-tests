package io.micronaut.management.endpoint.info.source;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
class $ConfigurationInfoSource$Definition$Intercepted extends ConfigurationInfoSource implements InterceptedProxy {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods;
   private final BeanLocator $beanLocator;
   private Qualifier $beanQualifier;
   private BeanResolutionContext $beanResolutionContext;

   @Override
   public Publisher getSource() {
      ExecutableMethod var1 = this.$proxyMethods[0];
      Interceptor[] var2 = this.$interceptors[0];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this.interceptedTarget(), var1);
      return (Publisher)var3.proceed();
   }

   public $ConfigurationInfoSource$Definition$Intercepted(Environment var1, BeanResolutionContext var2, BeanContext var3, Qualifier var4, List var5) {
      super(var1);
      this.$beanLocator = var3;
      this.$beanQualifier = var4;
      this.$beanResolutionContext = var2.copy();
      this.$proxyMethods = new ExecutableMethod[1];
      this.$interceptors = new Interceptor[1][];
      this.$proxyMethods[0] = var3.getProxyTargetMethod(
         Argument.of(ConfigurationInfoSource.class, $ConfigurationInfoSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]),
         var4,
         "getSource"
      );
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[0], var5);
   }

   @Override
   public void $withBeanQualifier(Qualifier var1) {
      this.$beanQualifier = var1;
   }

   @Override
   public Object interceptedTarget() {
      return (ConfigurationInfoSource)((DefaultBeanContext)this.$beanLocator)
         .getProxyTargetBean(
            this.$beanResolutionContext,
            Argument.of(ConfigurationInfoSource.class, $ConfigurationInfoSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]),
            this.$beanQualifier
         );
   }
}
