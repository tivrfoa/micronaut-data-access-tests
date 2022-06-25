package io.micronaut.jdbc.spring;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Factory
@Requirements({@Requires(
   classes = {DataSourceTransactionManager.class}
), @Requires(
   condition = HibernatePresenceCondition.class
)})
@Internal
public class DataSourceTransactionManagerFactory {
   @EachBean(DataSource.class)
   DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
      DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
      dataSourceTransactionManager.afterPropertiesSet();
      return dataSourceTransactionManager;
   }

   @Singleton
   DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener transactionAwareDataSourceListener() {
      return new DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener();
   }

   private static class TransactionAwareDataSourceListener implements BeanCreatedEventListener<DataSource> {
      private TransactionAwareDataSourceListener() {
      }

      public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
         DataSource dataSource = (DataSource)event.getBean();
         return (DataSource)(dataSource instanceof TransactionAwareDataSourceProxy ? dataSource : new TransactionAwareDataSourceProxy(dataSource));
      }
   }
}
