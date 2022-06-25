package io.micronaut.configuration.jdbc.hikari;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class DatasourceFactory implements AutoCloseable {
   private static final Logger LOG = LoggerFactory.getLogger(DatasourceFactory.class);
   private List<HikariUrlDataSource> dataSources = new ArrayList(2);
   private ApplicationContext applicationContext;

   public DatasourceFactory(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   @Context
   @EachBean(DatasourceConfiguration.class)
   public DataSource dataSource(DatasourceConfiguration datasourceConfiguration) {
      HikariUrlDataSource ds = new HikariUrlDataSource(datasourceConfiguration);
      this.addMeterRegistry(ds);
      this.dataSources.add(ds);
      return ds;
   }

   private void addMeterRegistry(HikariUrlDataSource ds) {
      try {
         MeterRegistry meterRegistry = this.getMeterRegistry();
         if (ds != null && meterRegistry != null && this.applicationContext.getProperty("micronaut.metrics.binders.jdbc.enabled", Boolean.TYPE).orElse(true)) {
            ds.setMetricRegistry(meterRegistry);
         }
      } catch (NoClassDefFoundError var3) {
         LOG.debug(
            "Could not wire metrics to HikariCP as there is no class of type MeterRegistry on the classpath, io.micronaut.configuration:micrometer-core library missing."
         );
      }

   }

   private MeterRegistry getMeterRegistry() {
      return this.applicationContext.containsBean(MeterRegistry.class) ? this.applicationContext.getBean(MeterRegistry.class) : null;
   }

   @PreDestroy
   public void close() {
      for(HikariUrlDataSource dataSource : this.dataSources) {
         try {
            dataSource.close();
         } catch (Exception var4) {
            if (LOG.isWarnEnabled()) {
               LOG.warn("Error closing data source [" + dataSource + "]: " + var4.getMessage(), var4);
            }
         }
      }

   }
}
