package com.example;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Generated;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class GenreDao$Intercepted extends GenreDao implements Introduced {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[13];

   @Override
   public List listGenres() {
      ExecutableMethod var1 = this.$proxyMethods[0];
      Interceptor[] var2 = this.$interceptors[0];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (List)var3.proceed();
   }

   @Override
   public Object save(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[1];
      Interceptor[] var3 = this.$interceptors[1];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Object update(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[2];
      Interceptor[] var3 = this.$interceptors[2];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Iterable updateAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[3];
      Interceptor[] var3 = this.$interceptors[3];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Iterable)var4.proceed();
   }

   @Override
   public Iterable saveAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[4];
      Interceptor[] var3 = this.$interceptors[4];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Iterable)var4.proceed();
   }

   @Override
   public Optional findById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[5];
      Interceptor[] var3 = this.$interceptors[5];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Optional)var4.proceed();
   }

   @Override
   public boolean existsById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[6];
      Interceptor[] var3 = this.$interceptors[6];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Iterable findAll() {
      ExecutableMethod var1 = this.$proxyMethods[7];
      Interceptor[] var2 = this.$interceptors[7];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Iterable)var3.proceed();
   }

   @Override
   public long count() {
      ExecutableMethod var1 = this.$proxyMethods[8];
      Interceptor[] var2 = this.$interceptors[8];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   @Override
   public void deleteById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[9];
      Interceptor[] var3 = this.$interceptors[9];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void delete(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[10];
      Interceptor[] var3 = this.$interceptors[10];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void deleteAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[11];
      Interceptor[] var3 = this.$interceptors[11];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void deleteAll() {
      ExecutableMethod var1 = this.$proxyMethods[12];
      Interceptor[] var2 = this.$interceptors[12];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public GenreDao$Intercepted(JdbcOperations var1, BeanResolutionContext var2, BeanContext var3, Qualifier var4, List var5) {
      super(var1);
      this.$interceptors = new Interceptor[13][];
      $GenreDao$Intercepted$Definition$Exec var6 = new $GenreDao$Intercepted$Definition$Exec(true);
      this.$proxyMethods[0] = var6.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveAroundInterceptors(var3, this.$proxyMethods[0], var5);
      this.$proxyMethods[1] = var6.getExecutableMethodByIndex(1);
      this.$interceptors[1] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[1], var5);
      this.$proxyMethods[2] = var6.getExecutableMethodByIndex(2);
      this.$interceptors[2] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[2], var5);
      this.$proxyMethods[3] = var6.getExecutableMethodByIndex(3);
      this.$interceptors[3] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[3], var5);
      this.$proxyMethods[4] = var6.getExecutableMethodByIndex(4);
      this.$interceptors[4] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[4], var5);
      this.$proxyMethods[5] = var6.getExecutableMethodByIndex(5);
      this.$interceptors[5] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[5], var5);
      this.$proxyMethods[6] = var6.getExecutableMethodByIndex(6);
      this.$interceptors[6] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[6], var5);
      this.$proxyMethods[7] = var6.getExecutableMethodByIndex(7);
      this.$interceptors[7] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[7], var5);
      this.$proxyMethods[8] = var6.getExecutableMethodByIndex(8);
      this.$interceptors[8] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[8], var5);
      this.$proxyMethods[9] = var6.getExecutableMethodByIndex(9);
      this.$interceptors[9] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[9], var5);
      this.$proxyMethods[10] = var6.getExecutableMethodByIndex(10);
      this.$interceptors[10] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[10], var5);
      this.$proxyMethods[11] = var6.getExecutableMethodByIndex(11);
      this.$interceptors[11] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[11], var5);
      this.$proxyMethods[12] = var6.getExecutableMethodByIndex(12);
      this.$interceptors[12] = InterceptorChain.resolveIntroductionInterceptors(var3, this.$proxyMethods[12], var5);
   }
}
