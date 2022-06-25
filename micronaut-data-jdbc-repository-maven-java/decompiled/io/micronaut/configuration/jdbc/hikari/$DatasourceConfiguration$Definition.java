package io.micronaut.configuration.jdbc.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $DatasourceConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DatasourceConfiguration>
   implements BeanFactory<DatasourceConfiguration>,
   InitializingBeanDefinition<DatasourceConfiguration>,
   ParametrizedBeanFactory<DatasourceConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DatasourceConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "name",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;

   @Override
   public DatasourceConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      DatasourceConfiguration var5 = new DatasourceConfiguration((String)var4.get("name"));
      var5 = (DatasourceConfiguration)this.injectBean(var1, var2, var5);
      DatasourceConfiguration var10001 = (DatasourceConfiguration)this.initialize(var1, var2, var5);
      return var5;
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DatasourceConfiguration var4 = (DatasourceConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "datasources.*.catalog")) {
            var4.setCatalog(
               (String)super.getPropertyValueForSetter(var1, var2, "setCatalog", $INJECTION_METHODS[0].arguments[0], "datasources.*.catalog", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.connection-timeout")) {
            var4.setConnectionTimeout(
               super.getPropertyValueForSetter(var1, var2, "setConnectionTimeout", $INJECTION_METHODS[1].arguments[0], "datasources.*.connection-timeout", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.idle-timeout")) {
            var4.setIdleTimeout(
               super.getPropertyValueForSetter(var1, var2, "setIdleTimeout", $INJECTION_METHODS[2].arguments[0], "datasources.*.idle-timeout", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.leak-detection-threshold")) {
            var4.setLeakDetectionThreshold(
               super.getPropertyValueForSetter(
                  var1, var2, "setLeakDetectionThreshold", $INJECTION_METHODS[3].arguments[0], "datasources.*.leak-detection-threshold", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.max-lifetime")) {
            var4.setMaxLifetime(
               super.getPropertyValueForSetter(var1, var2, "setMaxLifetime", $INJECTION_METHODS[4].arguments[0], "datasources.*.max-lifetime", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.maximum-pool-size")) {
            var4.setMaximumPoolSize(
               super.getPropertyValueForSetter(var1, var2, "setMaximumPoolSize", $INJECTION_METHODS[5].arguments[0], "datasources.*.maximum-pool-size", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.minimum-idle")) {
            var4.setMinimumIdle(
               super.getPropertyValueForSetter(var1, var2, "setMinimumIdle", $INJECTION_METHODS[6].arguments[0], "datasources.*.minimum-idle", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(var1, var2, "setPassword", $INJECTION_METHODS[7].arguments[0], "datasources.*.password", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.username")) {
            var4.setUsername(
               (String)super.getPropertyValueForSetter(var1, var2, "setUsername", $INJECTION_METHODS[8].arguments[0], "datasources.*.username", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.validation-timeout")) {
            var4.setValidationTimeout(
               super.getPropertyValueForSetter(var1, var2, "setValidationTimeout", $INJECTION_METHODS[9].arguments[0], "datasources.*.validation-timeout", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.connection-test-query")) {
            var4.setConnectionTestQuery(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setConnectionTestQuery", $INJECTION_METHODS[10].arguments[0], "datasources.*.connection-test-query", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.connection-init-sql")) {
            var4.setConnectionInitSql(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setConnectionInitSql", $INJECTION_METHODS[11].arguments[0], "datasources.*.connection-init-sql", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.data-source")) {
            var4.setDataSource(
               (DataSource)super.getPropertyValueForSetter(var1, var2, "setDataSource", $INJECTION_METHODS[12].arguments[0], "datasources.*.data-source", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.data-source-class-name")) {
            var4.setDataSourceClassName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setDataSourceClassName", $INJECTION_METHODS[13].arguments[0], "datasources.*.data-source-class-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.data-source-jndi")) {
            var4.setDataSourceJNDI(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setDataSourceJNDI", $INJECTION_METHODS[14].arguments[0], "datasources.*.data-source-jndi", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.driver-class-name")) {
            var4.setDriverClassName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setDriverClassName", $INJECTION_METHODS[15].arguments[0], "datasources.*.driver-class-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.jdbc-url")) {
            var4.setJdbcUrl(
               (String)super.getPropertyValueForSetter(var1, var2, "setJdbcUrl", $INJECTION_METHODS[16].arguments[0], "datasources.*.jdbc-url", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.auto-commit")) {
            var4.setAutoCommit(
               super.getPropertyValueForSetter(var1, var2, "setAutoCommit", $INJECTION_METHODS[17].arguments[0], "datasources.*.auto-commit", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.allow-pool-suspension")) {
            var4.setAllowPoolSuspension(
               super.getPropertyValueForSetter(
                  var1, var2, "setAllowPoolSuspension", $INJECTION_METHODS[18].arguments[0], "datasources.*.allow-pool-suspension", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.initialization-fail-timeout")) {
            var4.setInitializationFailTimeout(
               super.getPropertyValueForSetter(
                  var1, var2, "setInitializationFailTimeout", $INJECTION_METHODS[19].arguments[0], "datasources.*.initialization-fail-timeout", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.isolate-internal-queries")) {
            var4.setIsolateInternalQueries(
               super.getPropertyValueForSetter(
                  var1, var2, "setIsolateInternalQueries", $INJECTION_METHODS[20].arguments[0], "datasources.*.isolate-internal-queries", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.metrics-tracker-factory")) {
            var4.setMetricsTrackerFactory(
               (MetricsTrackerFactory)super.getPropertyValueForSetter(
                  var1, var2, "setMetricsTrackerFactory", $INJECTION_METHODS[21].arguments[0], "datasources.*.metrics-tracker-factory", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.metric-registry")) {
            var4.setMetricRegistry(
               super.getPropertyValueForSetter(var1, var2, "setMetricRegistry", $INJECTION_METHODS[22].arguments[0], "datasources.*.metric-registry", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.health-check-registry")) {
            var4.setHealthCheckRegistry(
               super.getPropertyValueForSetter(
                  var1, var2, "setHealthCheckRegistry", $INJECTION_METHODS[23].arguments[0], "datasources.*.health-check-registry", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "datasources.*.health-check-properties")) {
            var4.setHealthCheckProperties(
               (Properties)super.getPropertyValueForSetter(
                  var1, var2, "setHealthCheckProperties", $INJECTION_METHODS[24].arguments[0], "datasources.*.health-check-properties", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.keepalive-time")) {
            var4.setKeepaliveTime(
               super.getPropertyValueForSetter(var1, var2, "setKeepaliveTime", $INJECTION_METHODS[25].arguments[0], "datasources.*.keepalive-time", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.read-only")) {
            var4.setReadOnly(super.getPropertyValueForSetter(var1, var2, "setReadOnly", $INJECTION_METHODS[26].arguments[0], "datasources.*.read-only", null));
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.register-mbeans")) {
            var4.setRegisterMbeans(
               super.getPropertyValueForSetter(var1, var2, "setRegisterMbeans", $INJECTION_METHODS[27].arguments[0], "datasources.*.register-mbeans", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.pool-name")) {
            var4.setPoolName(
               (String)super.getPropertyValueForSetter(var1, var2, "setPoolName", $INJECTION_METHODS[28].arguments[0], "datasources.*.pool-name", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.scheduled-executor")) {
            var4.setScheduledExecutor(
               (ScheduledExecutorService)super.getPropertyValueForSetter(
                  var1, var2, "setScheduledExecutor", $INJECTION_METHODS[29].arguments[0], "datasources.*.scheduled-executor", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.schema")) {
            var4.setSchema((String)super.getPropertyValueForSetter(var1, var2, "setSchema", $INJECTION_METHODS[30].arguments[0], "datasources.*.schema", null));
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.exception-override-class-name")) {
            var4.setExceptionOverrideClassName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setExceptionOverrideClassName", $INJECTION_METHODS[31].arguments[0], "datasources.*.exception-override-class-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.transaction-isolation")) {
            var4.setTransactionIsolation(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setTransactionIsolation", $INJECTION_METHODS[32].arguments[0], "datasources.*.transaction-isolation", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.thread-factory")) {
            var4.setThreadFactory(
               (ThreadFactory)super.getPropertyValueForSetter(
                  var1, var2, "setThreadFactory", $INJECTION_METHODS[33].arguments[0], "datasources.*.thread-factory", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.url")) {
            var4.setUrl((String)super.getPropertyValueForSetter(var1, var2, "setUrl", $INJECTION_METHODS[35].arguments[0], "datasources.*.url", null));
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.validation-query")) {
            var4.setValidationQuery(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setValidationQuery", $INJECTION_METHODS[36].arguments[0], "datasources.*.validation-query", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.jndi-name")) {
            var4.setJndiName(
               (String)super.getPropertyValueForSetter(var1, var2, "setJndiName", $INJECTION_METHODS[37].arguments[0], "datasources.*.jndi-name", null)
            );
         }

         if (this.containsPropertiesValue(var1, var2, "datasources.*.data-source-properties")) {
            var4.setDataSourceProperties(
               (Map<String, ?>)super.getPropertyValueForSetter(
                  var1, var2, "setDataSourceProperties", $INJECTION_METHODS[38].arguments[0], "datasources.*.data-source-properties", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "datasources.*.data-source-properties")) {
            var4.setDataSourceProperties(
               (Properties)super.getPropertyValueForSetter(
                  var1, var2, "setDataSourceProperties", $INJECTION_METHODS[39].arguments[0], "datasources.*.data-source-properties", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.automatic-validation-query")) {
            var4.setAutomaticValidationQuery(
               super.getPropertyValueForSetter(
                  var1, var2, "setAutomaticValidationQuery", $INJECTION_METHODS[40].arguments[0], "datasources.*.automatic-validation-query", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DatasourceConfiguration initialize(BeanResolutionContext var1, BeanContext var2, DatasourceConfiguration var3) {
      DatasourceConfiguration var4 = (DatasourceConfiguration)var3;
      super.postConstruct(var1, var2, var3);
      var4.postConstruct();
      return var4;
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setCatalog",
            new Argument[]{Argument.of(String.class, "catalog")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property",
                           AnnotationUtil.mapOf("name", "datasources.*.catalog"),
                           var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.catalog"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setConnectionTimeout",
            new Argument[]{Argument.of(Long.TYPE, "connectionTimeoutMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setIdleTimeout",
            new Argument[]{Argument.of(Long.TYPE, "idleTimeoutMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.idle-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.idle-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setLeakDetectionThreshold",
            new Argument[]{Argument.of(Long.TYPE, "leakDetectionThresholdMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.leak-detection-threshold"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.leak-detection-threshold"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setMaxLifetime",
            new Argument[]{Argument.of(Long.TYPE, "maxLifetimeMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.max-lifetime"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.max-lifetime"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setMaximumPoolSize",
            new Argument[]{Argument.of(Integer.TYPE, "maxPoolSize")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.maximum-pool-size"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.maximum-pool-size"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setMinimumIdle",
            new Argument[]{Argument.of(Integer.TYPE, "minIdle")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.minimum-idle"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.minimum-idle"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setPassword",
            new Argument[]{Argument.of(String.class, "password")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.password"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.password"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setUsername",
            new Argument[]{Argument.of(String.class, "username")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.username"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.username"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setValidationTimeout",
            new Argument[]{Argument.of(Long.TYPE, "validationTimeoutMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.validation-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.validation-timeout"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setConnectionTestQuery",
            new Argument[]{Argument.of(String.class, "connectionTestQuery")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-test-query"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-test-query"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setConnectionInitSql",
            new Argument[]{Argument.of(String.class, "connectionInitSql")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-init-sql"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.connection-init-sql"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setDataSource",
            new Argument[]{Argument.of(DataSource.class, "dataSource")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setDataSourceClassName",
            new Argument[]{Argument.of(String.class, "className")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-class-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-class-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setDataSourceJNDI",
            new Argument[]{Argument.of(String.class, "jndiDataSource")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-jndi"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-jndi"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setDriverClassName",
            new Argument[]{Argument.of(String.class, "driverClassName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.driver-class-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.driver-class-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setJdbcUrl",
            new Argument[]{Argument.of(String.class, "jdbcUrl")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.jdbc-url"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.jdbc-url"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setAutoCommit",
            new Argument[]{Argument.of(Boolean.TYPE, "isAutoCommit")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.auto-commit"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.auto-commit"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setAllowPoolSuspension",
            new Argument[]{Argument.of(Boolean.TYPE, "isAllowPoolSuspension")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.allow-pool-suspension"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.allow-pool-suspension"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setInitializationFailTimeout",
            new Argument[]{Argument.of(Long.TYPE, "initializationFailTimeout")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.initialization-fail-timeout"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.initialization-fail-timeout"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setIsolateInternalQueries",
            new Argument[]{Argument.of(Boolean.TYPE, "isolate")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.isolate-internal-queries"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.isolate-internal-queries"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setMetricsTrackerFactory",
            new Argument[]{Argument.of(MetricsTrackerFactory.class, "metricsTrackerFactory")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.metrics-tracker-factory"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.metrics-tracker-factory"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setMetricRegistry",
            new Argument[]{Argument.of(Object.class, "metricRegistry")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.metric-registry"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.metric-registry"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setHealthCheckRegistry",
            new Argument[]{Argument.of(Object.class, "healthCheckRegistry")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.health-check-registry"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.health-check-registry"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setHealthCheckProperties",
            new Argument[]{Argument.of(Properties.class, "healthCheckProperties")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.health-check-properties"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.health-check-properties"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setKeepaliveTime",
            new Argument[]{Argument.of(Long.TYPE, "keepaliveTimeMs")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.keepalive-time"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.keepalive-time"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setReadOnly",
            new Argument[]{Argument.of(Boolean.TYPE, "readOnly")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.read-only"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.read-only"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setRegisterMbeans",
            new Argument[]{Argument.of(Boolean.TYPE, "register")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.register-mbeans"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.register-mbeans"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setPoolName",
            new Argument[]{Argument.of(String.class, "poolName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.pool-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.pool-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setScheduledExecutor",
            new Argument[]{Argument.of(ScheduledExecutorService.class, "executor")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.scheduled-executor"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.scheduled-executor"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setSchema",
            new Argument[]{Argument.of(String.class, "schema")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.schema"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.schema"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setExceptionOverrideClassName",
            new Argument[]{Argument.of(String.class, "exceptionOverrideClassName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.exception-override-class-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.exception-override-class-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setTransactionIsolation",
            new Argument[]{Argument.of(String.class, "isolationLevel")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.transaction-isolation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.transaction-isolation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            HikariConfig.class,
            "setThreadFactory",
            new Argument[]{Argument.of(ThreadFactory.class, "threadFactory")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.thread-factory"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.thread-factory"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "postConstruct",
            null,
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "datasources.*"),
                     "io.micronaut.context.annotation.EachProperty",
                     AnnotationUtil.mapOf("primary", "default", "value", "datasources")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("value", "datasources"),
                     "javax.inject.Scope",
                     Collections.EMPTY_MAP,
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("value", "datasources"),
                     "javax.inject.Scope",
                     Collections.EMPTY_MAP,
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.mapOf("prefix", "datasources.*"),
                     "io.micronaut.context.annotation.EachProperty",
                     AnnotationUtil.mapOf("primary", "default", "value", "datasources")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.ConfigurationReader",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty"),
                     "javax.inject.Scope",
                     AnnotationUtil.internListOf("javax.inject.Singleton"),
                     "javax.inject.Singleton",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty")
                  ),
                  false,
                  true
               ),
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            false,
            true,
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setUrl",
            new Argument[]{Argument.of(String.class, "url")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.url"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.url"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setValidationQuery",
            new Argument[]{Argument.of(String.class, "validationQuery")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.validation-query"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.validation-query"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setJndiName",
            new Argument[]{Argument.of(String.class, "jndiName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.jndi-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.jndi-name"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setDataSourceProperties",
            new Argument[]{
               Argument.of(
                  Map.class,
                  "dsProperties",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf("io.micronaut.core.convert.format.MapFormat", AnnotationUtil.mapOf("keyFormat", "RAW", "transformation", "FLAT")),
                     Collections.EMPTY_MAP,
                     Collections.EMPTY_MAP,
                     AnnotationUtil.mapOf("io.micronaut.core.convert.format.MapFormat", AnnotationUtil.mapOf("keyFormat", "RAW", "transformation", "FLAT")),
                     Collections.EMPTY_MAP,
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(String.class, "K"),
                  Argument.ofTypeVariable(Object.class, "V")
               )
            },
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-properties"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-properties"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setDataSourceProperties",
            new Argument[]{Argument.of(Properties.class, "dsProperties")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-properties"), var0
                        )
                     }
                  ),
                  "java.lang.Deprecated",
                  Collections.EMPTY_MAP
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.data-source-properties"), var0
                        )
                     }
                  ),
                  "java.lang.Deprecated",
                  Collections.EMPTY_MAP
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DatasourceConfiguration.class,
            "setAutomaticValidationQuery",
            new Argument[]{Argument.of(Boolean.TYPE, "automaticValidationQuery")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.automatic-validation-query"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.automatic-validation-query"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         )
      };
   }

   public $DatasourceConfiguration$Definition() {
      this(DatasourceConfiguration.class, $CONSTRUCTOR);
   }

   protected $DatasourceConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DatasourceConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
