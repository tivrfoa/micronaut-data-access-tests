package com.example;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Generated;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class GenreRepository$Intercepted implements GenreRepository, Introduced {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[17];

   @Override
   public Genre save(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[0];
      Interceptor[] var3 = this.$interceptors[0];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Genre)var4.proceed();
   }

   @Override
   public Genre saveWithException(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[1];
      Interceptor[] var3 = this.$interceptors[1];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Genre)var4.proceed();
   }

   @Override
   public long update(Long var1, String var2) {
      ExecutableMethod var3 = this.$proxyMethods[2];
      Interceptor[] var4 = this.$interceptors[2];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return var5.proceed();
   }

   @Override
   public Iterable findAll(Sort var1) {
      ExecutableMethod var2 = this.$proxyMethods[3];
      Interceptor[] var3 = this.$interceptors[3];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Iterable)var4.proceed();
   }

   @Override
   public Page findAll(Pageable var1) {
      ExecutableMethod var2 = this.$proxyMethods[4];
      Interceptor[] var3 = this.$interceptors[4];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Page)var4.proceed();
   }

   @Override
   public Object save(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[5];
      Interceptor[] var3 = this.$interceptors[5];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Object update(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[6];
      Interceptor[] var3 = this.$interceptors[6];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Iterable updateAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[7];
      Interceptor[] var3 = this.$interceptors[7];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Iterable)var4.proceed();
   }

   @Override
   public Iterable saveAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[8];
      Interceptor[] var3 = this.$interceptors[8];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Iterable)var4.proceed();
   }

   @Override
   public Optional findById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[9];
      Interceptor[] var3 = this.$interceptors[9];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Optional)var4.proceed();
   }

   @Override
   public boolean existsById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[10];
      Interceptor[] var3 = this.$interceptors[10];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   @Override
   public Iterable findAll() {
      ExecutableMethod var1 = this.$proxyMethods[11];
      Interceptor[] var2 = this.$interceptors[11];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Iterable)var3.proceed();
   }

   @Override
   public long count() {
      ExecutableMethod var1 = this.$proxyMethods[12];
      Interceptor[] var2 = this.$interceptors[12];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   @Override
   public void deleteById(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[13];
      Interceptor[] var3 = this.$interceptors[13];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void delete(Object var1) {
      ExecutableMethod var2 = this.$proxyMethods[14];
      Interceptor[] var3 = this.$interceptors[14];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void deleteAll(Iterable var1) {
      ExecutableMethod var2 = this.$proxyMethods[15];
      Interceptor[] var3 = this.$interceptors[15];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   @Override
   public void deleteAll() {
      ExecutableMethod var1 = this.$proxyMethods[16];
      Interceptor[] var2 = this.$interceptors[16];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public GenreRepository$Intercepted(BeanResolutionContext var1, BeanContext var2, Qualifier var3, List var4) {
      this.$interceptors = new Interceptor[17][];
      $GenreRepository$Intercepted$Definition$Exec var5 = new $GenreRepository$Intercepted$Definition$Exec(true);
      this.$proxyMethods[0] = var5.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[0], var4);
      this.$proxyMethods[1] = var5.getExecutableMethodByIndex(1);
      this.$interceptors[1] = InterceptorChain.resolveAroundInterceptors(var2, this.$proxyMethods[1], var4);
      this.$proxyMethods[2] = var5.getExecutableMethodByIndex(2);
      this.$interceptors[2] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[2], var4);
      this.$proxyMethods[3] = var5.getExecutableMethodByIndex(3);
      this.$interceptors[3] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[3], var4);
      this.$proxyMethods[4] = var5.getExecutableMethodByIndex(4);
      this.$interceptors[4] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[4], var4);
      this.$proxyMethods[5] = var5.getExecutableMethodByIndex(5);
      this.$interceptors[5] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[5], var4);
      this.$proxyMethods[6] = var5.getExecutableMethodByIndex(6);
      this.$interceptors[6] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[6], var4);
      this.$proxyMethods[7] = var5.getExecutableMethodByIndex(7);
      this.$interceptors[7] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[7], var4);
      this.$proxyMethods[8] = var5.getExecutableMethodByIndex(8);
      this.$interceptors[8] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[8], var4);
      this.$proxyMethods[9] = var5.getExecutableMethodByIndex(9);
      this.$interceptors[9] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[9], var4);
      this.$proxyMethods[10] = var5.getExecutableMethodByIndex(10);
      this.$interceptors[10] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[10], var4);
      this.$proxyMethods[11] = var5.getExecutableMethodByIndex(11);
      this.$interceptors[11] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[11], var4);
      this.$proxyMethods[12] = var5.getExecutableMethodByIndex(12);
      this.$interceptors[12] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[12], var4);
      this.$proxyMethods[13] = var5.getExecutableMethodByIndex(13);
      this.$interceptors[13] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[13], var4);
      this.$proxyMethods[14] = var5.getExecutableMethodByIndex(14);
      this.$interceptors[14] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[14], var4);
      this.$proxyMethods[15] = var5.getExecutableMethodByIndex(15);
      this.$interceptors[15] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[15], var4);
      this.$proxyMethods[16] = var5.getExecutableMethodByIndex(16);
      this.$interceptors[16] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[16], var4);
   }
}
