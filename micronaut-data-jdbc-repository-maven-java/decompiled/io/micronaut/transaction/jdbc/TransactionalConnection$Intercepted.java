package io.micronaut.transaction.jdbc;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.Introduced;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.ExecutableMethod;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

// $FF: synthetic class
@Generated
class TransactionalConnection$Intercepted implements TransactionalConnection, Introduced {
   private final Interceptor[][] $interceptors;
   private final ExecutableMethod[] $proxyMethods = new ExecutableMethod[53];

   public Statement createStatement() {
      ExecutableMethod var1 = this.$proxyMethods[0];
      Interceptor[] var2 = this.$interceptors[0];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Statement)var3.proceed();
   }

   public PreparedStatement prepareStatement(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[1];
      Interceptor[] var3 = this.$interceptors[1];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (PreparedStatement)var4.proceed();
   }

   public CallableStatement prepareCall(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[2];
      Interceptor[] var3 = this.$interceptors[2];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (CallableStatement)var4.proceed();
   }

   public String nativeSQL(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[3];
      Interceptor[] var3 = this.$interceptors[3];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (String)var4.proceed();
   }

   public void setAutoCommit(boolean var1) {
      ExecutableMethod var2 = this.$proxyMethods[4];
      Interceptor[] var3 = this.$interceptors[4];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public boolean getAutoCommit() {
      ExecutableMethod var1 = this.$proxyMethods[5];
      Interceptor[] var2 = this.$interceptors[5];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public void commit() {
      ExecutableMethod var1 = this.$proxyMethods[6];
      Interceptor[] var2 = this.$interceptors[6];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public void rollback() {
      ExecutableMethod var1 = this.$proxyMethods[7];
      Interceptor[] var2 = this.$interceptors[7];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public void close() {
      ExecutableMethod var1 = this.$proxyMethods[8];
      Interceptor[] var2 = this.$interceptors[8];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public boolean isClosed() {
      ExecutableMethod var1 = this.$proxyMethods[9];
      Interceptor[] var2 = this.$interceptors[9];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public DatabaseMetaData getMetaData() {
      ExecutableMethod var1 = this.$proxyMethods[10];
      Interceptor[] var2 = this.$interceptors[10];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (DatabaseMetaData)var3.proceed();
   }

   public void setReadOnly(boolean var1) {
      ExecutableMethod var2 = this.$proxyMethods[11];
      Interceptor[] var3 = this.$interceptors[11];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public boolean isReadOnly() {
      ExecutableMethod var1 = this.$proxyMethods[12];
      Interceptor[] var2 = this.$interceptors[12];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public void setCatalog(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[13];
      Interceptor[] var3 = this.$interceptors[13];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public String getCatalog() {
      ExecutableMethod var1 = this.$proxyMethods[14];
      Interceptor[] var2 = this.$interceptors[14];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (String)var3.proceed();
   }

   public void setTransactionIsolation(int var1) {
      ExecutableMethod var2 = this.$proxyMethods[15];
      Interceptor[] var3 = this.$interceptors[15];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public int getTransactionIsolation() {
      ExecutableMethod var1 = this.$proxyMethods[16];
      Interceptor[] var2 = this.$interceptors[16];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public SQLWarning getWarnings() {
      ExecutableMethod var1 = this.$proxyMethods[17];
      Interceptor[] var2 = this.$interceptors[17];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (SQLWarning)var3.proceed();
   }

   public void clearWarnings() {
      ExecutableMethod var1 = this.$proxyMethods[18];
      Interceptor[] var2 = this.$interceptors[18];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      var3.proceed();
   }

   public Statement createStatement(int var1, int var2) {
      ExecutableMethod var3 = this.$proxyMethods[19];
      Interceptor[] var4 = this.$interceptors[19];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (Statement)var5.proceed();
   }

   public PreparedStatement prepareStatement(String var1, int var2, int var3) {
      ExecutableMethod var4 = this.$proxyMethods[20];
      Interceptor[] var5 = this.$interceptors[20];
      MethodInterceptorChain var6 = new MethodInterceptorChain<>(var5, this, var4, var1, var2, var3);
      return (PreparedStatement)var6.proceed();
   }

   public CallableStatement prepareCall(String var1, int var2, int var3) {
      ExecutableMethod var4 = this.$proxyMethods[21];
      Interceptor[] var5 = this.$interceptors[21];
      MethodInterceptorChain var6 = new MethodInterceptorChain<>(var5, this, var4, var1, var2, var3);
      return (CallableStatement)var6.proceed();
   }

   public Map getTypeMap() {
      ExecutableMethod var1 = this.$proxyMethods[22];
      Interceptor[] var2 = this.$interceptors[22];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Map)var3.proceed();
   }

   public void setTypeMap(Map var1) {
      ExecutableMethod var2 = this.$proxyMethods[23];
      Interceptor[] var3 = this.$interceptors[23];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public void setHoldability(int var1) {
      ExecutableMethod var2 = this.$proxyMethods[24];
      Interceptor[] var3 = this.$interceptors[24];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public int getHoldability() {
      ExecutableMethod var1 = this.$proxyMethods[25];
      Interceptor[] var2 = this.$interceptors[25];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public Savepoint setSavepoint() {
      ExecutableMethod var1 = this.$proxyMethods[26];
      Interceptor[] var2 = this.$interceptors[26];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Savepoint)var3.proceed();
   }

   public Savepoint setSavepoint(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[27];
      Interceptor[] var3 = this.$interceptors[27];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (Savepoint)var4.proceed();
   }

   public void rollback(Savepoint var1) {
      ExecutableMethod var2 = this.$proxyMethods[28];
      Interceptor[] var3 = this.$interceptors[28];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public void releaseSavepoint(Savepoint var1) {
      ExecutableMethod var2 = this.$proxyMethods[29];
      Interceptor[] var3 = this.$interceptors[29];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public Statement createStatement(int var1, int var2, int var3) {
      ExecutableMethod var4 = this.$proxyMethods[30];
      Interceptor[] var5 = this.$interceptors[30];
      MethodInterceptorChain var6 = new MethodInterceptorChain<>(var5, this, var4, var1, var2, var3);
      return (Statement)var6.proceed();
   }

   public PreparedStatement prepareStatement(String var1, int var2, int var3, int var4) {
      ExecutableMethod var5 = this.$proxyMethods[31];
      Interceptor[] var6 = this.$interceptors[31];
      MethodInterceptorChain var7 = new MethodInterceptorChain<>(var6, this, var5, var1, var2, var3, var4);
      return (PreparedStatement)var7.proceed();
   }

   public CallableStatement prepareCall(String var1, int var2, int var3, int var4) {
      ExecutableMethod var5 = this.$proxyMethods[32];
      Interceptor[] var6 = this.$interceptors[32];
      MethodInterceptorChain var7 = new MethodInterceptorChain<>(var6, this, var5, var1, var2, var3, var4);
      return (CallableStatement)var7.proceed();
   }

   public PreparedStatement prepareStatement(String var1, int var2) {
      ExecutableMethod var3 = this.$proxyMethods[33];
      Interceptor[] var4 = this.$interceptors[33];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (PreparedStatement)var5.proceed();
   }

   public PreparedStatement prepareStatement(String var1, String[] var2) {
      ExecutableMethod var3 = this.$proxyMethods[34];
      Interceptor[] var4 = this.$interceptors[34];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (PreparedStatement)var5.proceed();
   }

   public Clob createClob() {
      ExecutableMethod var1 = this.$proxyMethods[35];
      Interceptor[] var2 = this.$interceptors[35];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Clob)var3.proceed();
   }

   public Blob createBlob() {
      ExecutableMethod var1 = this.$proxyMethods[36];
      Interceptor[] var2 = this.$interceptors[36];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Blob)var3.proceed();
   }

   public NClob createNClob() {
      ExecutableMethod var1 = this.$proxyMethods[37];
      Interceptor[] var2 = this.$interceptors[37];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (NClob)var3.proceed();
   }

   public SQLXML createSQLXML() {
      ExecutableMethod var1 = this.$proxyMethods[38];
      Interceptor[] var2 = this.$interceptors[38];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (SQLXML)var3.proceed();
   }

   public boolean isValid(int var1) {
      ExecutableMethod var2 = this.$proxyMethods[39];
      Interceptor[] var3 = this.$interceptors[39];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   public void setClientInfo(String var1, String var2) {
      ExecutableMethod var3 = this.$proxyMethods[40];
      Interceptor[] var4 = this.$interceptors[40];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      var5.proceed();
   }

   public void setClientInfo(Properties var1) {
      ExecutableMethod var2 = this.$proxyMethods[41];
      Interceptor[] var3 = this.$interceptors[41];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public String getClientInfo(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[42];
      Interceptor[] var3 = this.$interceptors[42];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return (String)var4.proceed();
   }

   public Properties getClientInfo() {
      ExecutableMethod var1 = this.$proxyMethods[43];
      Interceptor[] var2 = this.$interceptors[43];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (Properties)var3.proceed();
   }

   public Array createArrayOf(String var1, Object[] var2) {
      ExecutableMethod var3 = this.$proxyMethods[44];
      Interceptor[] var4 = this.$interceptors[44];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (Array)var5.proceed();
   }

   public Struct createStruct(String var1, Object[] var2) {
      ExecutableMethod var3 = this.$proxyMethods[45];
      Interceptor[] var4 = this.$interceptors[45];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      return (Struct)var5.proceed();
   }

   public void setSchema(String var1) {
      ExecutableMethod var2 = this.$proxyMethods[46];
      Interceptor[] var3 = this.$interceptors[46];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public String getSchema() {
      ExecutableMethod var1 = this.$proxyMethods[47];
      Interceptor[] var2 = this.$interceptors[47];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return (String)var3.proceed();
   }

   public void abort(Executor var1) {
      ExecutableMethod var2 = this.$proxyMethods[48];
      Interceptor[] var3 = this.$interceptors[48];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      var4.proceed();
   }

   public void setNetworkTimeout(Executor var1, int var2) {
      ExecutableMethod var3 = this.$proxyMethods[49];
      Interceptor[] var4 = this.$interceptors[49];
      MethodInterceptorChain var5 = new MethodInterceptorChain<>(var4, this, var3, var1, var2);
      var5.proceed();
   }

   public int getNetworkTimeout() {
      ExecutableMethod var1 = this.$proxyMethods[50];
      Interceptor[] var2 = this.$interceptors[50];
      MethodInterceptorChain var3 = new MethodInterceptorChain<>(var2, this, var1);
      return var3.proceed();
   }

   public Object unwrap(Class var1) {
      ExecutableMethod var2 = this.$proxyMethods[51];
      Interceptor[] var3 = this.$interceptors[51];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   public boolean isWrapperFor(Class var1) {
      ExecutableMethod var2 = this.$proxyMethods[52];
      Interceptor[] var3 = this.$interceptors[52];
      MethodInterceptorChain var4 = new MethodInterceptorChain<>(var3, this, var2, var1);
      return var4.proceed();
   }

   public TransactionalConnection$Intercepted(BeanResolutionContext var1, BeanContext var2, Qualifier var3, List var4) {
      this.$interceptors = new Interceptor[53][];
      $TransactionalConnection$Intercepted$Definition$Exec var5 = new $TransactionalConnection$Intercepted$Definition$Exec();
      this.$proxyMethods[0] = var5.getExecutableMethodByIndex(0);
      this.$interceptors[0] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[0], var4);
      this.$proxyMethods[1] = var5.getExecutableMethodByIndex(1);
      this.$interceptors[1] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[1], var4);
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
      this.$proxyMethods[17] = var5.getExecutableMethodByIndex(17);
      this.$interceptors[17] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[17], var4);
      this.$proxyMethods[18] = var5.getExecutableMethodByIndex(18);
      this.$interceptors[18] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[18], var4);
      this.$proxyMethods[19] = var5.getExecutableMethodByIndex(19);
      this.$interceptors[19] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[19], var4);
      this.$proxyMethods[20] = var5.getExecutableMethodByIndex(20);
      this.$interceptors[20] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[20], var4);
      this.$proxyMethods[21] = var5.getExecutableMethodByIndex(21);
      this.$interceptors[21] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[21], var4);
      this.$proxyMethods[22] = var5.getExecutableMethodByIndex(22);
      this.$interceptors[22] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[22], var4);
      this.$proxyMethods[23] = var5.getExecutableMethodByIndex(23);
      this.$interceptors[23] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[23], var4);
      this.$proxyMethods[24] = var5.getExecutableMethodByIndex(24);
      this.$interceptors[24] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[24], var4);
      this.$proxyMethods[25] = var5.getExecutableMethodByIndex(25);
      this.$interceptors[25] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[25], var4);
      this.$proxyMethods[26] = var5.getExecutableMethodByIndex(26);
      this.$interceptors[26] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[26], var4);
      this.$proxyMethods[27] = var5.getExecutableMethodByIndex(27);
      this.$interceptors[27] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[27], var4);
      this.$proxyMethods[28] = var5.getExecutableMethodByIndex(28);
      this.$interceptors[28] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[28], var4);
      this.$proxyMethods[29] = var5.getExecutableMethodByIndex(29);
      this.$interceptors[29] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[29], var4);
      this.$proxyMethods[30] = var5.getExecutableMethodByIndex(30);
      this.$interceptors[30] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[30], var4);
      this.$proxyMethods[31] = var5.getExecutableMethodByIndex(31);
      this.$interceptors[31] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[31], var4);
      this.$proxyMethods[32] = var5.getExecutableMethodByIndex(32);
      this.$interceptors[32] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[32], var4);
      this.$proxyMethods[33] = var5.getExecutableMethodByIndex(33);
      this.$interceptors[33] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[33], var4);
      this.$proxyMethods[34] = var5.getExecutableMethodByIndex(34);
      this.$interceptors[34] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[34], var4);
      this.$proxyMethods[35] = var5.getExecutableMethodByIndex(35);
      this.$interceptors[35] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[35], var4);
      this.$proxyMethods[36] = var5.getExecutableMethodByIndex(36);
      this.$interceptors[36] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[36], var4);
      this.$proxyMethods[37] = var5.getExecutableMethodByIndex(37);
      this.$interceptors[37] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[37], var4);
      this.$proxyMethods[38] = var5.getExecutableMethodByIndex(38);
      this.$interceptors[38] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[38], var4);
      this.$proxyMethods[39] = var5.getExecutableMethodByIndex(39);
      this.$interceptors[39] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[39], var4);
      this.$proxyMethods[40] = var5.getExecutableMethodByIndex(40);
      this.$interceptors[40] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[40], var4);
      this.$proxyMethods[41] = var5.getExecutableMethodByIndex(41);
      this.$interceptors[41] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[41], var4);
      this.$proxyMethods[42] = var5.getExecutableMethodByIndex(42);
      this.$interceptors[42] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[42], var4);
      this.$proxyMethods[43] = var5.getExecutableMethodByIndex(43);
      this.$interceptors[43] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[43], var4);
      this.$proxyMethods[44] = var5.getExecutableMethodByIndex(44);
      this.$interceptors[44] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[44], var4);
      this.$proxyMethods[45] = var5.getExecutableMethodByIndex(45);
      this.$interceptors[45] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[45], var4);
      this.$proxyMethods[46] = var5.getExecutableMethodByIndex(46);
      this.$interceptors[46] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[46], var4);
      this.$proxyMethods[47] = var5.getExecutableMethodByIndex(47);
      this.$interceptors[47] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[47], var4);
      this.$proxyMethods[48] = var5.getExecutableMethodByIndex(48);
      this.$interceptors[48] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[48], var4);
      this.$proxyMethods[49] = var5.getExecutableMethodByIndex(49);
      this.$interceptors[49] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[49], var4);
      this.$proxyMethods[50] = var5.getExecutableMethodByIndex(50);
      this.$interceptors[50] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[50], var4);
      this.$proxyMethods[51] = var5.getExecutableMethodByIndex(51);
      this.$interceptors[51] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[51], var4);
      this.$proxyMethods[52] = var5.getExecutableMethodByIndex(52);
      this.$interceptors[52] = InterceptorChain.resolveIntroductionInterceptors(var2, this.$proxyMethods[52], var4);
   }
}
