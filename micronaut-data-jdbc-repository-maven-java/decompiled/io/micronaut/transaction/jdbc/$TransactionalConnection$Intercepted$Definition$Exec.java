package io.micronaut.transaction.jdbc;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Wrapper;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

// $FF: synthetic class
@Generated
final class $TransactionalConnection$Intercepted$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createStatement",
         Argument.of(Statement.class, "java.sql.Statement"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareStatement",
         Argument.of(PreparedStatement.class, "java.sql.PreparedStatement"),
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareCall",
         Argument.of(CallableStatement.class, "java.sql.CallableStatement"),
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "nativeSQL",
         Argument.STRING,
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setAutoCommit",
         Argument.VOID,
         new Argument[]{Argument.of(Boolean.TYPE, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "getAutoCommit", Argument.BOOLEAN, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "commit", Argument.VOID, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "rollback", Argument.VOID, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "close", Argument.VOID, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "isClosed", Argument.BOOLEAN, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getMetaData",
         Argument.of(DatabaseMetaData.class, "java.sql.DatabaseMetaData"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setReadOnly",
         Argument.VOID,
         new Argument[]{Argument.of(Boolean.TYPE, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "isReadOnly", Argument.BOOLEAN, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setCatalog",
         Argument.VOID,
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "getCatalog", Argument.STRING, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setTransactionIsolation",
         Argument.VOID,
         new Argument[]{Argument.of(Integer.TYPE, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getTransactionIsolation",
         Argument.INT,
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getWarnings",
         Argument.of(SQLWarning.class, "java.sql.SQLWarning"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "clearWarnings", Argument.VOID, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createStatement",
         Argument.of(Statement.class, "java.sql.Statement"),
         new Argument[]{Argument.of(Integer.TYPE, "arg0"), Argument.of(Integer.TYPE, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareStatement",
         Argument.of(PreparedStatement.class, "java.sql.PreparedStatement"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(Integer.TYPE, "arg1"), Argument.of(Integer.TYPE, "arg2")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareCall",
         Argument.of(CallableStatement.class, "java.sql.CallableStatement"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(Integer.TYPE, "arg1"), Argument.of(Integer.TYPE, "arg2")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getTypeMap",
         Argument.of(
            Map.class,
            "java.util.Map",
            null,
            Argument.ofTypeVariable(String.class, "K"),
            Argument.ofTypeVariable(Class.class, "V", null, Argument.ofTypeVariable(Object.class, "T"))
         ),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setTypeMap",
         Argument.VOID,
         new Argument[]{
            Argument.of(
               Map.class,
               "arg0",
               null,
               Argument.ofTypeVariable(String.class, "K"),
               Argument.ofTypeVariable(Class.class, "V", null, Argument.ofTypeVariable(Object.class, "T"))
            )
         },
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setHoldability",
         Argument.VOID,
         new Argument[]{Argument.of(Integer.TYPE, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "getHoldability", Argument.INT, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setSavepoint",
         Argument.of(Savepoint.class, "java.sql.Savepoint"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setSavepoint",
         Argument.of(Savepoint.class, "java.sql.Savepoint"),
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "rollback",
         Argument.VOID,
         new Argument[]{Argument.of(Savepoint.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "releaseSavepoint",
         Argument.VOID,
         new Argument[]{Argument.of(Savepoint.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createStatement",
         Argument.of(Statement.class, "java.sql.Statement"),
         new Argument[]{Argument.of(Integer.TYPE, "arg0"), Argument.of(Integer.TYPE, "arg1"), Argument.of(Integer.TYPE, "arg2")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareStatement",
         Argument.of(PreparedStatement.class, "java.sql.PreparedStatement"),
         new Argument[]{
            Argument.of(String.class, "arg0"), Argument.of(Integer.TYPE, "arg1"), Argument.of(Integer.TYPE, "arg2"), Argument.of(Integer.TYPE, "arg3")
         },
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareCall",
         Argument.of(CallableStatement.class, "java.sql.CallableStatement"),
         new Argument[]{
            Argument.of(String.class, "arg0"), Argument.of(Integer.TYPE, "arg1"), Argument.of(Integer.TYPE, "arg2"), Argument.of(Integer.TYPE, "arg3")
         },
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareStatement",
         Argument.of(PreparedStatement.class, "java.sql.PreparedStatement"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(Integer.TYPE, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "prepareStatement",
         Argument.of(PreparedStatement.class, "java.sql.PreparedStatement"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(String[].class, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createClob",
         Argument.of(Clob.class, "java.sql.Clob"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createBlob",
         Argument.of(Blob.class, "java.sql.Blob"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createNClob",
         Argument.of(NClob.class, "java.sql.NClob"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createSQLXML",
         Argument.of(SQLXML.class, "java.sql.SQLXML"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "isValid",
         Argument.BOOLEAN,
         new Argument[]{Argument.of(Integer.TYPE, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setClientInfo",
         Argument.VOID,
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(String.class, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setClientInfo",
         Argument.VOID,
         new Argument[]{Argument.of(Properties.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getClientInfo",
         Argument.STRING,
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "getClientInfo",
         Argument.of(Properties.class, "java.util.Properties"),
         null,
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createArrayOf",
         Argument.of(Array.class, "java.sql.Array"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(Object[].class, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "createStruct",
         Argument.of(Struct.class, "java.sql.Struct"),
         new Argument[]{Argument.of(String.class, "arg0"), Argument.of(Object[].class, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setSchema",
         Argument.VOID,
         new Argument[]{Argument.of(String.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "getSchema", Argument.STRING, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "abort",
         Argument.VOID,
         new Argument[]{Argument.of(Executor.class, "arg0")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "setNetworkTimeout",
         Argument.VOID,
         new Argument[]{Argument.of(Executor.class, "arg0"), Argument.of(Integer.TYPE, "arg1")},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Connection.class, $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA, "getNetworkTimeout", Argument.INT, null, true, false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Wrapper.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "unwrap",
         Argument.ofTypeVariable(Object.class, "java.lang.Object", "T", null, null),
         new Argument[]{Argument.of(Class.class, "arg0", null, Argument.ofTypeVariable(Object.class, "T"))},
         true,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         Wrapper.class,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         "isWrapperFor",
         Argument.BOOLEAN,
         new Argument[]{Argument.of(Class.class, "arg0", null, Argument.ofTypeVariable(Object.class, "T"))},
         true,
         false
      )
   };

   public $TransactionalConnection$Intercepted$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((Connection)var2).createStatement();
         case 1:
            return ((Connection)var2).prepareStatement((String)var3[0]);
         case 2:
            return ((Connection)var2).prepareCall((String)var3[0]);
         case 3:
            return ((Connection)var2).nativeSQL((String)var3[0]);
         case 4:
            ((Connection)var2).setAutoCommit(var3[0]);
            return null;
         case 5:
            return ((Connection)var2).getAutoCommit();
         case 6:
            ((Connection)var2).commit();
            return null;
         case 7:
            ((Connection)var2).rollback();
            return null;
         case 8:
            ((Connection)var2).close();
            return null;
         case 9:
            return ((Connection)var2).isClosed();
         case 10:
            return ((Connection)var2).getMetaData();
         case 11:
            ((Connection)var2).setReadOnly(var3[0]);
            return null;
         case 12:
            return ((Connection)var2).isReadOnly();
         case 13:
            ((Connection)var2).setCatalog((String)var3[0]);
            return null;
         case 14:
            return ((Connection)var2).getCatalog();
         case 15:
            ((Connection)var2).setTransactionIsolation(var3[0]);
            return null;
         case 16:
            return ((Connection)var2).getTransactionIsolation();
         case 17:
            return ((Connection)var2).getWarnings();
         case 18:
            ((Connection)var2).clearWarnings();
            return null;
         case 19:
            return ((Connection)var2).createStatement(var3[0], var3[1]);
         case 20:
            return ((Connection)var2).prepareStatement((String)var3[0], var3[1], var3[2]);
         case 21:
            return ((Connection)var2).prepareCall((String)var3[0], var3[1], var3[2]);
         case 22:
            return ((Connection)var2).getTypeMap();
         case 23:
            ((Connection)var2).setTypeMap((Map)var3[0]);
            return null;
         case 24:
            ((Connection)var2).setHoldability(var3[0]);
            return null;
         case 25:
            return ((Connection)var2).getHoldability();
         case 26:
            return ((Connection)var2).setSavepoint();
         case 27:
            return ((Connection)var2).setSavepoint((String)var3[0]);
         case 28:
            ((Connection)var2).rollback((Savepoint)var3[0]);
            return null;
         case 29:
            ((Connection)var2).releaseSavepoint((Savepoint)var3[0]);
            return null;
         case 30:
            return ((Connection)var2).createStatement(var3[0], var3[1], var3[2]);
         case 31:
            return ((Connection)var2).prepareStatement((String)var3[0], var3[1], var3[2], var3[3]);
         case 32:
            return ((Connection)var2).prepareCall((String)var3[0], var3[1], var3[2], var3[3]);
         case 33:
            return ((Connection)var2).prepareStatement((String)var3[0], var3[1]);
         case 34:
            return ((Connection)var2).prepareStatement((String)var3[0], (String[])var3[1]);
         case 35:
            return ((Connection)var2).createClob();
         case 36:
            return ((Connection)var2).createBlob();
         case 37:
            return ((Connection)var2).createNClob();
         case 38:
            return ((Connection)var2).createSQLXML();
         case 39:
            return ((Connection)var2).isValid(var3[0]);
         case 40:
            ((Connection)var2).setClientInfo((String)var3[0], (String)var3[1]);
            return null;
         case 41:
            ((Connection)var2).setClientInfo((Properties)var3[0]);
            return null;
         case 42:
            return ((Connection)var2).getClientInfo((String)var3[0]);
         case 43:
            return ((Connection)var2).getClientInfo();
         case 44:
            return ((Connection)var2).createArrayOf((String)var3[0], var3[1]);
         case 45:
            return ((Connection)var2).createStruct((String)var3[0], var3[1]);
         case 46:
            ((Connection)var2).setSchema((String)var3[0]);
            return null;
         case 47:
            return ((Connection)var2).getSchema();
         case 48:
            ((Connection)var2).abort((Executor)var3[0]);
            return null;
         case 49:
            ((Connection)var2).setNetworkTimeout((Executor)var3[0], var3[1]);
            return null;
         case 50:
            return ((Connection)var2).getNetworkTimeout();
         case 51:
            return ((Wrapper)var2).unwrap((Class)var3[0]);
         case 52:
            return ((Wrapper)var2).isWrapperFor((Class)var3[0]);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createStatement", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 1:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareStatement", String.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareCall", String.class);
         case 3:
            return ReflectionUtils.getRequiredMethod(Connection.class, "nativeSQL", String.class);
         case 4:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setAutoCommit", Boolean.TYPE);
         case 5:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getAutoCommit", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 6:
            return ReflectionUtils.getRequiredMethod(Connection.class, "commit", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 7:
            return ReflectionUtils.getRequiredMethod(Connection.class, "rollback", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 8:
            return ReflectionUtils.getRequiredMethod(Connection.class, "close", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 9:
            return ReflectionUtils.getRequiredMethod(Connection.class, "isClosed", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 10:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getMetaData", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 11:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setReadOnly", Boolean.TYPE);
         case 12:
            return ReflectionUtils.getRequiredMethod(Connection.class, "isReadOnly", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 13:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setCatalog", String.class);
         case 14:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getCatalog", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 15:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setTransactionIsolation", Integer.TYPE);
         case 16:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getTransactionIsolation", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 17:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getWarnings", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 18:
            return ReflectionUtils.getRequiredMethod(Connection.class, "clearWarnings", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 19:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createStatement", Integer.TYPE, Integer.TYPE);
         case 20:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareStatement", String.class, Integer.TYPE, Integer.TYPE);
         case 21:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareCall", String.class, Integer.TYPE, Integer.TYPE);
         case 22:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getTypeMap", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 23:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setTypeMap", Map.class);
         case 24:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setHoldability", Integer.TYPE);
         case 25:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getHoldability", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 26:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setSavepoint", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 27:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setSavepoint", String.class);
         case 28:
            return ReflectionUtils.getRequiredMethod(Connection.class, "rollback", Savepoint.class);
         case 29:
            return ReflectionUtils.getRequiredMethod(Connection.class, "releaseSavepoint", Savepoint.class);
         case 30:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createStatement", Integer.TYPE, Integer.TYPE, Integer.TYPE);
         case 31:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareStatement", String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
         case 32:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareCall", String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
         case 33:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareStatement", String.class, Integer.TYPE);
         case 34:
            return ReflectionUtils.getRequiredMethod(Connection.class, "prepareStatement", String.class, String[].class);
         case 35:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createClob", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 36:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createBlob", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 37:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createNClob", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 38:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createSQLXML", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 39:
            return ReflectionUtils.getRequiredMethod(Connection.class, "isValid", Integer.TYPE);
         case 40:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setClientInfo", String.class, String.class);
         case 41:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setClientInfo", Properties.class);
         case 42:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getClientInfo", String.class);
         case 43:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getClientInfo", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 44:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createArrayOf", String.class, Object[].class);
         case 45:
            return ReflectionUtils.getRequiredMethod(Connection.class, "createStruct", String.class, Object[].class);
         case 46:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setSchema", String.class);
         case 47:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getSchema", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 48:
            return ReflectionUtils.getRequiredMethod(Connection.class, "abort", Executor.class);
         case 49:
            return ReflectionUtils.getRequiredMethod(Connection.class, "setNetworkTimeout", Executor.class, Integer.TYPE);
         case 50:
            return ReflectionUtils.getRequiredMethod(Connection.class, "getNetworkTimeout", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 51:
            return ReflectionUtils.getRequiredMethod(Wrapper.class, "unwrap", Class.class);
         case 52:
            return ReflectionUtils.getRequiredMethod(Wrapper.class, "isWrapperFor", Class.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   private final ExecutableMethod getMethod(String var1, Class[] var2) {
      switch(var1.hashCode()) {
         case -1718976051:
            if (this.methodAtIndexMatches(17, var1, var2)) {
               return this.getExecutableMethodByIndex(17);
            }
            break;
         case -1660954196:
            if (this.methodAtIndexMatches(12, var1, var2)) {
               return this.getExecutableMethodByIndex(12);
            }
            break;
         case -1354815177:
            if (this.methodAtIndexMatches(6, var1, var2)) {
               return this.getExecutableMethodByIndex(6);
            }
            break;
         case -1313202171:
            if (this.methodAtIndexMatches(2, var1, var2)) {
               return this.getExecutableMethodByIndex(2);
            }

            if (this.methodAtIndexMatches(21, var1, var2)) {
               return this.getExecutableMethodByIndex(21);
            }

            if (this.methodAtIndexMatches(32, var1, var2)) {
               return this.getExecutableMethodByIndex(32);
            }
            break;
         case -1304267319:
            if (this.methodAtIndexMatches(24, var1, var2)) {
               return this.getExecutableMethodByIndex(24);
            }
            break;
         case -1143247940:
            if (this.methodAtIndexMatches(5, var1, var2)) {
               return this.getExecutableMethodByIndex(5);
            }
            break;
         case -1113328600:
            if (this.methodAtIndexMatches(1, var1, var2)) {
               return this.getExecutableMethodByIndex(1);
            }

            if (this.methodAtIndexMatches(20, var1, var2)) {
               return this.getExecutableMethodByIndex(20);
            }

            if (this.methodAtIndexMatches(31, var1, var2)) {
               return this.getExecutableMethodByIndex(31);
            }

            if (this.methodAtIndexMatches(33, var1, var2)) {
               return this.getExecutableMethodByIndex(33);
            }

            if (this.methodAtIndexMatches(34, var1, var2)) {
               return this.getExecutableMethodByIndex(34);
            }
            break;
         case -1008245193:
            if (this.methodAtIndexMatches(3, var1, var2)) {
               return this.getExecutableMethodByIndex(3);
            }
            break;
         case -840111517:
            if (this.methodAtIndexMatches(51, var1, var2)) {
               return this.getExecutableMethodByIndex(51);
            }
            break;
         case -765597353:
            if (this.methodAtIndexMatches(13, var1, var2)) {
               return this.getExecutableMethodByIndex(13);
            }
            break;
         case -683486410:
            if (this.methodAtIndexMatches(9, var1, var2)) {
               return this.getExecutableMethodByIndex(9);
            }
            break;
         case -510730770:
            if (this.methodAtIndexMatches(37, var1, var2)) {
               return this.getExecutableMethodByIndex(37);
            }
            break;
         case -369116728:
            if (this.methodAtIndexMatches(4, var1, var2)) {
               return this.getExecutableMethodByIndex(4);
            }
            break;
         case -259719452:
            if (this.methodAtIndexMatches(7, var1, var2)) {
               return this.getExecutableMethodByIndex(7);
            }

            if (this.methodAtIndexMatches(28, var1, var2)) {
               return this.getExecutableMethodByIndex(28);
            }
            break;
         case -240139836:
            if (this.methodAtIndexMatches(18, var1, var2)) {
               return this.getExecutableMethodByIndex(18);
            }
            break;
         case -229535470:
            if (this.methodAtIndexMatches(15, var1, var2)) {
               return this.getExecutableMethodByIndex(15);
            }
            break;
         case -81327636:
            if (this.methodAtIndexMatches(29, var1, var2)) {
               return this.getExecutableMethodByIndex(29);
            }
            break;
         case 45191253:
            if (this.methodAtIndexMatches(49, var1, var2)) {
               return this.getExecutableMethodByIndex(49);
            }
            break;
         case 92611376:
            if (this.methodAtIndexMatches(48, var1, var2)) {
               return this.getExecutableMethodByIndex(48);
            }
            break;
         case 94756344:
            if (this.methodAtIndexMatches(8, var1, var2)) {
               return this.getExecutableMethodByIndex(8);
            }
            break;
         case 183752588:
            if (this.methodAtIndexMatches(22, var1, var2)) {
               return this.getExecutableMethodByIndex(22);
            }
            break;
         case 467468885:
            if (this.methodAtIndexMatches(25, var1, var2)) {
               return this.getExecutableMethodByIndex(25);
            }
            break;
         case 573410467:
            if (this.methodAtIndexMatches(46, var1, var2)) {
               return this.getExecutableMethodByIndex(46);
            }
            break;
         case 744686547:
            if (this.methodAtIndexMatches(0, var1, var2)) {
               return this.getExecutableMethodByIndex(0);
            }

            if (this.methodAtIndexMatches(19, var1, var2)) {
               return this.getExecutableMethodByIndex(19);
            }

            if (this.methodAtIndexMatches(30, var1, var2)) {
               return this.getExecutableMethodByIndex(30);
            }
            break;
         case 788027543:
            if (this.methodAtIndexMatches(47, var1, var2)) {
               return this.getExecutableMethodByIndex(47);
            }
            break;
         case 804366095:
            if (this.methodAtIndexMatches(42, var1, var2)) {
               return this.getExecutableMethodByIndex(42);
            }

            if (this.methodAtIndexMatches(43, var1, var2)) {
               return this.getExecutableMethodByIndex(43);
            }
            break;
         case 985344073:
            if (this.methodAtIndexMatches(50, var1, var2)) {
               return this.getExecutableMethodByIndex(50);
            }
            break;
         case 1121636580:
            if (this.methodAtIndexMatches(11, var1, var2)) {
               return this.getExecutableMethodByIndex(11);
            }
            break;
         case 1368680121:
            if (this.methodAtIndexMatches(36, var1, var2)) {
               return this.getExecutableMethodByIndex(36);
            }
            break;
         case 1368709912:
            if (this.methodAtIndexMatches(35, var1, var2)) {
               return this.getExecutableMethodByIndex(35);
            }
            break;
         case 1502314373:
            if (this.methodAtIndexMatches(38, var1, var2)) {
               return this.getExecutableMethodByIndex(38);
            }
            break;
         case 1512074612:
            if (this.methodAtIndexMatches(44, var1, var2)) {
               return this.getExecutableMethodByIndex(44);
            }
            break;
         case 1535798257:
            if (this.methodAtIndexMatches(45, var1, var2)) {
               return this.getExecutableMethodByIndex(45);
            }
            break;
         case 1542905856:
            if (this.methodAtIndexMatches(52, var1, var2)) {
               return this.getExecutableMethodByIndex(52);
            }
            break;
         case 1578497307:
            if (this.methodAtIndexMatches(40, var1, var2)) {
               return this.getExecutableMethodByIndex(40);
            }

            if (this.methodAtIndexMatches(41, var1, var2)) {
               return this.getExecutableMethodByIndex(41);
            }
            break;
         case 1592564707:
            if (this.methodAtIndexMatches(14, var1, var2)) {
               return this.getExecutableMethodByIndex(14);
            }
            break;
         case 1626942661:
            if (this.methodAtIndexMatches(10, var1, var2)) {
               return this.getExecutableMethodByIndex(10);
            }
            break;
         case 1739074545:
            if (this.methodAtIndexMatches(26, var1, var2)) {
               return this.getExecutableMethodByIndex(26);
            }

            if (this.methodAtIndexMatches(27, var1, var2)) {
               return this.getExecutableMethodByIndex(27);
            }
            break;
         case 2013159942:
            if (this.methodAtIndexMatches(16, var1, var2)) {
               return this.getExecutableMethodByIndex(16);
            }
            break;
         case 2073378034:
            if (this.methodAtIndexMatches(39, var1, var2)) {
               return this.getExecutableMethodByIndex(39);
            }
            break;
         case 2120557824:
            if (this.methodAtIndexMatches(23, var1, var2)) {
               return this.getExecutableMethodByIndex(23);
            }
      }

      return null;
   }
}
