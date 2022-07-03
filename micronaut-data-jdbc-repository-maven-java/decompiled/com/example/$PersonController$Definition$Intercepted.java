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
import java.sql.Timestamp;
import java.util.List;

// $FF: synthetic class
@Generated
class $PersonController$Definition$Intercepted extends PersonController implements Intercepted {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[3];

   @Override
   public List list(Pageable var1) {
      ExecutableMethod var2 = this.$proxyMethods[0];
      Interceptor[] var3 = this.$interceptors[0];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (List)var4.proceed();
   }

   @Override
   public List listWithRelationships(Pageable var1) {
      ExecutableMethod var2 = this.$proxyMethods[1];
      Interceptor[] var3 = this.$interceptors[1];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (List)var4.proceed();
   }

   @Override
   public HttpResponse save(String var1, Timestamp var2) {
      ExecutableMethod var3 = this.$proxyMethods[2];
      Interceptor[] var4 = this.$interceptors[2];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (HttpResponse)var5.proceed();
   }

   public $PersonController$Definition$Intercepted(
      GenreDao var1, PersonRepository var2, BeanResolutionContext var3, BeanContext var4, Qualifier var5, List var6
   ) {
      super(var1, var2);
      this.$interceptors = new Interceptor[3][];
      $PersonController$Definition$Exec var7 = new $PersonController$Definition$Exec(true);
      this.$proxyMethods[0] = var7.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[0], var6);
      this.$proxyMethods[1] = var7.getExecutableMethodByIndex(1);
      this.$interceptors[1] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[1], var6);
      this.$proxyMethods[2] = var7.getExecutableMethodByIndex(3);
      this.$interceptors[2] = InterceptorChain.resolveAroundInterceptors(var4, this.$proxyMethods[2], var6);
   }
}
