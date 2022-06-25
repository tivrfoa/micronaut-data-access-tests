package io.micronaut.transaction.jdbc;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.spring.DataSourceTransactionManagerFactory;
import jakarta.inject.Singleton;
import java.sql.Connection;
import javax.sql.DataSource;

@Singleton
@Requires(
   missingBeans = {DataSourceTransactionManagerFactory.class}
)
public class TransactionAwareDataSource implements BeanCreatedEventListener<DataSource> {
   private final BeanLocator beanLocator;

   public TransactionAwareDataSource(BeanLocator beanLocator) {
      this.beanLocator = beanLocator;
   }

   public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
      BeanIdentifier beanIdentifier = event.getBeanIdentifier();
      String name = beanIdentifier.getName();
      if (name.equalsIgnoreCase("primary")) {
         name = "default";
      }

      return new TransactionAwareDataSource.DataSourceProxy((DataSource)event.getBean(), name);
   }

   private final class DataSourceProxy extends DelegatingDataSource {
      private final String qualifier;
      private Connection transactionAwareConnection;

      DataSourceProxy(@NonNull DataSource targetDataSource, String qualifier) {
         super(targetDataSource);
         this.qualifier = qualifier;
      }

      @Override
      public Connection getConnection() {
         return this.getTransactionAwareConnection();
      }

      private Connection getTransactionAwareConnection() {
         if (this.transactionAwareConnection == null) {
            this.transactionAwareConnection = TransactionAwareDataSource.this.beanLocator.getBean(Connection.class, Qualifiers.byName(this.qualifier));
         }

         return this.transactionAwareConnection;
      }
   }
}
