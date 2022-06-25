package io.micronaut.jdbc.nativeimage;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.configure.ResourcesRegistry;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.graal.AutomaticFeatureUtils;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

@AutomaticFeature
@Internal
final class JdbcFeature implements Feature {
   private static final String H2_DRIVER = "org.h2.Driver";
   private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
   private static final String SQL_SERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
   private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
   private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
   private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
   private ResourcesRegistry resourcesRegistry;

   public void beforeAnalysis(BeforeAnalysisAccess access) {
      this.handleH2(access);
      this.handlePostgres(access);
      this.handleMariadb(access);
      this.handleOracle(access);
      this.handleSqlServer(access);
      this.handleMySql(access);
   }

   private void handleH2(BeforeAnalysisAccess access) {
      Class<?> h2Driver = access.findClassByName("org.h2.Driver");
      if (h2Driver != null) {
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "org.h2.mvstore.db.MVTableEngine");
         AutomaticFeatureUtils.registerClassForRuntimeReflection(access, "org.h2.Driver");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "org.h2.Driver");
         Collections.singletonList("org.h2.engine.Constants").forEach(s -> {
            AutomaticFeatureUtils.registerClassForRuntimeReflection(access, s);
            AutomaticFeatureUtils.registerMethodsForRuntimeReflection(access, s);
            AutomaticFeatureUtils.registerFieldsForRuntimeReflection(access, s);
         });
         Arrays.asList(
               "org.h2.store.fs.FilePathDisk",
               "org.h2.store.fs.FilePathMem",
               "org.h2.store.fs.FilePathMemLZF",
               "org.h2.store.fs.FilePathNioMem",
               "org.h2.store.fs.FilePathNioMemLZF",
               "org.h2.store.fs.FilePathSplit",
               "org.h2.store.fs.FilePathNio",
               "org.h2.store.fs.FilePathNioMapped",
               "org.h2.store.fs.FilePathAsync",
               "org.h2.store.fs.FilePathZip",
               "org.h2.store.fs.FilePathRetryOnInterrupt"
            )
            .forEach(c -> AutomaticFeatureUtils.registerClassForRuntimeReflectionAndReflectiveInstantiation(access, c));
         AutomaticFeatureUtils.addResourcePatterns("META-INF/services/java.sql.Driver", "org/h2/util/data.zip");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "java.sql.DriverManager");
      }

   }

   private void handlePostgres(BeforeAnalysisAccess access) {
      Class<?> postgresDriver = access.findClassByName("org.postgresql.Driver");
      if (postgresDriver != null) {
         AutomaticFeatureUtils.registerClassForRuntimeReflection(access, "org.postgresql.Driver");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "org.postgresql.Driver", "org.postgresql.util.SharedTimer");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "org.postgresql.PGProperty");
         AutomaticFeatureUtils.addResourcePatterns("META-INF/services/java.sql.Driver");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "java.sql.DriverManager");
      }

   }

   private void handleMariadb(BeforeAnalysisAccess access) {
      Class<?> mariaDriver = access.findClassByName("org.mariadb.jdbc.Driver");
      if (mariaDriver != null) {
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "org.mariadb.jdbc.Driver");
         AutomaticFeatureUtils.addResourcePatterns("META-INF/services/java.sql.Driver");
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "org.mariadb.jdbc.util.Options");
         AutomaticFeatureUtils.initializePackagesAtBuildTime("org.mariadb");
         AutomaticFeatureUtils.initializePackagesAtRunTime("org.mariadb.jdbc.credential.aws");
         AutomaticFeatureUtils.initializePackagesAtRunTime("org.mariadb.jdbc.internal.failover.impl");
         AutomaticFeatureUtils.initializeAtRunTime(access, "org.mariadb.jdbc.internal.com.send.authentication.SendPamAuthPacket");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "java.sql.DriverManager");
      }

   }

   private void handleOracle(BeforeAnalysisAccess access) {
      Class<?> oracleDriver = access.findClassByName("oracle.jdbc.OracleDriver");
      if (oracleDriver != null) {
         Arrays.asList(
               "oracle.jdbc.driver.T4CDriverExtension",
               "oracle.jdbc.driver.T2CDriverExtension",
               "oracle.net.ano.Ano",
               "oracle.net.ano.AuthenticationService",
               "oracle.net.ano.DataIntegrityService",
               "oracle.net.ano.EncryptionService",
               "oracle.net.ano.SupervisorService"
            )
            .forEach(c -> AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, c));
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "oracle.jdbc.logging.annotations.Supports");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "oracle.jdbc.logging.annotations.Feature");
         AutomaticFeatureUtils.addResourcePatterns(
            "META-INF/services/java.sql.Driver",
            "oracle/sql/converter_xcharset/lx20002.glb",
            "oracle/sql/converter_xcharset/lx2001f.glb",
            "oracle/sql/converter_xcharset/lx200b2.glb"
         );
         AutomaticFeatureUtils.addResourceBundles("oracle.net.jdbc.nl.mesg.NLSR", "oracle.net.mesg.Message");
         AutomaticFeatureUtils.initializeAtBuildTime(
            access,
            "oracle.net.jdbc.nl.mesg.NLSR_en",
            "oracle.jdbc.driver.DynamicByteArray",
            "oracle.jdbc.logging.annotations.Supports",
            "oracle.sql.ConverterArchive",
            "oracle.sql.converter.CharacterConverterJDBC",
            "oracle.sql.converter.CharacterConverter1Byte",
            "com.sun.jmx.mbeanserver.MBeanInstantiator",
            "com.sun.jmx.mbeanserver.MXBeanLookup",
            "com.sun.jmx.mbeanserver.Introspector",
            "com.sun.jmx.defaults.JmxProperties"
         );
         AutomaticFeatureUtils.initializeAtRunTime(access, "java.sql.DriverManager");

         try {
            String oraclePkiProvider = "oracle.security.pki.OraclePKIProvider";
            if (access.findClassByName(oraclePkiProvider) != null) {
               Class<?> providerClazz = Class.forName(oraclePkiProvider);
               RuntimeClassInitialization.initializeAtBuildTime(new String[]{"oracle.security"});
               Provider provider = (Provider)providerClazz.getConstructor().newInstance();
               Security.addProvider(provider);
               Class<?> loginClazz = access.findClassByName("oracle.security.o5logon.O5Logon");
               if (loginClazz != null) {
                  ((RuntimeClassInitializationSupport)ImageSingletons.lookup(RuntimeClassInitializationSupport.class))
                     .rerunInitialization(loginClazz, "Required for Secure Connectivity");
               }
            }

            Arrays.asList(
                  "oracle.security.crypto.cert.ext.AuthorityInfoAccessExtension",
                  "oracle.security.crypto.cert.ext.AuthorityKeyIDExtension",
                  "oracle.security.crypto.cert.ext.BasicConstraintsExtension",
                  "oracle.security.crypto.cert.ext.CRLDistPointExtension",
                  "oracle.security.crypto.cert.ext.CertificatePoliciesExtension",
                  "oracle.security.crypto.cert.ext.KeyUsageExtension",
                  "oracle.security.crypto.cert.ext.SubjectKeyIDExtension",
                  "oracle.security.crypto.core.DES_EDE",
                  "oracle.security.crypto.core.PKCS12PBE",
                  "oracle.security.crypto.core.RSAPrivateKey",
                  "oracle.security.crypto.core.RSAPublicKey",
                  "oracle.security.crypto.core.SHA",
                  "oracle.security.pki.OraclePKIProvider",
                  "oracle.security.pki.OracleSSOKeyStoreSpi"
               )
               .forEach(n -> AutomaticFeatureUtils.registerClassForRuntimeReflectionAndReflectiveInstantiation(access, n));
         } catch (Exception var7) {
            throw new RuntimeException("Unable to register OraclePKIProvider: " + var7.getMessage(), var7);
         }
      }

   }

   private void handleSqlServer(BeforeAnalysisAccess access) {
      Class<?> sqlServerDriver = access.findClassByName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      if (sqlServerDriver != null) {
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
         AutomaticFeatureUtils.initializeAtBuildTime(
            access, "com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.microsoft.sqlserver.jdbc.Util", "com.microsoft.sqlserver.jdbc.SQLServerException"
         );
         AutomaticFeatureUtils.addResourcePatterns("META-INF/services/java.sql.Driver", "javax.crypto.Cipher.class");
         AutomaticFeatureUtils.addResourceBundles("com.microsoft.sqlserver.jdbc.SQLServerResource");
         AutomaticFeatureUtils.initializeAtBuildTime(access, "java.sql.DriverManager");
      }

   }

   private void handleMySql(BeforeAnalysisAccess access) {
      Class<?> mysqlDriver = access.findClassByName("com.mysql.cj.jdbc.Driver");
      if (mysqlDriver != null) {
         Arrays.asList(
               "com.mysql.cj.exceptions.AssertionFailedException",
               "com.mysql.cj.exceptions.CJCommunicationsException",
               "com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException",
               "com.mysql.cj.exceptions.CJException",
               "com.mysql.cj.exceptions.CJOperationNotSupportedException",
               "com.mysql.cj.exceptions.CJPacketTooBigException",
               "com.mysql.cj.exceptions.CJTimeoutException",
               "com.mysql.cj.exceptions.ClosedOnExpiredPasswordException",
               "com.mysql.cj.exceptions.ConnectionIsClosedException",
               "com.mysql.cj.exceptions.DataConversionException",
               "com.mysql.cj.exceptions.DataReadException",
               "com.mysql.cj.exceptions.DataTruncationException",
               "com.mysql.cj.exceptions.FeatureNotAvailableException",
               "com.mysql.cj.exceptions.InvalidConnectionAttributeException",
               "com.mysql.cj.exceptions.MysqlErrorNumbers",
               "com.mysql.cj.exceptions.NumberOutOfRange",
               "com.mysql.cj.exceptions.OperationCancelledException",
               "com.mysql.cj.exceptions.PasswordExpiredException",
               "com.mysql.cj.exceptions.PropertyNotModifiableException",
               "com.mysql.cj.exceptions.RSAException",
               "com.mysql.cj.exceptions.SSLParamsException",
               "com.mysql.cj.exceptions.StatementIsClosedException",
               "com.mysql.cj.exceptions.UnableToConnectException",
               "com.mysql.cj.exceptions.UnsupportedConnectionStringException",
               "com.mysql.cj.exceptions.WrongArgumentExceptio"
            )
            .forEach(name -> {
               AutomaticFeatureUtils.registerClassForRuntimeReflection(access, name);
               AutomaticFeatureUtils.registerConstructorsForRuntimeReflection(access, name);
            });
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "com.mysql.cj.jdbc.Driver");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "com.mysql.cj.log.StandardLogger");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "com.mysql.cj.conf.url.SingleConnectionUrl");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "com.mysql.cj.conf.url.XDevApiConnectionUrl");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "com.mysql.cj.protocol.x.SyncFlushDeflaterOutputStream");
         AutomaticFeatureUtils.registerAllForRuntimeReflection(access, "java.util.zip.InflaterInputStream");
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "com.mysql.cj.protocol.StandardSocketFactory");
         AutomaticFeatureUtils.registerFieldsAndMethodsWithReflectiveAccess(access, "com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
         AutomaticFeatureUtils.addResourcePatterns(
            "META-INF/services/java.sql.Driver",
            "com/mysql/cj/TlsSettings.properties",
            "com/mysql/cj/LocalizedErrorMessages.properties",
            "com/mysql/cj/util/TimeZoneMapping.properties"
         );
         AutomaticFeatureUtils.addResourceBundles("com.mysql.cj.LocalizedErrorMessages");
         AutomaticFeatureUtils.initializeAtRunTime(access, "java.sql.DriverManager");
      }

   }
}
