package io.micronaut.http.server.util.locale;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.MessageSource;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.LocaleResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpLocalizedMessageSource$Definition$Intercepted extends HttpLocalizedMessageSource implements InterceptedProxy {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods;
   private final BeanLocator $beanLocator;
   private Qualifier $beanQualifier;
   private BeanResolutionContext $beanResolutionContext;

   @Override
   public Optional getMessage(String var1, Object[] var2) {
      ExecutableMethod var3 = this.$proxyMethods[0];
      Interceptor[] var4 = this.$interceptors[0];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this.interceptedTarget(), var3, var1, var2);
      return (Optional)var5.proceed();
   }

   @Override
   public Optional getMessage(String var1, Map var2) {
      ExecutableMethod var3 = this.$proxyMethods[1];
      Interceptor[] var4 = this.$interceptors[1];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this.interceptedTarget(), var3, var1, var2);
      return (Optional)var5.proceed();
   }

   @Override
   public Optional getMessage(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[2];
      Interceptor[] var3 = this.$interceptors[2];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this.interceptedTarget(), var2, var1);
      return (Optional)var4.proceed();
   }

   @Override
   public void setRequest(HttpRequest var1) {
      ExecutableMethod var2 = this.$proxyMethods[3];
      Interceptor[] var3 = this.$interceptors[3];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this.interceptedTarget(), var2, var1);
      var4.proceed();
   }

   public $HttpLocalizedMessageSource$Definition$Intercepted(
      LocaleResolver var1, MessageSource var2, BeanResolutionContext var3, BeanContext var4, Qualifier var5, List var6
   ) {
      super(var1, var2);
      this.$beanLocator = var4;
      this.$beanQualifier = var5;
      this.$beanResolutionContext = var3.copy();
      this.$proxyMethods = new ExecutableMethod[4];
      this.$interceptors = new Interceptor[4][];
      this.$proxyMethods[0] = var4.getProxyTargetMethod(
         Argument.of(
            HttpLocalizedMessageSource.class, $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]
         ),
         var5,
         "getMessage",
         String.class,
         Object[].class
      );
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[0], var6);
      this.$proxyMethods[1] = var4.getProxyTargetMethod(
         Argument.of(
            HttpLocalizedMessageSource.class, $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]
         ),
         var5,
         "getMessage",
         String.class,
         Map.class
      );
      this.$interceptors[1] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[1], var6);
      this.$proxyMethods[2] = var4.getProxyTargetMethod(
         Argument.of(
            HttpLocalizedMessageSource.class, $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]
         ),
         var5,
         "getMessage",
         String.class
      );
      this.$interceptors[2] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[2], var6);
      this.$proxyMethods[3] = var4.getProxyTargetMethod(
         Argument.of(
            HttpLocalizedMessageSource.class, $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]
         ),
         var5,
         "setRequest",
         HttpRequest.class
      );
      this.$interceptors[3] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[3], var6);
   }

   @Override
   public void $withBeanQualifier(Qualifier var1) {
      this.$beanQualifier = var1;
   }

   @Override
   public Object interceptedTarget() {
      return (HttpLocalizedMessageSource)((DefaultBeanContext)this.$beanLocator)
         .getProxyTargetBean(
            this.$beanResolutionContext,
            Argument.of(
               HttpLocalizedMessageSource.class, $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference.$ANNOTATION_METADATA, new Class[0]
            ),
            this.$beanQualifier
         );
   }
}
