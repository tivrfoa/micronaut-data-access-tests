package com.example;

import io.micronaut.aop.Intercepted;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Generated;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;

// $FF: synthetic class
@Generated
class $GenreController$Definition$Intercepted extends GenreController implements Intercepted {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[4];

   @Override
   public HttpResponse update(GenreUpdateCommand var1) {
      ExecutableMethod var2 = this.$proxyMethods[0];
      Interceptor[] var3 = this.$interceptors[0];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (HttpResponse)var4.proceed();
   }

   @Override
   public List list(Pageable var1) {
      ExecutableMethod var2 = this.$proxyMethods[1];
      Interceptor[] var3 = this.$interceptors[1];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (List)var4.proceed();
   }

   @Override
   public HttpResponse save(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[2];
      Interceptor[] var3 = this.$interceptors[2];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (HttpResponse)var4.proceed();
   }

   @Override
   public HttpResponse saveExceptions(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[3];
      Interceptor[] var3 = this.$interceptors[3];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (HttpResponse)var4.proceed();
   }

   public $GenreController$Definition$Intercepted(GenreRepository var1, BeanResolutionContext var2, BeanContext var3, Qualifier var4, List var5) {
      super(var1);
      this.$interceptors = new Interceptor[4][];
      $GenreController$Definition$Exec var6 = new $GenreController$Definition$Exec(true);
      this.$proxyMethods[0] = var6.getExecutableMethodByIndex(1);
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[0], var5);
      this.$proxyMethods[1] = var6.getExecutableMethodByIndex(2);
      this.$interceptors[1] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[1], var5);
      this.$proxyMethods[2] = var6.getExecutableMethodByIndex(3);
      this.$interceptors[2] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[2], var5);
      this.$proxyMethods[3] = var6.getExecutableMethodByIndex(4);
      this.$interceptors[3] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[3], var5);
   }
}
