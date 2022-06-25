package io.micronaut.transaction.jdbc;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.transaction.exceptions.NoTransactionException;
import io.micronaut.transaction.jdbc.exceptions.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

@Prototype
public final class TransactionalConnectionInterceptor implements MethodInterceptor<Connection, Object> {
   private final DataSource dataSource;
   private boolean closed;

   @Internal
   TransactionalConnectionInterceptor(BeanContext beanContext, Qualifier<DataSource> qualifier) {
      DataSource dataSource = beanContext.getBean(DataSource.class, qualifier);
      if (dataSource instanceof DelegatingDataSource) {
         dataSource = ((DelegatingDataSource)dataSource).getTargetDataSource();
      }

      this.dataSource = dataSource;
   }

   @Override
   public Object intercept(MethodInvocationContext<Connection, Object> context) {
      Connection connection;
      try {
         connection = DataSourceUtils.getConnection(this.dataSource, false);
      } catch (CannotGetJdbcConnectionException var6) {
         throw new NoTransactionException("No current transaction present. Consider declaring @Transactional on the surrounding method", var6);
      }

      ExecutableMethod<Connection, Object> method = context.getExecutableMethod();
      if (method.getName().equals("close")) {
         try {
            DataSourceUtils.doReleaseConnection(connection, this.dataSource);
            return null;
         } catch (SQLException var5) {
            throw new CannotGetJdbcConnectionException("Failed to release connection: " + var5.getMessage(), var5);
         }
      } else {
         return method.invoke(connection, context.getParameterValues());
      }
   }
}
