package com.zaxxer.hikari.util;

import com.zaxxer.hikari.pool.ProxyCallableStatement;
import com.zaxxer.hikari.pool.ProxyConnection;
import com.zaxxer.hikari.pool.ProxyDatabaseMetaData;
import com.zaxxer.hikari.pool.ProxyPreparedStatement;
import com.zaxxer.hikari.pool.ProxyResultSet;
import com.zaxxer.hikari.pool.ProxyStatement;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public final class JavassistProxyFactory {
   private static ClassPool classPool;
   private static String genDirectory = "";

   public static void main(String... args) throws Exception {
      classPool = new ClassPool();
      classPool.importPackage("java.sql");
      classPool.appendClassPath(new LoaderClassPath(JavassistProxyFactory.class.getClassLoader()));
      if (args.length > 0) {
         genDirectory = args[0];
      }

      String methodBody = "{ try { return delegate.method($$); } catch (SQLException e) { throw checkException(e); } }";
      generateProxyClass(Connection.class, ProxyConnection.class.getName(), methodBody);
      generateProxyClass(Statement.class, ProxyStatement.class.getName(), methodBody);
      generateProxyClass(ResultSet.class, ProxyResultSet.class.getName(), methodBody);
      generateProxyClass(DatabaseMetaData.class, ProxyDatabaseMetaData.class.getName(), methodBody);
      methodBody = "{ try { return ((cast) delegate).method($$); } catch (SQLException e) { throw checkException(e); } }";
      generateProxyClass(PreparedStatement.class, ProxyPreparedStatement.class.getName(), methodBody);
      generateProxyClass(CallableStatement.class, ProxyCallableStatement.class.getName(), methodBody);
      modifyProxyFactory();
   }

   private static void modifyProxyFactory() throws NotFoundException, CannotCompileException, IOException {
      System.out.println("Generating method bodies for com.zaxxer.hikari.proxy.ProxyFactory");
      String packageName = ProxyConnection.class.getPackage().getName();
      CtClass proxyCt = classPool.getCtClass("com.zaxxer.hikari.pool.ProxyFactory");

      for(CtMethod method : proxyCt.getMethods()) {
         String var6 = method.getName();
         switch(var6) {
            case "getProxyConnection":
               method.setBody("{return new " + packageName + ".HikariProxyConnection($$);}");
               break;
            case "getProxyStatement":
               method.setBody("{return new " + packageName + ".HikariProxyStatement($$);}");
               break;
            case "getProxyPreparedStatement":
               method.setBody("{return new " + packageName + ".HikariProxyPreparedStatement($$);}");
               break;
            case "getProxyCallableStatement":
               method.setBody("{return new " + packageName + ".HikariProxyCallableStatement($$);}");
               break;
            case "getProxyResultSet":
               method.setBody("{return new " + packageName + ".HikariProxyResultSet($$);}");
               break;
            case "getProxyDatabaseMetaData":
               method.setBody("{return new " + packageName + ".HikariProxyDatabaseMetaData($$);}");
         }
      }

      proxyCt.writeFile(genDirectory + "target/classes");
   }

   private static <T> void generateProxyClass(Class<T> primaryInterface, String superClassName, String methodBody) throws Exception {
      String newClassName = superClassName.replaceAll("(.+)\\.(\\w+)", "$1.Hikari$2");
      CtClass superCt = classPool.getCtClass(superClassName);
      CtClass targetCt = classPool.makeClass(newClassName, superCt);
      targetCt.setModifiers(Modifier.setPublic(16));
      System.out.println("Generating " + newClassName);
      Set<String> superSigs = new HashSet();

      for(CtMethod method : superCt.getMethods()) {
         if ((method.getModifiers() & 16) == 16) {
            superSigs.add(method.getName() + method.getSignature());
         }
      }

      Set<String> methods = new HashSet();

      for(Class<?> intf : getAllInterfaces(primaryInterface)) {
         CtClass intfCt = classPool.getCtClass(intf.getName());
         targetCt.addInterface(intfCt);

         for(CtMethod intfMethod : intfCt.getDeclaredMethods()) {
            String signature = intfMethod.getName() + intfMethod.getSignature();
            if (!superSigs.contains(signature) && !methods.contains(signature)) {
               methods.add(signature);
               CtMethod method = CtNewMethod.copy(intfMethod, targetCt, null);
               String modifiedBody = methodBody;
               CtMethod superMethod = superCt.getMethod(intfMethod.getName(), intfMethod.getSignature());
               if ((superMethod.getModifiers() & 1024) != 1024 && !isDefaultMethod(intf, intfMethod)) {
                  modifiedBody = methodBody.replace("((cast) ", "");
                  modifiedBody = modifiedBody.replace("delegate", "super");
                  modifiedBody = modifiedBody.replace("super)", "super");
               }

               modifiedBody = modifiedBody.replace("cast", primaryInterface.getName());
               if (isThrowsSqlException(intfMethod)) {
                  modifiedBody = modifiedBody.replace("method", method.getName());
               } else {
                  modifiedBody = "{ return ((cast) delegate).method($$); }".replace("method", method.getName()).replace("cast", primaryInterface.getName());
               }

               if (method.getReturnType() == CtClass.voidType) {
                  modifiedBody = modifiedBody.replace("return", "");
               }

               method.setBody(modifiedBody);
               targetCt.addMethod(method);
            }
         }
      }

      targetCt.getClassFile().setMajorVersion(52);
      targetCt.writeFile(genDirectory + "target/classes");
   }

   private static boolean isThrowsSqlException(CtMethod method) {
      try {
         for(CtClass clazz : method.getExceptionTypes()) {
            if (clazz.getSimpleName().equals("SQLException")) {
               return true;
            }
         }
      } catch (NotFoundException var5) {
      }

      return false;
   }

   private static boolean isDefaultMethod(Class<?> intf, CtMethod intfMethod) throws Exception {
      List<Class<?>> paramTypes = new ArrayList();

      for(CtClass pt : intfMethod.getParameterTypes()) {
         paramTypes.add(toJavaClass(pt));
      }

      return intf.getDeclaredMethod(intfMethod.getName(), (Class[])paramTypes.toArray(new Class[0])).toString().contains("default ");
   }

   private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
      Set<Class<?>> interfaces = new LinkedHashSet();

      for(Class<?> intf : clazz.getInterfaces()) {
         if (intf.getInterfaces().length > 0) {
            interfaces.addAll(getAllInterfaces(intf));
         }

         interfaces.add(intf);
      }

      if (clazz.getSuperclass() != null) {
         interfaces.addAll(getAllInterfaces(clazz.getSuperclass()));
      }

      if (clazz.isInterface()) {
         interfaces.add(clazz);
      }

      return interfaces;
   }

   private static Class<?> toJavaClass(CtClass cls) throws Exception {
      return cls.getName().endsWith("[]") ? Array.newInstance(toJavaClass(cls.getName().replace("[]", "")), 0).getClass() : toJavaClass(cls.getName());
   }

   private static Class<?> toJavaClass(String cn) throws Exception {
      switch(cn) {
         case "int":
            return Integer.TYPE;
         case "long":
            return Long.TYPE;
         case "short":
            return Short.TYPE;
         case "byte":
            return Byte.TYPE;
         case "float":
            return Float.TYPE;
         case "double":
            return Double.TYPE;
         case "boolean":
            return Boolean.TYPE;
         case "char":
            return Character.TYPE;
         case "void":
            return Void.TYPE;
         default:
            return Class.forName(cn);
      }
   }
}
