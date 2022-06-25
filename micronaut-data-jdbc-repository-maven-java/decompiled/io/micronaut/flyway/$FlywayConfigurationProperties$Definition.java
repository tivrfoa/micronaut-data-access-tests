package io.micronaut.flyway;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;

// $FF: synthetic class
@Generated
class $FlywayConfigurationProperties$Definition
   extends AbstractInitializableBeanDefinition<FlywayConfigurationProperties>
   implements BeanFactory<FlywayConfigurationProperties>,
   ParametrizedBeanFactory<FlywayConfigurationProperties> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FlywayConfigurationProperties.class,
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
   public FlywayConfigurationProperties doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      FlywayConfigurationProperties var5 = new FlywayConfigurationProperties((String)var4.get("name"));
      return (FlywayConfigurationProperties)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         FlywayConfigurationProperties var4 = (FlywayConfigurationProperties)var3;
         Optional var5 = this.getValueForPath(var1, var2, Argument.of(Configuration.class, "configuration"), "flyway.datasources.*.configuration");
         if (var5.isPresent()) {
            try {
               var4.fluentConfiguration.configuration((Configuration)var5.get());
            } catch (NoSuchMethodError var152) {
            }
         }

         Optional var6 = this.getValueForPath(var1, var2, Argument.of(OutputStream.class, "dry-run-output"), "flyway.datasources.*.dry-run-output");
         if (var6.isPresent()) {
            try {
               var4.fluentConfiguration.dryRunOutput((OutputStream)var6.get());
            } catch (NoSuchMethodError var151) {
            }
         }

         Optional var7 = this.getValueForPath(var1, var2, Argument.of(File.class, "dry-run-output"), "flyway.datasources.*.dry-run-output");
         if (var7.isPresent()) {
            try {
               var4.fluentConfiguration.dryRunOutput((File)var7.get());
            } catch (NoSuchMethodError var150) {
            }
         }

         Optional var8 = this.getValueForPath(var1, var2, Argument.of(String.class, "dry-run-output"), "flyway.datasources.*.dry-run-output");
         if (var8.isPresent()) {
            try {
               var4.fluentConfiguration.dryRunOutput((String)var8.get());
            } catch (NoSuchMethodError var149) {
            }
         }

         Optional var9 = this.getValueForPath(var1, var2, Argument.of(String[].class, "error-overrides"), "flyway.datasources.*.error-overrides");
         if (var9.isPresent()) {
            try {
               var4.fluentConfiguration.errorOverrides((String[])var9.get());
            } catch (NoSuchMethodError var148) {
            }
         }

         Optional var10 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "group"), "flyway.datasources.*.group");
         if (var10.isPresent()) {
            try {
               var4.fluentConfiguration.group(var10.get());
            } catch (NoSuchMethodError var147) {
            }
         }

         Optional var11 = this.getValueForPath(var1, var2, Argument.of(String.class, "installed-by"), "flyway.datasources.*.installed-by");
         if (var11.isPresent()) {
            try {
               var4.fluentConfiguration.installedBy((String)var11.get());
            } catch (NoSuchMethodError var146) {
            }
         }

         Optional var12 = this.getValueForPath(var1, var2, Argument.of(String[].class, "loggers"), "flyway.datasources.*.loggers");
         if (var12.isPresent()) {
            try {
               var4.fluentConfiguration.loggers((String[])var12.get());
            } catch (NoSuchMethodError var145) {
            }
         }

         Optional var13 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "mixed"), "flyway.datasources.*.mixed");
         if (var13.isPresent()) {
            try {
               var4.fluentConfiguration.mixed(var13.get());
            } catch (NoSuchMethodError var144) {
            }
         }

         Optional var14 = this.getValueForPath(
            var1, var2, Argument.of(String[].class, "ignore-migration-patterns"), "flyway.datasources.*.ignore-migration-patterns"
         );
         if (var14.isPresent()) {
            try {
               var4.fluentConfiguration.ignoreMigrationPatterns((String[])var14.get());
            } catch (NoSuchMethodError var143) {
            }
         }

         Optional var15 = this.getValueForPath(
            var1, var2, Argument.of(ValidatePattern[].class, "ignore-migration-patterns"), "flyway.datasources.*.ignore-migration-patterns"
         );
         if (var15.isPresent()) {
            try {
               var4.fluentConfiguration.ignoreMigrationPatterns((ValidatePattern[])var15.get());
            } catch (NoSuchMethodError var142) {
            }
         }

         Optional var16 = this.getValueForPath(
            var1, var2, Argument.of(Boolean.TYPE, "validate-migration-naming"), "flyway.datasources.*.validate-migration-naming"
         );
         if (var16.isPresent()) {
            try {
               var4.fluentConfiguration.validateMigrationNaming(var16.get());
            } catch (NoSuchMethodError var141) {
            }
         }

         Optional var17 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "validate-on-migrate"), "flyway.datasources.*.validate-on-migrate");
         if (var17.isPresent()) {
            try {
               var4.fluentConfiguration.validateOnMigrate(var17.get());
            } catch (NoSuchMethodError var140) {
            }
         }

         Optional var18 = this.getValueForPath(
            var1, var2, Argument.of(Boolean.TYPE, "clean-on-validation-error"), "flyway.datasources.*.clean-on-validation-error"
         );
         if (var18.isPresent()) {
            try {
               var4.fluentConfiguration.cleanOnValidationError(var18.get());
            } catch (NoSuchMethodError var139) {
            }
         }

         Optional var19 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "clean-disabled"), "flyway.datasources.*.clean-disabled");
         if (var19.isPresent()) {
            try {
               var4.fluentConfiguration.cleanDisabled(var19.get());
            } catch (NoSuchMethodError var138) {
            }
         }

         Optional var20 = this.getValueForPath(var1, var2, Argument.of(String.class, "encoding"), "flyway.datasources.*.encoding");
         if (var20.isPresent()) {
            try {
               var4.fluentConfiguration.encoding((String)var20.get());
            } catch (NoSuchMethodError var137) {
            }
         }

         Optional var21 = this.getValueForPath(var1, var2, Argument.of(Charset.class, "encoding"), "flyway.datasources.*.encoding");
         if (var21.isPresent()) {
            try {
               var4.fluentConfiguration.encoding((Charset)var21.get());
            } catch (NoSuchMethodError var136) {
            }
         }

         Optional var22 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "detect-encoding"), "flyway.datasources.*.detect-encoding");
         if (var22.isPresent()) {
            try {
               var4.fluentConfiguration.detectEncoding(var22.get());
            } catch (NoSuchMethodError var135) {
            }
         }

         Optional var23 = this.getValueForPath(var1, var2, Argument.of(String.class, "default-schema"), "flyway.datasources.*.default-schema");
         if (var23.isPresent()) {
            try {
               var4.fluentConfiguration.defaultSchema((String)var23.get());
            } catch (NoSuchMethodError var134) {
            }
         }

         Optional var24 = this.getValueForPath(var1, var2, Argument.of(String[].class, "schemas"), "flyway.datasources.*.schemas");
         if (var24.isPresent()) {
            try {
               var4.fluentConfiguration.schemas((String[])var24.get());
            } catch (NoSuchMethodError var133) {
            }
         }

         Optional var25 = this.getValueForPath(var1, var2, Argument.of(String.class, "table"), "flyway.datasources.*.table");
         if (var25.isPresent()) {
            try {
               var4.fluentConfiguration.table((String)var25.get());
            } catch (NoSuchMethodError var132) {
            }
         }

         Optional var26 = this.getValueForPath(var1, var2, Argument.of(String.class, "tablespace"), "flyway.datasources.*.tablespace");
         if (var26.isPresent()) {
            try {
               var4.fluentConfiguration.tablespace((String)var26.get());
            } catch (NoSuchMethodError var131) {
            }
         }

         Optional var27 = this.getValueForPath(var1, var2, Argument.of(MigrationVersion.class, "target"), "flyway.datasources.*.target");
         if (var27.isPresent()) {
            try {
               var4.fluentConfiguration.target((MigrationVersion)var27.get());
            } catch (NoSuchMethodError var130) {
            }
         }

         Optional var28 = this.getValueForPath(var1, var2, Argument.of(String.class, "target"), "flyway.datasources.*.target");
         if (var28.isPresent()) {
            try {
               var4.fluentConfiguration.target((String)var28.get());
            } catch (NoSuchMethodError var129) {
            }
         }

         Optional var29 = this.getValueForPath(var1, var2, Argument.of(MigrationPattern[].class, "cherry-pick"), "flyway.datasources.*.cherry-pick");
         if (var29.isPresent()) {
            try {
               var4.fluentConfiguration.cherryPick((MigrationPattern[])var29.get());
            } catch (NoSuchMethodError var128) {
            }
         }

         Optional var30 = this.getValueForPath(var1, var2, Argument.of(String[].class, "cherry-pick"), "flyway.datasources.*.cherry-pick");
         if (var30.isPresent()) {
            try {
               var4.fluentConfiguration.cherryPick((String[])var30.get());
            } catch (NoSuchMethodError var127) {
            }
         }

         Optional var31 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "placeholder-replacement"), "flyway.datasources.*.placeholder-replacement");
         if (var31.isPresent()) {
            try {
               var4.fluentConfiguration.placeholderReplacement(var31.get());
            } catch (NoSuchMethodError var126) {
            }
         }

         Optional var32 = this.getValueForPath(
            var1,
            var2,
            Argument.of(Map.class, "placeholders", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(String.class, "V")),
            "flyway.datasources.*.placeholders"
         );
         if (var32.isPresent()) {
            try {
               var4.fluentConfiguration.placeholders((Map<String, String>)var32.get());
            } catch (NoSuchMethodError var125) {
            }
         }

         Optional var33 = this.getValueForPath(var1, var2, Argument.of(String.class, "placeholder-prefix"), "flyway.datasources.*.placeholder-prefix");
         if (var33.isPresent()) {
            try {
               var4.fluentConfiguration.placeholderPrefix((String)var33.get());
            } catch (NoSuchMethodError var124) {
            }
         }

         Optional var34 = this.getValueForPath(var1, var2, Argument.of(String.class, "placeholder-suffix"), "flyway.datasources.*.placeholder-suffix");
         if (var34.isPresent()) {
            try {
               var4.fluentConfiguration.placeholderSuffix((String)var34.get());
            } catch (NoSuchMethodError var123) {
            }
         }

         Optional var35 = this.getValueForPath(var1, var2, Argument.of(String.class, "placeholder-separator"), "flyway.datasources.*.placeholder-separator");
         if (var35.isPresent()) {
            try {
               var4.fluentConfiguration.placeholderSeparator((String)var35.get());
            } catch (NoSuchMethodError var122) {
            }
         }

         Optional var36 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "script-placeholder-prefix"), "flyway.datasources.*.script-placeholder-prefix"
         );
         if (var36.isPresent()) {
            try {
               var4.fluentConfiguration.scriptPlaceholderPrefix((String)var36.get());
            } catch (NoSuchMethodError var121) {
            }
         }

         Optional var37 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "script-placeholder-suffix"), "flyway.datasources.*.script-placeholder-suffix"
         );
         if (var37.isPresent()) {
            try {
               var4.fluentConfiguration.scriptPlaceholderSuffix((String)var37.get());
            } catch (NoSuchMethodError var120) {
            }
         }

         Optional var38 = this.getValueForPath(var1, var2, Argument.of(String.class, "sql-migration-prefix"), "flyway.datasources.*.sql-migration-prefix");
         if (var38.isPresent()) {
            try {
               var4.fluentConfiguration.sqlMigrationPrefix((String)var38.get());
            } catch (NoSuchMethodError var119) {
            }
         }

         Optional var39 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "baseline-migration-prefix"), "flyway.datasources.*.baseline-migration-prefix"
         );
         if (var39.isPresent()) {
            try {
               var4.fluentConfiguration.baselineMigrationPrefix((String)var39.get());
            } catch (NoSuchMethodError var118) {
            }
         }

         Optional var40 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "undo-sql-migration-prefix"), "flyway.datasources.*.undo-sql-migration-prefix"
         );
         if (var40.isPresent()) {
            try {
               var4.fluentConfiguration.undoSqlMigrationPrefix((String)var40.get());
            } catch (NoSuchMethodError var117) {
            }
         }

         Optional var41 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "repeatable-sql-migration-prefix"), "flyway.datasources.*.repeatable-sql-migration-prefix"
         );
         if (var41.isPresent()) {
            try {
               var4.fluentConfiguration.repeatableSqlMigrationPrefix((String)var41.get());
            } catch (NoSuchMethodError var116) {
            }
         }

         Optional var42 = this.getValueForPath(var1, var2, Argument.of(String.class, "sql-migration-separator"), "flyway.datasources.*.sql-migration-separator");
         if (var42.isPresent()) {
            try {
               var4.fluentConfiguration.sqlMigrationSeparator((String)var42.get());
            } catch (NoSuchMethodError var115) {
            }
         }

         Optional var43 = this.getValueForPath(var1, var2, Argument.of(String[].class, "sql-migration-suffixes"), "flyway.datasources.*.sql-migration-suffixes");
         if (var43.isPresent()) {
            try {
               var4.fluentConfiguration.sqlMigrationSuffixes((String[])var43.get());
            } catch (NoSuchMethodError var114) {
            }
         }

         Optional var44 = this.getValueForPath(var1, var2, Argument.of(JavaMigration[].class, "java-migrations"), "flyway.datasources.*.java-migrations");
         if (var44.isPresent()) {
            try {
               var4.fluentConfiguration.javaMigrations((JavaMigration[])var44.get());
            } catch (NoSuchMethodError var113) {
            }
         }

         Optional var45 = this.getValueForPath(var1, var2, Argument.of(DataSource.class, "data-source"), "flyway.datasources.*.data-source");
         if (var45.isPresent()) {
            try {
               var4.fluentConfiguration.dataSource((DataSource)var45.get());
            } catch (NoSuchMethodError var112) {
            }
         }

         Optional var46 = this.getValueForPath(var1, var2, Argument.of(Integer.TYPE, "connect-retries"), "flyway.datasources.*.connect-retries");
         if (var46.isPresent()) {
            try {
               var4.fluentConfiguration.connectRetries(var46.get());
            } catch (NoSuchMethodError var111) {
            }
         }

         Optional var47 = this.getValueForPath(
            var1, var2, Argument.of(Integer.TYPE, "connect-retries-interval"), "flyway.datasources.*.connect-retries-interval"
         );
         if (var47.isPresent()) {
            try {
               var4.fluentConfiguration.connectRetriesInterval(var47.get());
            } catch (NoSuchMethodError var110) {
            }
         }

         Optional var48 = this.getValueForPath(var1, var2, Argument.of(String.class, "init-sql"), "flyway.datasources.*.init-sql");
         if (var48.isPresent()) {
            try {
               var4.fluentConfiguration.initSql((String)var48.get());
            } catch (NoSuchMethodError var109) {
            }
         }

         Optional var49 = this.getValueForPath(var1, var2, Argument.of(MigrationVersion.class, "baseline-version"), "flyway.datasources.*.baseline-version");
         if (var49.isPresent()) {
            try {
               var4.fluentConfiguration.baselineVersion((MigrationVersion)var49.get());
            } catch (NoSuchMethodError var108) {
            }
         }

         Optional var50 = this.getValueForPath(var1, var2, Argument.of(String.class, "baseline-version"), "flyway.datasources.*.baseline-version");
         if (var50.isPresent()) {
            try {
               var4.fluentConfiguration.baselineVersion((String)var50.get());
            } catch (NoSuchMethodError var107) {
            }
         }

         Optional var51 = this.getValueForPath(var1, var2, Argument.of(String.class, "baseline-description"), "flyway.datasources.*.baseline-description");
         if (var51.isPresent()) {
            try {
               var4.fluentConfiguration.baselineDescription((String)var51.get());
            } catch (NoSuchMethodError var106) {
            }
         }

         Optional var52 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "baseline-on-migrate"), "flyway.datasources.*.baseline-on-migrate");
         if (var52.isPresent()) {
            try {
               var4.fluentConfiguration.baselineOnMigrate(var52.get());
            } catch (NoSuchMethodError var105) {
            }
         }

         Optional var53 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "out-of-order"), "flyway.datasources.*.out-of-order");
         if (var53.isPresent()) {
            try {
               var4.fluentConfiguration.outOfOrder(var53.get());
            } catch (NoSuchMethodError var104) {
            }
         }

         Optional var54 = this.getValueForPath(
            var1, var2, Argument.of(Boolean.TYPE, "skip-executing-migrations"), "flyway.datasources.*.skip-executing-migrations"
         );
         if (var54.isPresent()) {
            try {
               var4.fluentConfiguration.skipExecutingMigrations(var54.get());
            } catch (NoSuchMethodError var103) {
            }
         }

         Optional var55 = this.getValueForPath(var1, var2, Argument.of(Callback[].class, "callbacks"), "flyway.datasources.*.callbacks");
         if (var55.isPresent()) {
            try {
               var4.fluentConfiguration.callbacks((Callback[])var55.get());
            } catch (NoSuchMethodError var102) {
            }
         }

         Optional var56 = this.getValueForPath(var1, var2, Argument.of(String[].class, "callbacks"), "flyway.datasources.*.callbacks");
         if (var56.isPresent()) {
            try {
               var4.fluentConfiguration.callbacks((String[])var56.get());
            } catch (NoSuchMethodError var101) {
            }
         }

         Optional var57 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "skip-default-callbacks"), "flyway.datasources.*.skip-default-callbacks");
         if (var57.isPresent()) {
            try {
               var4.fluentConfiguration.skipDefaultCallbacks(var57.get());
            } catch (NoSuchMethodError var100) {
            }
         }

         Optional var58 = this.getValueForPath(var1, var2, Argument.of(MigrationResolver[].class, "resolvers"), "flyway.datasources.*.resolvers");
         if (var58.isPresent()) {
            try {
               var4.fluentConfiguration.resolvers((MigrationResolver[])var58.get());
            } catch (NoSuchMethodError var99) {
            }
         }

         Optional var59 = this.getValueForPath(var1, var2, Argument.of(String[].class, "resolvers"), "flyway.datasources.*.resolvers");
         if (var59.isPresent()) {
            try {
               var4.fluentConfiguration.resolvers((String[])var59.get());
            } catch (NoSuchMethodError var98) {
            }
         }

         Optional var60 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "skip-default-resolvers"), "flyway.datasources.*.skip-default-resolvers");
         if (var60.isPresent()) {
            try {
               var4.fluentConfiguration.skipDefaultResolvers(var60.get());
            } catch (NoSuchMethodError var97) {
            }
         }

         Optional var61 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "stream"), "flyway.datasources.*.stream");
         if (var61.isPresent()) {
            try {
               var4.fluentConfiguration.stream(var61.get());
            } catch (NoSuchMethodError var96) {
            }
         }

         Optional var62 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "batch"), "flyway.datasources.*.batch");
         if (var62.isPresent()) {
            try {
               var4.fluentConfiguration.batch(var62.get());
            } catch (NoSuchMethodError var95) {
            }
         }

         Optional var63 = this.getValueForPath(var1, var2, Argument.of(Integer.TYPE, "lock-retry-count"), "flyway.datasources.*.lock-retry-count");
         if (var63.isPresent()) {
            try {
               var4.fluentConfiguration.lockRetryCount(var63.get());
            } catch (NoSuchMethodError var94) {
            }
         }

         Optional var64 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "oracle-sqlplus"), "flyway.datasources.*.oracle-sqlplus");
         if (var64.isPresent()) {
            try {
               var4.fluentConfiguration.oracleSqlplus(var64.get());
            } catch (NoSuchMethodError var93) {
            }
         }

         Optional var65 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "oracle-sqlplus-warn"), "flyway.datasources.*.oracle-sqlplus-warn");
         if (var65.isPresent()) {
            try {
               var4.fluentConfiguration.oracleSqlplusWarn(var65.get());
            } catch (NoSuchMethodError var92) {
            }
         }

         Optional var66 = this.getValueForPath(var1, var2, Argument.of(String.class, "kerberos-config-file"), "flyway.datasources.*.kerberos-config-file");
         if (var66.isPresent()) {
            try {
               var4.fluentConfiguration.kerberosConfigFile((String)var66.get());
            } catch (NoSuchMethodError var91) {
            }
         }

         Optional var67 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "oracle-kerberos-config-file"), "flyway.datasources.*.oracle-kerberos-config-file"
         );
         if (var67.isPresent()) {
            try {
               var4.fluentConfiguration.oracleKerberosConfigFile((String)var67.get());
            } catch (NoSuchMethodError var90) {
            }
         }

         Optional var68 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "oracle-kerberos-cache-file"), "flyway.datasources.*.oracle-kerberos-cache-file"
         );
         if (var68.isPresent()) {
            try {
               var4.fluentConfiguration.oracleKerberosCacheFile((String)var68.get());
            } catch (NoSuchMethodError var89) {
            }
         }

         Optional var69 = this.getValueForPath(var1, var2, Argument.of(String.class, "oracle-wallet-location"), "flyway.datasources.*.oracle-wallet-location");
         if (var69.isPresent()) {
            try {
               var4.fluentConfiguration.oracleWalletLocation((String)var69.get());
            } catch (NoSuchMethodError var88) {
            }
         }

         Optional var70 = this.getValueForPath(var1, var2, Argument.of(String.class, "license-key"), "flyway.datasources.*.license-key");
         if (var70.isPresent()) {
            try {
               var4.fluentConfiguration.licenseKey((String)var70.get());
            } catch (NoSuchMethodError var87) {
            }
         }

         Optional var71 = this.getValueForPath(var1, var2, Argument.of(ResourceProvider.class, "resource-provider"), "flyway.datasources.*.resource-provider");
         if (var71.isPresent()) {
            try {
               var4.fluentConfiguration.resourceProvider((ResourceProvider)var71.get());
            } catch (NoSuchMethodError var86) {
            }
         }

         Optional var72 = this.getValueForPath(
            var1,
            var2,
            Argument.of(ClassProvider.class, "java-migration-class-provider", null, Argument.ofTypeVariable(JavaMigration.class, "I")),
            "flyway.datasources.*.java-migration-class-provider"
         );
         if (var72.isPresent()) {
            try {
               var4.fluentConfiguration.javaMigrationClassProvider((ClassProvider<JavaMigration>)var72.get());
            } catch (NoSuchMethodError var85) {
            }
         }

         Optional var73 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "output-query-results"), "flyway.datasources.*.output-query-results");
         if (var73.isPresent()) {
            try {
               var4.fluentConfiguration.outputQueryResults(var73.get());
            } catch (NoSuchMethodError var84) {
            }
         }

         Optional var74 = this.getValueForPath(var1, var2, Argument.of(Properties.class, "configuration"), "flyway.datasources.*.configuration");
         if (var74.isPresent()) {
            try {
               var4.fluentConfiguration.configuration((Properties)var74.get());
            } catch (NoSuchMethodError var83) {
            }
         }

         Optional var75 = this.getValueForPath(
            var1,
            var2,
            Argument.of(Map.class, "configuration", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(String.class, "V")),
            "flyway.datasources.*.configuration"
         );
         if (var75.isPresent()) {
            try {
               var4.fluentConfiguration.configuration((Map<String, String>)var75.get());
            } catch (NoSuchMethodError var82) {
            }
         }

         Optional var76 = this.getValueForPath(
            var1, var2, Argument.of(String.class, "load-default-configuration-files"), "flyway.datasources.*.load-default-configuration-files"
         );
         if (var76.isPresent()) {
            try {
               var4.fluentConfiguration.loadDefaultConfigurationFiles((String)var76.get());
            } catch (NoSuchMethodError var81) {
            }
         }

         Optional var77 = this.getValueForPath(var1, var2, Argument.of(Boolean.TYPE, "create-schemas"), "flyway.datasources.*.create-schemas");
         if (var77.isPresent()) {
            try {
               var4.fluentConfiguration.createSchemas(var77.get());
            } catch (NoSuchMethodError var80) {
            }
         }

         Optional var78 = this.getValueForPath(
            var1, var2, Argument.of(Boolean.TYPE, "fail-on-missing-locations"), "flyway.datasources.*.fail-on-missing-locations"
         );
         if (var78.isPresent()) {
            try {
               var4.fluentConfiguration.failOnMissingLocations(var78.get());
            } catch (NoSuchMethodError var79) {
            }
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.enabled")) {
            var4.setEnabled(super.getPropertyValueForSetter(var1, var2, "setEnabled", $INJECTION_METHODS[0].arguments[0], "flyway.datasources.*.enabled", null));
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.async")) {
            var4.setAsync(super.getPropertyValueForSetter(var1, var2, "setAsync", $INJECTION_METHODS[1].arguments[0], "flyway.datasources.*.async", null));
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.clean-schema")) {
            var4.setCleanSchema(
               super.getPropertyValueForSetter(var1, var2, "setCleanSchema", $INJECTION_METHODS[2].arguments[0], "flyway.datasources.*.clean-schema", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.url")) {
            var4.setUrl((String)super.getPropertyValueForSetter(var1, var2, "setUrl", $INJECTION_METHODS[3].arguments[0], "flyway.datasources.*.url", null));
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.user")) {
            var4.setUser((String)super.getPropertyValueForSetter(var1, var2, "setUser", $INJECTION_METHODS[4].arguments[0], "flyway.datasources.*.user", null));
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.username")) {
            var4.setUsername(
               (String)super.getPropertyValueForSetter(var1, var2, "setUsername", $INJECTION_METHODS[5].arguments[0], "flyway.datasources.*.username", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(var1, var2, "setPassword", $INJECTION_METHODS[6].arguments[0], "flyway.datasources.*.password", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "flyway.datasources.*.locations")) {
            var4.setLocations(
               (String[])super.getPropertyValueForSetter(var1, var2, "setLocations", $INJECTION_METHODS[7].arguments[0], "flyway.datasources.*.locations", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            FlywayConfigurationProperties.class,
            "setEnabled",
            new Argument[]{Argument.of(Boolean.TYPE, "enabled")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property",
                           AnnotationUtil.mapOf("name", "flyway.datasources.*.enabled"),
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.enabled"), var0)
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
            FlywayConfigurationProperties.class,
            "setAsync",
            new Argument[]{Argument.of(Boolean.TYPE, "async")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.async"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.async"), var0)
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
            FlywayConfigurationProperties.class,
            "setCleanSchema",
            new Argument[]{Argument.of(Boolean.TYPE, "cleanSchema")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.clean-schema"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.clean-schema"), var0)
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
            FlywayConfigurationProperties.class,
            "setUrl",
            new Argument[]{Argument.of(String.class, "url")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.url"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.url"), var0)
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
            FlywayConfigurationProperties.class,
            "setUser",
            new Argument[]{Argument.of(String.class, "user")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.user"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.user"), var0)
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
            FlywayConfigurationProperties.class,
            "setUsername",
            new Argument[]{Argument.of(String.class, "username")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.username"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.username"), var0)
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
            FlywayConfigurationProperties.class,
            "setPassword",
            new Argument[]{Argument.of(String.class, "password")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.password"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.password"), var0)
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
            FlywayConfigurationProperties.class,
            "setLocations",
            new Argument[]{Argument.of(String[].class, "locations")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.locations"), var0)
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
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "flyway.datasources.*.locations"), var0)
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

   public $FlywayConfigurationProperties$Definition() {
      this(FlywayConfigurationProperties.class, $CONSTRUCTOR);
   }

   protected $FlywayConfigurationProperties$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FlywayConfigurationProperties$Definition$Reference.$ANNOTATION_METADATA,
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
