package com.mysql.cj.conf;

import com.mysql.cj.Messages;
import com.mysql.cj.PerConnectionLRUFactory;
import com.mysql.cj.log.Log;
import com.mysql.cj.log.StandardLogger;
import com.mysql.cj.util.PerVmServerConfigCacheFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PropertyDefinitions {
   public static final String SYSP_line_separator = "line.separator";
   public static final String SYSP_java_vendor = "java.vendor";
   public static final String SYSP_java_version = "java.version";
   public static final String SYSP_java_vm_vendor = "java.vm.vendor";
   public static final String SYSP_os_name = "os.name";
   public static final String SYSP_os_arch = "os.arch";
   public static final String SYSP_os_version = "os.version";
   public static final String SYSP_file_encoding = "file.encoding";
   public static final String SYSP_disableAbandonedConnectionCleanup = "com.mysql.cj.disableAbandonedConnectionCleanup";
   public static final String SYSP_testsuite_url = "com.mysql.cj.testsuite.url";
   public static final String SYSP_testsuite_url_cluster = "com.mysql.cj.testsuite.url.cluster";
   public static final String SYSP_testsuite_url_mysqlx = "com.mysql.cj.testsuite.mysqlx.url";
   public static final String SYSP_testsuite_cantGrant = "com.mysql.cj.testsuite.cantGrant";
   public static final String SYSP_testsuite_unavailable_host = "com.mysql.cj.testsuite.unavailable.host";
   public static final String SYSP_testsuite_ds_host = "com.mysql.cj.testsuite.ds.host";
   public static final String SYSP_testsuite_ds_port = "com.mysql.cj.testsuite.ds.port";
   public static final String SYSP_testsuite_ds_db = "com.mysql.cj.testsuite.ds.db";
   public static final String SYSP_testsuite_ds_user = "com.mysql.cj.testsuite.ds.user";
   public static final String SYSP_testsuite_ds_password = "com.mysql.cj.testsuite.ds.password";
   public static final String SYSP_testsuite_loadstoreperf_tabletype = "com.mysql.cj.testsuite.loadstoreperf.tabletype";
   public static final String SYSP_testsuite_loadstoreperf_useBigResults = "com.mysql.cj.testsuite.loadstoreperf.useBigResults";
   public static final String SYSP_testsuite_miniAdminTest_runShutdown = "com.mysql.cj.testsuite.miniAdminTest.runShutdown";
   public static final String SYSP_testsuite_noDebugOutput = "com.mysql.cj.testsuite.noDebugOutput";
   public static final String SYSP_testsuite_retainArtifacts = "com.mysql.cj.testsuite.retainArtifacts";
   public static final String SYSP_testsuite_runLongTests = "com.mysql.cj.testsuite.runLongTests";
   public static final String SYSP_testsuite_serverController_basedir = "com.mysql.cj.testsuite.serverController.basedir";
   public static final String SYSP_com_mysql_cj_build_verbose = "com.mysql.cj.build.verbose";
   public static final String CATEGORY_AUTH = Messages.getString("ConnectionProperties.categoryAuthentication");
   public static final String CATEGORY_CONNECTION = Messages.getString("ConnectionProperties.categoryConnection");
   public static final String CATEGORY_SESSION = Messages.getString("ConnectionProperties.categorySession");
   public static final String CATEGORY_NETWORK = Messages.getString("ConnectionProperties.categoryNetworking");
   public static final String CATEGORY_SECURITY = Messages.getString("ConnectionProperties.categorySecurity");
   public static final String CATEGORY_STATEMENTS = Messages.getString("ConnectionProperties.categoryStatements");
   public static final String CATEGORY_PREPARED_STATEMENTS = Messages.getString("ConnectionProperties.categoryPreparedStatements");
   public static final String CATEGORY_RESULT_SETS = Messages.getString("ConnectionProperties.categoryResultSets");
   public static final String CATEGORY_METADATA = Messages.getString("ConnectionProperties.categoryMetadata");
   public static final String CATEGORY_BLOBS = Messages.getString("ConnectionProperties.categoryBlobs");
   public static final String CATEGORY_DATETIMES = Messages.getString("ConnectionProperties.categoryDatetimes");
   public static final String CATEGORY_HA = Messages.getString("ConnectionProperties.categoryHA");
   public static final String CATEGORY_PERFORMANCE = Messages.getString("ConnectionProperties.categoryPerformance");
   public static final String CATEGORY_DEBUGING_PROFILING = Messages.getString("ConnectionProperties.categoryDebuggingProfiling");
   public static final String CATEGORY_EXCEPTIONS = Messages.getString("ConnectionProperties.categoryExceptions");
   public static final String CATEGORY_INTEGRATION = Messages.getString("ConnectionProperties.categoryIntegration");
   public static final String CATEGORY_JDBC = Messages.getString("ConnectionProperties.categoryJDBC");
   public static final String CATEGORY_XDEVAPI = Messages.getString("ConnectionProperties.categoryXDevAPI");
   public static final String CATEGORY_USER_DEFINED = Messages.getString("ConnectionProperties.categoryUserDefined");
   public static final String[] PROPERTY_CATEGORIES = new String[]{
      CATEGORY_AUTH,
      CATEGORY_CONNECTION,
      CATEGORY_SESSION,
      CATEGORY_NETWORK,
      CATEGORY_SECURITY,
      CATEGORY_STATEMENTS,
      CATEGORY_PREPARED_STATEMENTS,
      CATEGORY_RESULT_SETS,
      CATEGORY_METADATA,
      CATEGORY_BLOBS,
      CATEGORY_DATETIMES,
      CATEGORY_HA,
      CATEGORY_PERFORMANCE,
      CATEGORY_DEBUGING_PROFILING,
      CATEGORY_EXCEPTIONS,
      CATEGORY_INTEGRATION,
      CATEGORY_JDBC,
      CATEGORY_XDEVAPI
   };
   public static final boolean DEFAULT_VALUE_TRUE = true;
   public static final boolean DEFAULT_VALUE_FALSE = false;
   public static final String DEFAULT_VALUE_NULL_STRING = null;
   public static final String NO_ALIAS = null;
   public static final boolean RUNTIME_MODIFIABLE = true;
   public static final boolean RUNTIME_NOT_MODIFIABLE = false;
   public static final Map<PropertyKey, PropertyDefinition<?>> PROPERTY_KEY_TO_PROPERTY_DEFINITION;

   public static PropertyDefinition<?> getPropertyDefinition(PropertyKey propertyKey) {
      return (PropertyDefinition<?>)PROPERTY_KEY_TO_PROPERTY_DEFINITION.get(propertyKey);
   }

   static {
      String STANDARD_LOGGER_NAME = StandardLogger.class.getName();
      PropertyDefinition<?>[] pdefs = new PropertyDefinition[]{
         new StringPropertyDefinition(
            PropertyKey.USER,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.Username"),
            Messages.getString("ConnectionProperties.allVersions"),
            CATEGORY_AUTH,
            -2147483647
         ),
         new StringPropertyDefinition(
            PropertyKey.PASSWORD,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.Password"),
            Messages.getString("ConnectionProperties.allVersions"),
            CATEGORY_AUTH,
            -2147483646
         ),
         new StringPropertyDefinition(
            PropertyKey.password1, DEFAULT_VALUE_NULL_STRING, false, Messages.getString("ConnectionProperties.Password1"), "8.0.28", CATEGORY_AUTH, -2147483645
         ),
         new StringPropertyDefinition(
            PropertyKey.password2, DEFAULT_VALUE_NULL_STRING, false, Messages.getString("ConnectionProperties.Password2"), "8.0.28", CATEGORY_AUTH, -2147483644
         ),
         new StringPropertyDefinition(
            PropertyKey.password3, DEFAULT_VALUE_NULL_STRING, false, Messages.getString("ConnectionProperties.Password3"), "8.0.28", CATEGORY_AUTH, -2147483643
         ),
         new StringPropertyDefinition(
            PropertyKey.authenticationPlugins,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.authenticationPlugins"),
            "5.1.19",
            CATEGORY_AUTH,
            -2147483642
         ),
         new StringPropertyDefinition(
            PropertyKey.disabledAuthenticationPlugins,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.disabledAuthenticationPlugins"),
            "5.1.19",
            CATEGORY_AUTH,
            -2147483641
         ),
         new StringPropertyDefinition(
            PropertyKey.defaultAuthenticationPlugin,
            "mysql_native_password",
            true,
            Messages.getString("ConnectionProperties.defaultAuthenticationPlugin"),
            "5.1.19",
            CATEGORY_AUTH,
            -2147483640
         ),
         new StringPropertyDefinition(
            PropertyKey.ldapServerHostname,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.ldapServerHostname"),
            "8.0.23",
            CATEGORY_AUTH,
            -2147483639
         ),
         new StringPropertyDefinition(
            PropertyKey.ociConfigFile,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.ociConfigFile"),
            "8.0.27",
            CATEGORY_AUTH,
            -2147483641
         ),
         new StringPropertyDefinition(
            PropertyKey.authenticationFidoCallbackHandler,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.authenticationFidoCallbackHandler"),
            "8.0.29",
            CATEGORY_AUTH,
            -2147483640
         ),
         new StringPropertyDefinition(
            PropertyKey.passwordCharacterEncoding,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.passwordCharacterEncoding"),
            "5.1.7",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.connectionAttributes,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.connectionAttributes"),
            "5.1.25",
            CATEGORY_CONNECTION,
            7
         ),
         new StringPropertyDefinition(
            PropertyKey.clientInfoProvider,
            "com.mysql.cj.jdbc.CommentClientInfoProvider",
            true,
            Messages.getString("ConnectionProperties.clientInfoProvider"),
            "5.1.0",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.connectionLifecycleInterceptors,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.connectionLifecycleInterceptors"),
            "5.1.4",
            CATEGORY_CONNECTION,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.createDatabaseIfNotExist,
            false,
            true,
            Messages.getString("ConnectionProperties.createDatabaseIfNotExist"),
            "3.1.9",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.interactiveClient,
            false,
            false,
            Messages.getString("ConnectionProperties.interactiveClient"),
            "3.1.0",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.propertiesTransform,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.connectionPropertiesTransform"),
            "3.1.4",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.rollbackOnPooledClose,
            true,
            true,
            Messages.getString("ConnectionProperties.rollbackOnPooledClose"),
            "3.0.15",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.useConfigs,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.useConfigs"),
            "3.1.5",
            CATEGORY_CONNECTION,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useAffectedRows,
            false,
            true,
            Messages.getString("ConnectionProperties.useAffectedRows"),
            "5.1.7",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.disconnectOnExpiredPasswords,
            true,
            true,
            Messages.getString("ConnectionProperties.disconnectOnExpiredPasswords"),
            "5.1.23",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.detectCustomCollations,
            false,
            true,
            Messages.getString("ConnectionProperties.detectCustomCollations"),
            "5.1.29",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new EnumPropertyDefinition(
            PropertyKey.databaseTerm,
            PropertyDefinitions.DatabaseTerm.CATALOG,
            true,
            Messages.getString("ConnectionProperties.databaseTerm"),
            "8.0.17",
            CATEGORY_CONNECTION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.characterEncoding,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.characterEncoding"),
            "1.1g",
            CATEGORY_SESSION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.characterSetResults,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.characterSetResults"),
            "3.0.13",
            CATEGORY_SESSION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.customCharsetMapping,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.customCharsetMapping"),
            "8.0.26",
            CATEGORY_SESSION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.connectionCollation,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.connectionCollation"),
            "3.0.13",
            CATEGORY_SESSION,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.sessionVariables,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.sessionVariables"),
            "3.1.8",
            CATEGORY_SESSION,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.trackSessionState,
            false,
            true,
            Messages.getString("ConnectionProperties.trackSessionState"),
            "8.0.26",
            CATEGORY_SESSION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useUnbufferedInput,
            true,
            true,
            Messages.getString("ConnectionProperties.useUnbufferedInput"),
            "3.0.11",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.connectTimeout, 0, true, Messages.getString("ConnectionProperties.connectTimeout"), "3.0.1", CATEGORY_NETWORK, 9, 0, Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.localSocketAddress,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.localSocketAddress"),
            "5.0.5",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.socketFactory,
            "com.mysql.cj.protocol.StandardSocketFactory",
            true,
            Messages.getString("ConnectionProperties.socketFactory"),
            "3.0.3",
            CATEGORY_NETWORK,
            4
         ),
         new StringPropertyDefinition(
            PropertyKey.socksProxyHost,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.socksProxyHost"),
            "5.1.34",
            CATEGORY_NETWORK,
            1
         ),
         new IntegerPropertyDefinition(
            PropertyKey.socksProxyPort, 1080, true, Messages.getString("ConnectionProperties.socksProxyPort"), "5.1.34", CATEGORY_NETWORK, 2, 0, 65535
         ),
         new BooleanPropertyDefinition(
            PropertyKey.socksProxyRemoteDns,
            false,
            true,
            Messages.getString("ConnectionProperties.socksProxyRemoteDns"),
            "8.0.29",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.socketTimeout, 0, true, Messages.getString("ConnectionProperties.socketTimeout"), "3.0.1", CATEGORY_NETWORK, 10, 0, Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.tcpNoDelay, true, true, Messages.getString("ConnectionProperties.tcpNoDelay"), "5.0.7", CATEGORY_NETWORK, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.tcpKeepAlive, true, true, Messages.getString("ConnectionProperties.tcpKeepAlive"), "5.0.7", CATEGORY_NETWORK, Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.tcpRcvBuf,
            0,
            true,
            Messages.getString("ConnectionProperties.tcpSoRcvBuf"),
            "5.0.7",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.tcpSndBuf,
            0,
            true,
            Messages.getString("ConnectionProperties.tcpSoSndBuf"),
            "5.0.7",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.tcpTrafficClass,
            0,
            true,
            Messages.getString("ConnectionProperties.tcpTrafficClass"),
            "5.0.7",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE,
            0,
            255
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useCompression, false, true, Messages.getString("ConnectionProperties.useCompression"), "3.0.17", CATEGORY_NETWORK, Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.maxAllowedPacket,
            65535,
            true,
            Messages.getString("ConnectionProperties.maxAllowedPacket"),
            "5.1.8",
            CATEGORY_NETWORK,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.dnsSrv, false, false, Messages.getString("ConnectionProperties.dnsSrv"), "8.0.19", CATEGORY_NETWORK, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(PropertyKey.paranoid, false, false, Messages.getString("ConnectionProperties.paranoid"), "3.0.1", CATEGORY_SECURITY, 1),
         new StringPropertyDefinition(
            PropertyKey.serverRSAPublicKeyFile,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.serverRSAPublicKeyFile"),
            "5.1.31",
            CATEGORY_SECURITY,
            2
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowPublicKeyRetrieval,
            false,
            true,
            Messages.getString("ConnectionProperties.allowPublicKeyRetrieval"),
            "5.1.31",
            CATEGORY_SECURITY,
            3
         ),
         new EnumPropertyDefinition(
            PropertyKey.sslMode,
            PropertyDefinitions.SslMode.PREFERRED,
            true,
            Messages.getString("ConnectionProperties.sslMode"),
            "8.0.13",
            CATEGORY_SECURITY,
            4
         ),
         new StringPropertyDefinition(
            PropertyKey.trustCertificateKeyStoreUrl,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.trustCertificateKeyStoreUrl"),
            "5.1.0",
            CATEGORY_SECURITY,
            5
         ),
         new StringPropertyDefinition(
            PropertyKey.trustCertificateKeyStoreType,
            "JKS",
            true,
            Messages.getString("ConnectionProperties.trustCertificateKeyStoreType"),
            "5.1.0",
            CATEGORY_SECURITY,
            6
         ),
         new StringPropertyDefinition(
            PropertyKey.trustCertificateKeyStorePassword,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.trustCertificateKeyStorePassword"),
            "5.1.0",
            CATEGORY_SECURITY,
            7
         ),
         new BooleanPropertyDefinition(
            PropertyKey.fallbackToSystemTrustStore,
            true,
            true,
            Messages.getString("ConnectionProperties.fallbackToSystemTrustStore"),
            "8.0.22",
            CATEGORY_SECURITY,
            8
         ),
         new StringPropertyDefinition(
            PropertyKey.clientCertificateKeyStoreUrl,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.clientCertificateKeyStoreUrl"),
            "5.1.0",
            CATEGORY_SECURITY,
            9
         ),
         new StringPropertyDefinition(
            PropertyKey.clientCertificateKeyStoreType,
            "JKS",
            true,
            Messages.getString("ConnectionProperties.clientCertificateKeyStoreType"),
            "5.1.0",
            CATEGORY_SECURITY,
            10
         ),
         new StringPropertyDefinition(
            PropertyKey.clientCertificateKeyStorePassword,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.clientCertificateKeyStorePassword"),
            "5.1.0",
            CATEGORY_SECURITY,
            11
         ),
         new BooleanPropertyDefinition(
            PropertyKey.fallbackToSystemKeyStore,
            true,
            true,
            Messages.getString("ConnectionProperties.fallbackToSystemKeyStore"),
            "8.0.22",
            CATEGORY_SECURITY,
            12
         ),
         new StringPropertyDefinition(
            PropertyKey.tlsCiphersuites,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.tlsCiphersuites"),
            "5.1.35",
            CATEGORY_SECURITY,
            13
         ),
         new StringPropertyDefinition(
            PropertyKey.tlsVersions, DEFAULT_VALUE_NULL_STRING, true, Messages.getString("ConnectionProperties.tlsVersions"), "8.0.8", CATEGORY_SECURITY, 14
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowLoadLocalInfile,
            false,
            true,
            Messages.getString("ConnectionProperties.loadDataLocal"),
            "3.0.3",
            CATEGORY_SECURITY,
            Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.allowLoadLocalInfileInPath,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.loadDataLocalInPath"),
            "8.0.22",
            CATEGORY_SECURITY,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowMultiQueries,
            false,
            true,
            Messages.getString("ConnectionProperties.allowMultiQueries"),
            "3.1.1",
            CATEGORY_SECURITY,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowUrlInLocalInfile,
            false,
            true,
            Messages.getString("ConnectionProperties.allowUrlInLoadLocal"),
            "3.1.4",
            CATEGORY_SECURITY,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useSSL, true, true, Messages.getString("ConnectionProperties.useSSL"), "3.0.2", CATEGORY_SECURITY, Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.requireSSL, false, true, Messages.getString("ConnectionProperties.requireSSL"), "3.1.0", CATEGORY_SECURITY, Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.verifyServerCertificate,
            false,
            true,
            Messages.getString("ConnectionProperties.verifyServerCertificate"),
            "5.1.6",
            CATEGORY_SECURITY,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.continueBatchOnError,
            true,
            true,
            Messages.getString("ConnectionProperties.continueBatchOnError"),
            "3.0.3",
            CATEGORY_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.dontTrackOpenResources,
            false,
            true,
            Messages.getString("ConnectionProperties.dontTrackOpenResources"),
            "3.1.7",
            CATEGORY_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.queryTimeoutKillsConnection,
            false,
            true,
            Messages.getString("ConnectionProperties.queryTimeoutKillsConnection"),
            "5.1.9",
            CATEGORY_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.queryInterceptors,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.queryInterceptors"),
            "8.0.7",
            CATEGORY_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.cacheDefaultTimeZone,
            true,
            true,
            Messages.getString("ConnectionProperties.cacheDefaultTimeZone"),
            "8.0.20",
            CATEGORY_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowNanAndInf,
            false,
            true,
            Messages.getString("ConnectionProperties.allowNANandINF"),
            "3.1.5",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.autoClosePStmtStreams,
            false,
            true,
            Messages.getString("ConnectionProperties.autoClosePstmtStreams"),
            "3.1.12",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.compensateOnDuplicateKeyUpdateCounts,
            false,
            true,
            Messages.getString("ConnectionProperties.compensateOnDuplicateKeyUpdateCounts"),
            "5.1.7",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useServerPrepStmts,
            false,
            true,
            Messages.getString("ConnectionProperties.useServerPrepStmts"),
            "3.1.0",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.emulateUnsupportedPstmts,
            true,
            true,
            Messages.getString("ConnectionProperties.emulateUnsupportedPstmts"),
            "3.1.7",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.generateSimpleParameterMetadata,
            false,
            true,
            Messages.getString("ConnectionProperties.generateSimpleParameterMetadata"),
            "5.0.5",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.processEscapeCodesForPrepStmts,
            true,
            true,
            Messages.getString("ConnectionProperties.processEscapeCodesForPrepStmts"),
            "3.1.12",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useStreamLengthsInPrepStmts,
            true,
            true,
            Messages.getString("ConnectionProperties.useStreamLengthsInPrepStmts"),
            "3.0.2",
            CATEGORY_PREPARED_STATEMENTS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.clobberStreamingResults,
            false,
            true,
            Messages.getString("ConnectionProperties.clobberStreamingResults"),
            "3.0.9",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.emptyStringsConvertToZero,
            true,
            true,
            Messages.getString("ConnectionProperties.emptyStringsConvertToZero"),
            "3.1.8",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.holdResultsOpenOverStatementClose,
            false,
            true,
            Messages.getString("ConnectionProperties.holdRSOpenOverStmtClose"),
            "3.1.7",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.jdbcCompliantTruncation,
            true,
            true,
            Messages.getString("ConnectionProperties.jdbcCompliantTruncation"),
            "3.1.2",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.maxRows,
            -1,
            true,
            Messages.getString("ConnectionProperties.maxRows"),
            Messages.getString("ConnectionProperties.allVersions"),
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE,
            -1,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.netTimeoutForStreamingResults,
            600,
            true,
            Messages.getString("ConnectionProperties.netTimeoutForStreamingResults"),
            "5.1.0",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.padCharsWithSpace,
            false,
            true,
            Messages.getString("ConnectionProperties.padCharsWithSpace"),
            "5.0.6",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.populateInsertRowWithDefaultValues,
            false,
            true,
            Messages.getString("ConnectionProperties.populateInsertRowWithDefaultValues"),
            "5.0.5",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.strictUpdates, true, true, Messages.getString("ConnectionProperties.strictUpdates"), "3.0.4", CATEGORY_RESULT_SETS, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.tinyInt1isBit, true, true, Messages.getString("ConnectionProperties.tinyInt1isBit"), "3.0.16", CATEGORY_RESULT_SETS, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.transformedBitIsBoolean,
            false,
            true,
            Messages.getString("ConnectionProperties.transformedBitIsBoolean"),
            "3.1.9",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.scrollTolerantForwardOnly,
            false,
            false,
            Messages.getString("ConnectionProperties.scrollTolerantForwardOnly"),
            "8.0.24",
            CATEGORY_RESULT_SETS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.noAccessToProcedureBodies,
            false,
            true,
            Messages.getString("ConnectionProperties.noAccessToProcedureBodies"),
            "5.0.3",
            CATEGORY_METADATA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.nullDatabaseMeansCurrent,
            false,
            true,
            Messages.getString("ConnectionProperties.nullCatalogMeansCurrent"),
            "3.1.8",
            CATEGORY_METADATA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useHostsInPrivileges,
            true,
            true,
            Messages.getString("ConnectionProperties.useHostsInPrivileges"),
            "3.0.2",
            CATEGORY_METADATA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useInformationSchema,
            false,
            true,
            Messages.getString("ConnectionProperties.useInformationSchema"),
            "5.0.0",
            CATEGORY_METADATA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.getProceduresReturnsFunctions,
            true,
            true,
            Messages.getString("ConnectionProperties.getProceduresReturnsFunctions"),
            "5.1.26",
            CATEGORY_METADATA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.autoDeserialize, false, true, Messages.getString("ConnectionProperties.autoDeserialize"), "3.1.5", CATEGORY_BLOBS, Integer.MIN_VALUE
         ),
         new MemorySizePropertyDefinition(
            PropertyKey.blobSendChunkSize,
            1048576,
            true,
            Messages.getString("ConnectionProperties.blobSendChunkSize"),
            "3.1.9",
            CATEGORY_BLOBS,
            Integer.MIN_VALUE,
            0,
            0
         ),
         new BooleanPropertyDefinition(
            PropertyKey.blobsAreStrings, false, true, Messages.getString("ConnectionProperties.blobsAreStrings"), "5.0.8", CATEGORY_BLOBS, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.functionsNeverReturnBlobs,
            false,
            true,
            Messages.getString("ConnectionProperties.functionsNeverReturnBlobs"),
            "5.0.8",
            CATEGORY_BLOBS,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.clobCharacterEncoding,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.clobCharacterEncoding"),
            "5.0.0",
            CATEGORY_BLOBS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.emulateLocators, false, true, Messages.getString("ConnectionProperties.emulateLocators"), "3.1.0", CATEGORY_BLOBS, Integer.MIN_VALUE
         ),
         new MemorySizePropertyDefinition(
            PropertyKey.locatorFetchBufferSize,
            1048576,
            true,
            Messages.getString("ConnectionProperties.locatorFetchBufferSize"),
            "3.2.1",
            CATEGORY_BLOBS,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.noDatetimeStringSync,
            false,
            true,
            Messages.getString("ConnectionProperties.noDatetimeStringSync"),
            "3.1.7",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.connectionTimeZone,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.connectionTimeZone"),
            "3.0.2",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.forceConnectionTimeZoneToSession,
            false,
            true,
            Messages.getString("ConnectionProperties.forceConnectionTimeZoneToSession"),
            "8.0.23",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.preserveInstants,
            true,
            true,
            Messages.getString("ConnectionProperties.preserveInstants"),
            "8.0.23",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.treatUtilDateAsTimestamp,
            true,
            true,
            Messages.getString("ConnectionProperties.treatUtilDateAsTimestamp"),
            "5.0.5",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.sendFractionalSeconds,
            true,
            true,
            Messages.getString("ConnectionProperties.sendFractionalSeconds"),
            "5.1.37",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.sendFractionalSecondsForTime,
            true,
            true,
            Messages.getString("ConnectionProperties.sendFractionalSecondsForTime"),
            "8.0.23",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.yearIsDateType, true, true, Messages.getString("ConnectionProperties.yearIsDateType"), "3.1.9", CATEGORY_DATETIMES, Integer.MIN_VALUE
         ),
         new EnumPropertyDefinition(
            PropertyKey.zeroDateTimeBehavior,
            PropertyDefinitions.ZeroDatetimeBehavior.EXCEPTION,
            true,
            Messages.getString(
               "ConnectionProperties.zeroDateTimeBehavior",
               new Object[]{
                  PropertyDefinitions.ZeroDatetimeBehavior.EXCEPTION,
                  PropertyDefinitions.ZeroDatetimeBehavior.ROUND,
                  PropertyDefinitions.ZeroDatetimeBehavior.CONVERT_TO_NULL
               }
            ),
            "3.1.4",
            CATEGORY_DATETIMES,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowSourceDownConnections,
            false,
            true,
            Messages.getString("ConnectionProperties.allowSourceDownConnections"),
            "5.1.27",
            CATEGORY_HA,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.allowReplicaDownConnections,
            false,
            true,
            Messages.getString("ConnectionProperties.allowReplicaDownConnections"),
            "6.0.2",
            CATEGORY_HA,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.readFromSourceWhenNoReplicas,
            false,
            true,
            Messages.getString("ConnectionProperties.readFromSourceWhenNoReplicas"),
            "6.0.2",
            CATEGORY_HA,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(PropertyKey.autoReconnect, false, true, Messages.getString("ConnectionProperties.autoReconnect"), "1.1", CATEGORY_HA, 0),
         new BooleanPropertyDefinition(
            PropertyKey.autoReconnectForPools, false, true, Messages.getString("ConnectionProperties.autoReconnectForPools"), "3.1.3", CATEGORY_HA, 1
         ),
         new BooleanPropertyDefinition(
            PropertyKey.failOverReadOnly, true, true, Messages.getString("ConnectionProperties.failoverReadOnly"), "3.0.12", CATEGORY_HA, 2
         ),
         new IntegerPropertyDefinition(
            PropertyKey.initialTimeout, 2, false, Messages.getString("ConnectionProperties.initialTimeout"), "1.1", CATEGORY_HA, 5, 1, Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.ha_loadBalanceStrategy,
            "random",
            true,
            Messages.getString("ConnectionProperties.loadBalanceStrategy"),
            "5.0.6",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.loadBalanceBlocklistTimeout,
            0,
            true,
            Messages.getString("ConnectionProperties.loadBalanceBlocklistTimeout"),
            "5.1.0",
            CATEGORY_HA,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.loadBalancePingTimeout,
            0,
            true,
            Messages.getString("ConnectionProperties.loadBalancePingTimeout"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.loadBalanceValidateConnectionOnSwapServer,
            false,
            true,
            Messages.getString("ConnectionProperties.loadBalanceValidateConnectionOnSwapServer"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.loadBalanceConnectionGroup,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.loadBalanceConnectionGroup"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.loadBalanceExceptionChecker,
            "com.mysql.cj.jdbc.ha.StandardLoadBalanceExceptionChecker",
            true,
            Messages.getString("ConnectionProperties.loadBalanceExceptionChecker"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.loadBalanceSQLStateFailover,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.loadBalanceSQLStateFailover"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.loadBalanceSQLExceptionSubclassFailover,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.loadBalanceSQLExceptionSubclassFailover"),
            "5.1.13",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.loadBalanceAutoCommitStatementRegex,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementRegex"),
            "5.1.15",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.loadBalanceAutoCommitStatementThreshold,
            0,
            true,
            Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementThreshold"),
            "5.1.15",
            CATEGORY_HA,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.maxReconnects, 3, true, Messages.getString("ConnectionProperties.maxReconnects"), "1.1", CATEGORY_HA, 4, 1, Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.retriesAllDown, 120, true, Messages.getString("ConnectionProperties.retriesAllDown"), "5.1.6", CATEGORY_HA, 4, 0, Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.pinGlobalTxToPhysicalConnection,
            false,
            true,
            Messages.getString("ConnectionProperties.pinGlobalTxToPhysicalConnection"),
            "5.0.1",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.queriesBeforeRetrySource,
            50,
            true,
            Messages.getString("ConnectionProperties.queriesBeforeRetrySource"),
            "3.0.2",
            CATEGORY_HA,
            7,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.reconnectAtTxEnd, false, true, Messages.getString("ConnectionProperties.reconnectAtTxEnd"), "3.0.10", CATEGORY_HA, 4
         ),
         new StringPropertyDefinition(
            PropertyKey.replicationConnectionGroup,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.replicationConnectionGroup"),
            "8.0.7",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.resourceId,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.resourceId"),
            "5.0.1",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.secondsBeforeRetrySource,
            30,
            true,
            Messages.getString("ConnectionProperties.secondsBeforeRetrySource"),
            "3.0.2",
            CATEGORY_HA,
            8,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.selfDestructOnPingSecondsLifetime,
            0,
            true,
            Messages.getString("ConnectionProperties.selfDestructOnPingSecondsLifetime"),
            "5.1.6",
            CATEGORY_HA,
            Integer.MAX_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.selfDestructOnPingMaxOperations,
            0,
            true,
            Messages.getString("ConnectionProperties.selfDestructOnPingMaxOperations"),
            "5.1.6",
            CATEGORY_HA,
            Integer.MAX_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.ha_enableJMX, false, true, Messages.getString("ConnectionProperties.ha.enableJMX"), "5.1.27", CATEGORY_HA, Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.loadBalanceHostRemovalGracePeriod,
            15000,
            true,
            Messages.getString("ConnectionProperties.loadBalanceHostRemovalGracePeriod"),
            "6.0.3",
            CATEGORY_HA,
            Integer.MAX_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.serverAffinityOrder,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.serverAffinityOrder"),
            "8.0.8",
            CATEGORY_HA,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.alwaysSendSetIsolation,
            true,
            true,
            Messages.getString("ConnectionProperties.alwaysSendSetIsolation"),
            "3.1.7",
            CATEGORY_PERFORMANCE,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.cacheCallableStmts,
            false,
            true,
            Messages.getString("ConnectionProperties.cacheCallableStatements"),
            "3.1.2",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.cachePrepStmts,
            false,
            true,
            Messages.getString("ConnectionProperties.cachePrepStmts"),
            "3.0.10",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.cacheResultSetMetadata,
            false,
            true,
            Messages.getString("ConnectionProperties.cacheRSMetadata"),
            "3.1.1",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.serverConfigCacheFactory,
            PerVmServerConfigCacheFactory.class.getName(),
            true,
            Messages.getString("ConnectionProperties.serverConfigCacheFactory"),
            "5.1.1",
            CATEGORY_PERFORMANCE,
            12
         ),
         new BooleanPropertyDefinition(
            PropertyKey.cacheServerConfiguration,
            false,
            true,
            Messages.getString("ConnectionProperties.cacheServerConfiguration"),
            "3.1.5",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.callableStmtCacheSize,
            100,
            true,
            Messages.getString("ConnectionProperties.callableStmtCacheSize"),
            "3.1.2",
            CATEGORY_PERFORMANCE,
            5,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.defaultFetchSize,
            0,
            true,
            Messages.getString("ConnectionProperties.defaultFetchSize"),
            "3.1.9",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.elideSetAutoCommits,
            false,
            true,
            Messages.getString("ConnectionProperties.eliseSetAutoCommit"),
            "3.1.3",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.enableQueryTimeouts,
            true,
            true,
            Messages.getString("ConnectionProperties.enableQueryTimeouts"),
            "5.0.6",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new MemorySizePropertyDefinition(
            PropertyKey.largeRowSizeThreshold,
            2048,
            true,
            Messages.getString("ConnectionProperties.largeRowSizeThreshold"),
            "5.1.1",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.maintainTimeStats,
            true,
            true,
            Messages.getString("ConnectionProperties.maintainTimeStats"),
            "3.1.9",
            CATEGORY_PERFORMANCE,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.metadataCacheSize,
            50,
            true,
            Messages.getString("ConnectionProperties.metadataCacheSize"),
            "3.1.1",
            CATEGORY_PERFORMANCE,
            5,
            1,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.prepStmtCacheSize,
            25,
            true,
            Messages.getString("ConnectionProperties.prepStmtCacheSize"),
            "3.0.10",
            CATEGORY_PERFORMANCE,
            10,
            0,
            Integer.MAX_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.prepStmtCacheSqlLimit,
            256,
            true,
            Messages.getString("ConnectionProperties.prepStmtCacheSqlLimit"),
            "3.0.10",
            CATEGORY_PERFORMANCE,
            11,
            1,
            Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.queryInfoCacheFactory,
            PerConnectionLRUFactory.class.getName(),
            true,
            Messages.getString("ConnectionProperties.queryInfoCacheFactory"),
            "5.1.1",
            CATEGORY_PERFORMANCE,
            12
         ),
         new BooleanPropertyDefinition(
            PropertyKey.rewriteBatchedStatements,
            false,
            true,
            Messages.getString("ConnectionProperties.rewriteBatchedStatements"),
            "3.1.13",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useCursorFetch,
            false,
            true,
            Messages.getString("ConnectionProperties.useCursorFetch"),
            "5.0.0",
            CATEGORY_PERFORMANCE,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useLocalSessionState, false, true, Messages.getString("ConnectionProperties.useLocalSessionState"), "3.1.7", CATEGORY_PERFORMANCE, 5
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useLocalTransactionState,
            false,
            true,
            Messages.getString("ConnectionProperties.useLocalTransactionState"),
            "5.1.7",
            CATEGORY_PERFORMANCE,
            6
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useReadAheadInput,
            true,
            true,
            Messages.getString("ConnectionProperties.useReadAheadInput"),
            "3.1.5",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.dontCheckOnDuplicateKeyUpdateInSQL,
            false,
            true,
            Messages.getString("ConnectionProperties.dontCheckOnDuplicateKeyUpdateInSQL"),
            "5.1.32",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.readOnlyPropagatesToServer,
            true,
            true,
            Messages.getString("ConnectionProperties.readOnlyPropagatesToServer"),
            "5.1.35",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.enableEscapeProcessing,
            true,
            true,
            Messages.getString("ConnectionProperties.enableEscapeProcessing"),
            "6.0.1",
            CATEGORY_PERFORMANCE,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.logger,
            STANDARD_LOGGER_NAME,
            true,
            Messages.getString("ConnectionProperties.logger", new Object[]{Log.class.getName(), STANDARD_LOGGER_NAME}),
            "3.1.1",
            CATEGORY_DEBUGING_PROFILING,
            0
         ),
         new StringPropertyDefinition(
            PropertyKey.profilerEventHandler,
            "com.mysql.cj.log.LoggingProfilerEventHandler",
            true,
            Messages.getString("ConnectionProperties.profilerEventHandler"),
            "5.1.6",
            CATEGORY_DEBUGING_PROFILING,
            1
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useNanosForElapsedTime,
            false,
            true,
            Messages.getString("ConnectionProperties.useNanosForElapsedTime"),
            "5.0.7",
            CATEGORY_DEBUGING_PROFILING,
            2
         ),
         new IntegerPropertyDefinition(
            PropertyKey.maxQuerySizeToLog,
            2048,
            true,
            Messages.getString("ConnectionProperties.maxQuerySizeToLog"),
            "3.1.3",
            CATEGORY_DEBUGING_PROFILING,
            3,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.profileSQL, false, true, Messages.getString("ConnectionProperties.profileSQL"), "3.1.0", CATEGORY_DEBUGING_PROFILING, 4
         ),
         new BooleanPropertyDefinition(
            PropertyKey.logSlowQueries, false, true, Messages.getString("ConnectionProperties.logSlowQueries"), "3.1.2", CATEGORY_DEBUGING_PROFILING, 5
         ),
         new IntegerPropertyDefinition(
            PropertyKey.slowQueryThresholdMillis,
            2000,
            true,
            Messages.getString("ConnectionProperties.slowQueryThresholdMillis"),
            "3.1.2",
            CATEGORY_DEBUGING_PROFILING,
            6,
            0,
            Integer.MAX_VALUE
         ),
         new LongPropertyDefinition(
            PropertyKey.slowQueryThresholdNanos,
            0L,
            true,
            Messages.getString("ConnectionProperties.slowQueryThresholdNanos"),
            "5.0.7",
            CATEGORY_DEBUGING_PROFILING,
            7
         ),
         new BooleanPropertyDefinition(
            PropertyKey.autoSlowLog, true, true, Messages.getString("ConnectionProperties.autoSlowLog"), "5.1.4", CATEGORY_DEBUGING_PROFILING, 8
         ),
         new BooleanPropertyDefinition(
            PropertyKey.explainSlowQueries, false, true, Messages.getString("ConnectionProperties.explainSlowQueries"), "3.1.2", CATEGORY_DEBUGING_PROFILING, 9
         ),
         new BooleanPropertyDefinition(
            PropertyKey.gatherPerfMetrics, false, true, Messages.getString("ConnectionProperties.gatherPerfMetrics"), "3.1.2", CATEGORY_DEBUGING_PROFILING, 10
         ),
         new IntegerPropertyDefinition(
            PropertyKey.reportMetricsIntervalMillis,
            30000,
            true,
            Messages.getString("ConnectionProperties.reportMetricsIntervalMillis"),
            "3.1.2",
            CATEGORY_DEBUGING_PROFILING,
            11,
            0,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.logXaCommands, false, true, Messages.getString("ConnectionProperties.logXaCommands"), "5.0.5", CATEGORY_DEBUGING_PROFILING, 12
         ),
         new BooleanPropertyDefinition(
            PropertyKey.traceProtocol, false, true, Messages.getString("ConnectionProperties.traceProtocol"), "3.1.2", CATEGORY_DEBUGING_PROFILING, 13
         ),
         new BooleanPropertyDefinition(
            PropertyKey.enablePacketDebug, false, true, Messages.getString("ConnectionProperties.enablePacketDebug"), "3.1.3", CATEGORY_DEBUGING_PROFILING, 14
         ),
         new IntegerPropertyDefinition(
            PropertyKey.packetDebugBufferSize,
            20,
            true,
            Messages.getString("ConnectionProperties.packetDebugBufferSize"),
            "3.1.3",
            CATEGORY_DEBUGING_PROFILING,
            15,
            1,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useUsageAdvisor, false, true, Messages.getString("ConnectionProperties.useUsageAdvisor"), "3.1.1", CATEGORY_DEBUGING_PROFILING, 16
         ),
         new IntegerPropertyDefinition(
            PropertyKey.resultSetSizeThreshold,
            100,
            true,
            Messages.getString("ConnectionProperties.resultSetSizeThreshold"),
            "5.0.5",
            CATEGORY_DEBUGING_PROFILING,
            17
         ),
         new BooleanPropertyDefinition(
            PropertyKey.autoGenerateTestcaseScript,
            false,
            true,
            Messages.getString("ConnectionProperties.autoGenerateTestcaseScript"),
            "3.1.9",
            CATEGORY_DEBUGING_PROFILING,
            18
         ),
         new BooleanPropertyDefinition(
            PropertyKey.dumpQueriesOnException,
            false,
            true,
            Messages.getString("ConnectionProperties.dumpQueriesOnException"),
            "3.1.3",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.exceptionInterceptors,
            DEFAULT_VALUE_NULL_STRING,
            true,
            Messages.getString("ConnectionProperties.exceptionInterceptors"),
            "5.1.8",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.includeInnodbStatusInDeadlockExceptions,
            false,
            true,
            Messages.getString("ConnectionProperties.includeInnodbStatusInDeadlockExceptions"),
            "5.0.7",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.includeThreadDumpInDeadlockExceptions,
            false,
            true,
            Messages.getString("ConnectionProperties.includeThreadDumpInDeadlockExceptions"),
            "5.1.15",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.includeThreadNamesAsStatementComment,
            false,
            true,
            Messages.getString("ConnectionProperties.includeThreadNamesAsStatementComment"),
            "5.1.15",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.ignoreNonTxTables,
            false,
            true,
            Messages.getString("ConnectionProperties.ignoreNonTxTables"),
            "3.0.9",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useOnlyServerErrorMessages,
            true,
            true,
            Messages.getString("ConnectionProperties.useOnlyServerErrorMessages"),
            "3.0.15",
            CATEGORY_EXCEPTIONS,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.overrideSupportsIntegrityEnhancementFacility,
            false,
            true,
            Messages.getString("ConnectionProperties.overrideSupportsIEF"),
            "3.1.12",
            CATEGORY_INTEGRATION,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.ultraDevHack, false, true, Messages.getString("ConnectionProperties.ultraDevHack"), "2.0.3", CATEGORY_INTEGRATION, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.pedantic, false, true, Messages.getString("ConnectionProperties.pedantic"), "3.0.0", CATEGORY_JDBC, Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useColumnNamesInFindColumn,
            false,
            true,
            Messages.getString("ConnectionProperties.useColumnNamesInFindColumn"),
            "5.1.7",
            CATEGORY_JDBC,
            Integer.MAX_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.useOldAliasMetadataBehavior,
            false,
            true,
            Messages.getString("ConnectionProperties.useOldAliasMetadataBehavior"),
            "5.0.4",
            CATEGORY_JDBC,
            Integer.MIN_VALUE
         ),
         new EnumPropertyDefinition(
            PropertyKey.xdevapiSslMode,
            PropertyDefinitions.XdevapiSslMode.REQUIRED,
            true,
            Messages.getString("ConnectionProperties.xdevapiSslMode"),
            "8.0.7",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiTlsCiphersuites,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiTlsCiphersuites"),
            "8.0.19",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiTlsVersions,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiTlsVersions"),
            "8.0.19",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslKeyStoreUrl,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiSslKeyStoreUrl"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslKeyStorePassword,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiSslKeyStorePassword"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslKeyStoreType,
            "JKS",
            false,
            Messages.getString("ConnectionProperties.xdevapiSslKeyStoreType"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.xdevapiFallbackToSystemKeyStore,
            true,
            false,
            Messages.getString("ConnectionProperties.xdevapiFallbackToSystemKeyStore"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslTrustStoreUrl,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiSslTrustStoreUrl"),
            "6.0.6",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslTrustStorePassword,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiSslTrustStorePassword"),
            "6.0.6",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiSslTrustStoreType,
            "JKS",
            false,
            Messages.getString("ConnectionProperties.xdevapiSslTrustStoreType"),
            "6.0.6",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.xdevapiFallbackToSystemTrustStore,
            true,
            false,
            Messages.getString("ConnectionProperties.xdevapiFallbackToSystemTrustStore"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new EnumPropertyDefinition(
            PropertyKey.xdevapiAuth,
            PropertyDefinitions.AuthMech.PLAIN,
            false,
            Messages.getString("ConnectionProperties.auth"),
            "8.0.8",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new IntegerPropertyDefinition(
            PropertyKey.xdevapiConnectTimeout,
            10000,
            true,
            Messages.getString("ConnectionProperties.xdevapiConnectTimeout"),
            "8.0.13",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE,
            0,
            Integer.MAX_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiConnectionAttributes,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiConnectionAttributes"),
            "8.0.16",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new BooleanPropertyDefinition(
            PropertyKey.xdevapiDnsSrv, false, false, Messages.getString("ConnectionProperties.xdevapiDnsSrv"), "8.0.19", CATEGORY_XDEVAPI, Integer.MIN_VALUE
         ),
         new EnumPropertyDefinition(
            PropertyKey.xdevapiCompression,
            PropertyDefinitions.Compression.PREFERRED,
            false,
            Messages.getString("ConnectionProperties.xdevapiCompression"),
            "8.0.20",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiCompressionAlgorithms,
            "zstd_stream,lz4_message,deflate_stream",
            false,
            Messages.getString("ConnectionProperties.xdevapiCompressionAlgorithms"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         ),
         new StringPropertyDefinition(
            PropertyKey.xdevapiCompressionExtensions,
            DEFAULT_VALUE_NULL_STRING,
            false,
            Messages.getString("ConnectionProperties.xdevapiCompressionExtensions"),
            "8.0.22",
            CATEGORY_XDEVAPI,
            Integer.MIN_VALUE
         )
      };
      HashMap<PropertyKey, PropertyDefinition<?>> propertyKeyToPropertyDefinitionMap = new HashMap();

      for(PropertyDefinition<?> pdef : pdefs) {
         propertyKeyToPropertyDefinitionMap.put(pdef.getPropertyKey(), pdef);
      }

      PROPERTY_KEY_TO_PROPERTY_DEFINITION = Collections.unmodifiableMap(propertyKeyToPropertyDefinitionMap);
   }

   public static enum AuthMech {
      PLAIN,
      MYSQL41,
      SHA256_MEMORY,
      EXTERNAL;
   }

   public static enum Compression {
      PREFERRED,
      REQUIRED,
      DISABLED;
   }

   public static enum DatabaseTerm {
      CATALOG,
      SCHEMA;
   }

   public static enum SslMode {
      PREFERRED,
      REQUIRED,
      VERIFY_CA,
      VERIFY_IDENTITY,
      DISABLED;
   }

   public static enum XdevapiSslMode {
      REQUIRED,
      VERIFY_CA,
      VERIFY_IDENTITY,
      DISABLED;
   }

   public static enum ZeroDatetimeBehavior {
      CONVERT_TO_NULL,
      EXCEPTION,
      ROUND;
   }
}
