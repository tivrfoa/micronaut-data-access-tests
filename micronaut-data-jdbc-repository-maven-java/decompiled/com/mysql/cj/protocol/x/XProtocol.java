package com.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessageV3;
import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.QueryResult;
import com.mysql.cj.Session;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.log.LogFactory;
import com.mysql.cj.protocol.AbstractProtocol;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ExportControlled;
import com.mysql.cj.protocol.FullReadInputStream;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.MessageSender;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ResultBuilder;
import com.mysql.cj.protocol.ResultStreamer;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerCapabilities;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.protocol.ValueEncoder;
import com.mysql.cj.protocol.a.NativeSocketConnection;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.LongValueFactory;
import com.mysql.cj.util.SequentialIdLease;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxConnection;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import com.mysql.cj.x.protobuf.MysqlxNotice;
import com.mysql.cj.x.protobuf.MysqlxResultset;
import com.mysql.cj.x.protobuf.MysqlxSession;
import com.mysql.cj.x.protobuf.MysqlxSql;
import com.mysql.cj.xdevapi.PreparableStatement;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XProtocol extends AbstractProtocol<XMessage> implements Protocol<XMessage> {
   private static int RETRY_PREPARE_STATEMENT_COUNTDOWN = 100;
   private MessageReader<XMessageHeader, XMessage> reader;
   private MessageSender<XMessage> sender;
   private Closeable managedResource;
   private ResultStreamer currentResultStreamer;
   XServerSession serverSession = null;
   Boolean useSessionResetKeepOpen = null;
   public String defaultSchemaName;
   private Map<String, Object> clientCapabilities = new HashMap();
   private boolean supportsPreparedStatements = true;
   private int retryPrepareStatementCountdown = 0;
   private SequentialIdLease preparedStatementIds = new SequentialIdLease();
   private ReferenceQueue<PreparableStatement<?>> preparableStatementRefQueue = new ReferenceQueue();
   private Map<Integer, PreparableStatement.PreparableStatementFinalizer> preparableStatementFinalizerReferences = new TreeMap();
   private boolean compressionEnabled = false;
   private CompressionAlgorithm compressionAlgorithm;
   private Map<Class<? extends GeneratedMessageV3>, ProtocolEntityFactory<? extends ProtocolEntity, XMessage>> messageToProtocolEntityFactory = new HashMap();
   private String currUser = null;
   private String currPassword = null;
   private String currDatabase = null;

   public XProtocol(HostInfo hostInfo, PropertySet propertySet) {
      String host = hostInfo.getHost();
      if (host == null || StringUtils.isEmptyOrWhitespaceOnly(host)) {
         host = "localhost";
      }

      int port = hostInfo.getPort();
      if (port < 0) {
         port = 33060;
      }

      this.defaultSchemaName = hostInfo.getDatabase();
      RuntimeProperty<Integer> connectTimeout = propertySet.getIntegerProperty(PropertyKey.connectTimeout);
      RuntimeProperty<Integer> xdevapiConnectTimeout = propertySet.getIntegerProperty(PropertyKey.xdevapiConnectTimeout);
      if (xdevapiConnectTimeout.isExplicitlySet() || !connectTimeout.isExplicitlySet()) {
         connectTimeout.setValue(xdevapiConnectTimeout.getValue());
      }

      SocketConnection socketConn = new NativeSocketConnection();
      socketConn.connect(host, port, propertySet, null, null, 0);
      this.init(null, socketConn, propertySet, null);
   }

   @Override
   public void init(Session sess, SocketConnection socketConn, PropertySet propSet, TransactionEventHandler trManager) {
      super.init(sess, socketConn, propSet, trManager);
      this.log = LogFactory.getLogger(this.getPropertySet().getStringProperty(PropertyKey.logger).getStringValue(), "MySQL");
      this.messageBuilder = new XMessageBuilder();
      this.authProvider = new XAuthenticationProvider();
      this.authProvider.init(this, propSet, null);
      this.useSessionResetKeepOpen = null;
      this.messageToProtocolEntityFactory.put(MysqlxResultset.ColumnMetaData.class, new FieldFactory("latin1"));
      this.messageToProtocolEntityFactory.put(MysqlxNotice.Frame.class, new NoticeFactory());
      this.messageToProtocolEntityFactory.put(MysqlxResultset.Row.class, new XProtocolRowFactory());
      this.messageToProtocolEntityFactory.put(MysqlxResultset.FetchDoneMoreResultsets.class, new FetchDoneMoreResultsFactory());
      this.messageToProtocolEntityFactory.put(MysqlxResultset.FetchDone.class, new FetchDoneEntityFactory());
      this.messageToProtocolEntityFactory.put(MysqlxSql.StmtExecuteOk.class, new StatementExecuteOkFactory());
      this.messageToProtocolEntityFactory.put(Mysqlx.Ok.class, new OkFactory());
   }

   @Override
   public ServerSession getServerSession() {
      return this.serverSession;
   }

   public void sendCapabilities(Map<String, Object> keyValuePair) {
      keyValuePair.forEach((k, v) -> ((XServerCapabilities)this.getServerSession().getCapabilities()).setCapability(k, v));
      this.sender.send(((XMessageBuilder)this.messageBuilder).buildCapabilitiesSet(keyValuePair));
      this.readQueryResult(new OkBuilder());
   }

   @Override
   public void negotiateSSLConnection() {
      if (!ExportControlled.enabled()) {
         throw new CJConnectionFeatureNotAvailableException();
      } else if (!((XServerCapabilities)this.serverSession.getCapabilities()).hasCapability(XServerCapabilities.KEY_TLS)) {
         throw new CJCommunicationsException("A secure connection is required but the server is not configured with SSL.");
      } else {
         this.reader.stopAfterNextMessage();
         Map<String, Object> tlsCapabilities = new HashMap();
         tlsCapabilities.put(XServerCapabilities.KEY_TLS, true);
         this.sendCapabilities(tlsCapabilities);

         try {
            this.socketConnection.performTlsHandshake(null, this.log);
         } catch (FeatureNotAvailableException | IOException | SSLParamsException var4) {
            throw new CJCommunicationsException(var4);
         }

         try {
            this.sender = new SyncMessageSender(this.socketConnection.getMysqlOutput());
            this.reader = new SyncMessageReader(this.socketConnection.getMysqlInput(), this);
         } catch (IOException var3) {
            throw new XProtocolError(var3.getMessage(), var3);
         }
      }
   }

   public void negotiateCompression() {
      PropertyDefinitions.Compression compression = (PropertyDefinitions.Compression)this.propertySet
         .getEnumProperty(PropertyKey.xdevapiCompression.getKeyName())
         .getValue();
      if (compression != PropertyDefinitions.Compression.DISABLED) {
         Map<String, List<String>> compressionCapabilities = this.serverSession.serverCapabilities.getCompression();
         if (!compressionCapabilities.isEmpty()
            && compressionCapabilities.containsKey(XServerCapabilities.SUBKEY_COMPRESSION_ALGORITHM)
            && !((List)compressionCapabilities.get(XServerCapabilities.SUBKEY_COMPRESSION_ALGORITHM)).isEmpty()) {
            RuntimeProperty<String> compressionAlgorithmsProp = this.propertySet.getStringProperty(PropertyKey.xdevapiCompressionAlgorithms.getKeyName());
            String compressionAlgorithmsList = (String)compressionAlgorithmsProp.getValue();
            compressionAlgorithmsList = compressionAlgorithmsList == null ? "" : compressionAlgorithmsList.trim();
            String[] compressionAlgsOrder = compressionAlgorithmsList.split("\\s*,\\s*");
            String[] compressionAlgorithmsOrder = (String[])((Stream)Arrays.stream(compressionAlgsOrder).sequential())
               .filter(n -> n != null && n.length() > 0)
               .map(String::toLowerCase)
               .map(CompressionAlgorithm::getNormalizedAlgorithmName)
               .toArray(x$0 -> new String[x$0]);
            String compressionExtensions = (String)this.propertySet.getStringProperty(PropertyKey.xdevapiCompressionExtensions.getKeyName()).getValue();
            compressionExtensions = compressionExtensions == null ? "" : compressionExtensions.trim();
            Map<String, CompressionAlgorithm> compressionAlgorithms = this.getCompressionExtensions(compressionExtensions);
            Optional<String> algorithmOpt = ((Stream)Arrays.stream(compressionAlgorithmsOrder).sequential())
               .filter(((List)compressionCapabilities.get(XServerCapabilities.SUBKEY_COMPRESSION_ALGORITHM))::contains)
               .filter(compressionAlgorithms::containsKey)
               .findFirst();
            if (algorithmOpt.isPresent()) {
               String algorithm = (String)algorithmOpt.get();
               this.compressionAlgorithm = (CompressionAlgorithm)compressionAlgorithms.get(algorithm);
               this.compressionAlgorithm.getInputStreamClass();
               this.compressionAlgorithm.getOutputStreamClass();
               Map<String, Object> compressionCap = new HashMap();
               compressionCap.put(XServerCapabilities.SUBKEY_COMPRESSION_ALGORITHM, algorithm);
               compressionCap.put(XServerCapabilities.SUBKEY_COMPRESSION_SERVER_COMBINE_MIXED_MESSAGES, true);
               this.sendCapabilities(Collections.singletonMap(XServerCapabilities.KEY_COMPRESSION, compressionCap));
               this.compressionEnabled = true;
            } else if (compression == PropertyDefinitions.Compression.REQUIRED) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("Protocol.Compression.2"));
            }
         } else if (compression == PropertyDefinitions.Compression.REQUIRED) {
            throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("Protocol.Compression.0"));
         }
      }
   }

   @Override
   public void beforeHandshake() {
      this.serverSession = new XServerSession();

      try {
         this.sender = new SyncMessageSender(this.socketConnection.getMysqlOutput());
         this.reader = new SyncMessageReader(this.socketConnection.getMysqlInput(), this);
         this.managedResource = this.socketConnection.getMysqlSocket();
      } catch (IOException var25) {
         throw new XProtocolError(var25.getMessage(), var25);
      }

      this.serverSession.setCapabilities(this.readServerCapabilities());
      String attributes = (String)this.propertySet.getStringProperty(PropertyKey.xdevapiConnectionAttributes).getValue();
      if (attributes == null || !attributes.equalsIgnoreCase("false")) {
         Map<String, String> attMap = this.getConnectionAttributesMap("true".equalsIgnoreCase(attributes) ? "" : attributes);
         this.clientCapabilities.put(XServerCapabilities.KEY_SESSION_CONNECT_ATTRS, attMap);
      }

      RuntimeProperty<PropertyDefinitions.XdevapiSslMode> xdevapiSslMode = this.propertySet.getEnumProperty(PropertyKey.xdevapiSslMode);
      RuntimeProperty<PropertyDefinitions.SslMode> jdbcSslMode = this.propertySet.getEnumProperty(PropertyKey.sslMode);
      if (xdevapiSslMode.isExplicitlySet() || !jdbcSslMode.isExplicitlySet()) {
         jdbcSslMode.setValue(PropertyDefinitions.SslMode.valueOf(((PropertyDefinitions.XdevapiSslMode)xdevapiSslMode.getValue()).toString()));
      }

      RuntimeProperty<String> xdevapiSslKeyStoreUrl = this.propertySet.getStringProperty(PropertyKey.xdevapiSslKeyStoreUrl);
      RuntimeProperty<String> jdbcClientCertKeyStoreUrl = this.propertySet.getStringProperty(PropertyKey.clientCertificateKeyStoreUrl);
      if (xdevapiSslKeyStoreUrl.isExplicitlySet() || !jdbcClientCertKeyStoreUrl.isExplicitlySet()) {
         jdbcClientCertKeyStoreUrl.setValue(xdevapiSslKeyStoreUrl.getValue());
      }

      RuntimeProperty<String> xdevapiSslKeyStoreType = this.propertySet.getStringProperty(PropertyKey.xdevapiSslKeyStoreType);
      RuntimeProperty<String> jdbcClientCertKeyStoreType = this.propertySet.getStringProperty(PropertyKey.clientCertificateKeyStoreType);
      if (xdevapiSslKeyStoreType.isExplicitlySet() || !jdbcClientCertKeyStoreType.isExplicitlySet()) {
         jdbcClientCertKeyStoreType.setValue(xdevapiSslKeyStoreType.getValue());
      }

      RuntimeProperty<String> xdevapiSslKeyStorePassword = this.propertySet.getStringProperty(PropertyKey.xdevapiSslKeyStorePassword);
      RuntimeProperty<String> jdbcClientCertKeyStorePassword = this.propertySet.getStringProperty(PropertyKey.clientCertificateKeyStorePassword);
      if (xdevapiSslKeyStorePassword.isExplicitlySet() || !jdbcClientCertKeyStorePassword.isExplicitlySet()) {
         jdbcClientCertKeyStorePassword.setValue(xdevapiSslKeyStorePassword.getValue());
      }

      RuntimeProperty<Boolean> xdevapiFallbackToSystemKeyStore = this.propertySet.getBooleanProperty(PropertyKey.xdevapiFallbackToSystemKeyStore);
      RuntimeProperty<Boolean> jdbcFallbackToSystemKeyStore = this.propertySet.getBooleanProperty(PropertyKey.fallbackToSystemKeyStore);
      if (xdevapiFallbackToSystemKeyStore.isExplicitlySet() || !jdbcFallbackToSystemKeyStore.isExplicitlySet()) {
         jdbcFallbackToSystemKeyStore.setValue(xdevapiFallbackToSystemKeyStore.getValue());
      }

      RuntimeProperty<String> xdevapiSslTrustStoreUrl = this.propertySet.getStringProperty(PropertyKey.xdevapiSslTrustStoreUrl);
      RuntimeProperty<String> jdbcTrustCertKeyStoreUrl = this.propertySet.getStringProperty(PropertyKey.trustCertificateKeyStoreUrl);
      if (xdevapiSslTrustStoreUrl.isExplicitlySet() || !jdbcTrustCertKeyStoreUrl.isExplicitlySet()) {
         jdbcTrustCertKeyStoreUrl.setValue(xdevapiSslTrustStoreUrl.getValue());
      }

      RuntimeProperty<String> xdevapiSslTrustStoreType = this.propertySet.getStringProperty(PropertyKey.xdevapiSslTrustStoreType);
      RuntimeProperty<String> jdbcTrustCertKeyStoreType = this.propertySet.getStringProperty(PropertyKey.trustCertificateKeyStoreType);
      if (xdevapiSslTrustStoreType.isExplicitlySet() || !jdbcTrustCertKeyStoreType.isExplicitlySet()) {
         jdbcTrustCertKeyStoreType.setValue(xdevapiSslTrustStoreType.getValue());
      }

      RuntimeProperty<String> xdevapiSslTrustStorePassword = this.propertySet.getStringProperty(PropertyKey.xdevapiSslTrustStorePassword);
      RuntimeProperty<String> jdbcTrustCertKeyStorePassword = this.propertySet.getStringProperty(PropertyKey.trustCertificateKeyStorePassword);
      if (xdevapiSslTrustStorePassword.isExplicitlySet() || !jdbcTrustCertKeyStorePassword.isExplicitlySet()) {
         jdbcTrustCertKeyStorePassword.setValue(xdevapiSslTrustStorePassword.getValue());
      }

      RuntimeProperty<Boolean> xdevapiFallbackToSystemTrustStore = this.propertySet.getBooleanProperty(PropertyKey.xdevapiFallbackToSystemTrustStore);
      RuntimeProperty<Boolean> jdbcFallbackToSystemTrustStore = this.propertySet.getBooleanProperty(PropertyKey.fallbackToSystemTrustStore);
      if (xdevapiFallbackToSystemTrustStore.isExplicitlySet() || !jdbcFallbackToSystemTrustStore.isExplicitlySet()) {
         jdbcFallbackToSystemTrustStore.setValue(xdevapiFallbackToSystemTrustStore.getValue());
      }

      if (jdbcSslMode.getValue() == PropertyDefinitions.SslMode.PREFERRED) {
         jdbcSslMode.setValue(PropertyDefinitions.SslMode.REQUIRED);
      }

      if (jdbcSslMode.getValue() != PropertyDefinitions.SslMode.DISABLED) {
         RuntimeProperty<String> xdevapiTlsVersions = this.propertySet.getStringProperty(PropertyKey.xdevapiTlsVersions);
         RuntimeProperty<String> jdbcEnabledTlsProtocols = this.propertySet.getStringProperty(PropertyKey.tlsVersions);
         if (xdevapiTlsVersions.isExplicitlySet()) {
            String[] tlsVersions = ((String)xdevapiTlsVersions.getValue()).split("\\s*,\\s*");
            List<String> tryProtocols = Arrays.asList(tlsVersions);
            ExportControlled.checkValidProtocols(tryProtocols);
            jdbcEnabledTlsProtocols.setValue(xdevapiTlsVersions.getValue());
         }

         RuntimeProperty<String> xdevapiTlsCiphersuites = this.propertySet.getStringProperty(PropertyKey.xdevapiTlsCiphersuites);
         RuntimeProperty<String> jdbcEnabledSslCipherSuites = this.propertySet.getStringProperty(PropertyKey.tlsCiphersuites);
         if (xdevapiTlsCiphersuites.isExplicitlySet()) {
            jdbcEnabledSslCipherSuites.setValue(xdevapiTlsCiphersuites.getValue());
         }
      }

      if (this.clientCapabilities.size() > 0) {
         try {
            this.sendCapabilities(this.clientCapabilities);
         } catch (XProtocolError var26) {
            if (var26.getErrorCode() != 5002 && !var26.getMessage().contains(XServerCapabilities.KEY_SESSION_CONNECT_ATTRS)) {
               throw var26;
            }

            this.clientCapabilities.remove(XServerCapabilities.KEY_SESSION_CONNECT_ATTRS);
         }
      }

      if (jdbcSslMode.getValue() != PropertyDefinitions.SslMode.DISABLED) {
         this.negotiateSSLConnection();
      }

      this.negotiateCompression();
   }

   private Map<String, String> getConnectionAttributesMap(String attStr) {
      Map<String, String> attMap = new HashMap();
      if (attStr != null) {
         if (attStr.startsWith("[") && attStr.endsWith("]")) {
            attStr = attStr.substring(1, attStr.length() - 1);
         }

         if (!StringUtils.isNullOrEmpty(attStr)) {
            String[] pairs = attStr.split(",");

            for(String pair : pairs) {
               String[] kv = pair.split("=");
               String key = kv[0].trim();
               String value = kv.length > 1 ? kv[1].trim() : "";
               if (key.startsWith("_")) {
                  throw (WrongArgumentException)ExceptionFactory.createException(
                     WrongArgumentException.class, Messages.getString("Protocol.WrongAttributeName")
                  );
               }

               if (attMap.put(key, value) != null) {
                  throw (WrongArgumentException)ExceptionFactory.createException(
                     WrongArgumentException.class, Messages.getString("Protocol.DuplicateAttribute", new Object[]{key})
                  );
               }
            }
         }
      }

      attMap.put("_platform", Constants.OS_ARCH);
      attMap.put("_os", Constants.OS_NAME + "-" + Constants.OS_VERSION);
      attMap.put("_client_name", "MySQL Connector/J");
      attMap.put("_client_version", "8.0.29");
      attMap.put("_client_license", "GPL");
      attMap.put("_runtime_version", Constants.JVM_VERSION);
      attMap.put("_runtime_vendor", Constants.JVM_VENDOR);
      return attMap;
   }

   private Map<String, CompressionAlgorithm> getCompressionExtensions(String compressionExtensions) {
      Map<String, CompressionAlgorithm> compressionExtensionsMap = CompressionAlgorithm.getDefaultInstances();
      if (compressionExtensions.length() == 0) {
         return compressionExtensionsMap;
      } else {
         String[] compressionExtAlgs = compressionExtensions.split(",");

         for(String compressionExtAlg : compressionExtAlgs) {
            String[] compressionExtAlgParts = compressionExtAlg.split(":");
            if (compressionExtAlgParts.length != 3) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, Messages.getString("Protocol.Compression.1"));
            }

            String algorithmName = compressionExtAlgParts[0].toLowerCase();
            String inputStreamClassName = compressionExtAlgParts[1];
            String outputStreamClassName = compressionExtAlgParts[2];
            CompressionAlgorithm compressionAlg = new CompressionAlgorithm(algorithmName, inputStreamClassName, outputStreamClassName);
            compressionExtensionsMap.put(compressionAlg.getAlgorithmIdentifier(), compressionAlg);
         }

         return compressionExtensionsMap;
      }
   }

   @Override
   public void connect(String user, String password, String database) {
      this.currUser = user;
      this.currPassword = password;
      this.currDatabase = database;
      this.beforeHandshake();
      this.authProvider.connect(user, password, database);
   }

   @Override
   public void changeUser(String user, String password, String database) {
      this.currUser = user;
      this.currPassword = password;
      this.currDatabase = database;
      this.authProvider.changeUser(user, password, database);
   }

   @Override
   public void afterHandshake() {
      if (this.compressionEnabled) {
         try {
            this.reader = new SyncMessageReader(
               new FullReadInputStream(
                  new CompressionSplittedInputStream(this.socketConnection.getMysqlInput(), new CompressorStreamsFactory(this.compressionAlgorithm))
               ),
               this
            );
         } catch (IOException var3) {
            ExceptionFactory.createException(Messages.getString("Protocol.Compression.6"), var3);
         }

         try {
            this.sender = new SyncMessageSender(
               new CompressionSplittedOutputStream(this.socketConnection.getMysqlOutput(), new CompressorStreamsFactory(this.compressionAlgorithm))
            );
         } catch (IOException var2) {
            ExceptionFactory.createException(Messages.getString("Protocol.Compression.7"), var2);
         }
      }

      this.initServerSession();
   }

   @Override
   public void configureTimeZone() {
   }

   @Override
   public void initServerSession() {
      this.configureTimeZone();
      this.send(this.messageBuilder.buildSqlStatement("select @@mysqlx_max_allowed_packet"), 0);
      ColumnDefinition metadata = this.readMetadata();
      long count = new XProtocolRowInputStream(metadata, this, null).next().getValue(0, new LongValueFactory(this.propertySet));
      this.readQueryResult(new StatementExecuteOkBuilder());
      this.setMaxAllowedPacket((int)count);
   }

   public void readAuthenticateOk() {
      try {
         XMessage mess = this.reader.readMessage(null, 4);
         if (mess != null && mess.getNotices() != null) {
            for(Notice notice : mess.getNotices()) {
               if (notice instanceof Notice.XSessionStateChanged) {
                  switch(((Notice.XSessionStateChanged)notice).getParamType()) {
                     case 2:
                     default:
                        break;
                     case 11:
                        this.getServerSession().getCapabilities().setThreadId(((Notice.XSessionStateChanged)notice).getValue().getVUnsignedInt());
                  }
               }
            }
         }

      } catch (IOException var4) {
         throw new XProtocolError(var4.getMessage(), var4);
      }
   }

   public byte[] readAuthenticateContinue() {
      try {
         MysqlxSession.AuthenticateContinue msg = (MysqlxSession.AuthenticateContinue)this.reader.readMessage(null, 3).getMessage();
         byte[] data = msg.getAuthData().toByteArray();
         if (data.length != 20) {
            throw AssertionFailedException.shouldNotHappen("Salt length should be 20, but is " + data.length);
         } else {
            return data;
         }
      } catch (IOException var3) {
         throw new XProtocolError(var3.getMessage(), var3);
      }
   }

   public boolean hasMoreResults() {
      try {
         if (((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 16) {
            this.reader.readMessage(null, 16);
            return ((SyncMessageReader)this.reader).getNextNonNoticeMessageType() != 14;
         } else {
            return false;
         }
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   @Override
   public <T extends QueryResult> T readQueryResult(ResultBuilder<T> resultBuilder) {
      try {
         XMessage mess;
         Class<? extends GeneratedMessageV3> msgClass;
         for(boolean done = false;
            !done;
            done = resultBuilder.addProtocolEntity(
               (ProtocolEntity)((ProtocolEntityFactory)this.messageToProtocolEntityFactory.get(msgClass)).createFromMessage(mess)
            )
         ) {
            XMessageHeader header = this.reader.readHeader();
            mess = this.reader.readMessage(null, header);
            msgClass = mess.getMessage().getClass();
            if (Mysqlx.Error.class.equals(msgClass)) {
               throw new XProtocolError((Mysqlx.Error)Mysqlx.Error.class.cast(mess.getMessage()));
            }

            if (!this.messageToProtocolEntityFactory.containsKey(msgClass)) {
               throw new WrongArgumentException("Unhandled msg class (" + msgClass + ") + msg=" + mess.getMessage());
            }

            List<Notice> notices;
            if ((notices = mess.getNotices()) != null) {
               notices.stream().forEach(resultBuilder::addProtocolEntity);
            }
         }

         return resultBuilder.build();
      } catch (IOException var7) {
         throw new XProtocolError(var7.getMessage(), var7);
      }
   }

   public boolean hasResults() {
      try {
         return ((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 12;
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   public void drainRows() {
      try {
         while(((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 13) {
            this.reader.readMessage(null, 13);
         }

      } catch (XProtocolError var2) {
         this.currentResultStreamer = null;
         throw var2;
      } catch (IOException var3) {
         this.currentResultStreamer = null;
         throw new XProtocolError(var3.getMessage(), var3);
      }
   }

   @Override
   public ColumnDefinition readMetadata() {
      return this.readMetadata(null);
   }

   public ColumnDefinition readMetadata(Consumer<Notice> noticeConsumer) {
      try {
         List<MysqlxResultset.ColumnMetaData> fromServer = new LinkedList();

         do {
            XMessage mess = this.reader.readMessage(null, 12);
            List<Notice> notices;
            if (noticeConsumer != null && (notices = mess.getNotices()) != null) {
               notices.stream().forEach(noticeConsumer::accept);
            }

            fromServer.add((MysqlxResultset.ColumnMetaData)mess.getMessage());
         } while(((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 12);

         ArrayList<Field> metadata = new ArrayList(fromServer.size());
         ProtocolEntityFactory<Field, XMessage> fieldFactory = (ProtocolEntityFactory)this.messageToProtocolEntityFactory
            .get(MysqlxResultset.ColumnMetaData.class);
         fromServer.forEach(col -> metadata.add(fieldFactory.createFromMessage(new XMessage(col))));
         return new DefaultColumnDefinition((Field[])metadata.toArray(new Field[0]));
      } catch (IOException var6) {
         throw new XProtocolError(var6.getMessage(), var6);
      }
   }

   public ColumnDefinition readMetadata(Field f, Consumer<Notice> noticeConsumer) {
      try {
         List<MysqlxResultset.ColumnMetaData> fromServer;
         XMessage mess;
         for(fromServer = new LinkedList();
            ((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 12;
            fromServer.add((MysqlxResultset.ColumnMetaData)mess.getMessage())
         ) {
            mess = this.reader.readMessage(null, 12);
            List<Notice> notices;
            if (noticeConsumer != null && (notices = mess.getNotices()) != null) {
               notices.stream().forEach(noticeConsumer::accept);
            }
         }

         ArrayList<Field> metadata = new ArrayList(fromServer.size());
         metadata.add(f);
         ProtocolEntityFactory<Field, XMessage> fieldFactory = (ProtocolEntityFactory)this.messageToProtocolEntityFactory
            .get(MysqlxResultset.ColumnMetaData.class);
         fromServer.forEach(col -> metadata.add(fieldFactory.createFromMessage(new XMessage(col))));
         return new DefaultColumnDefinition((Field[])metadata.toArray(new Field[0]));
      } catch (IOException var7) {
         throw new XProtocolError(var7.getMessage(), var7);
      }
   }

   public XProtocolRow readRowOrNull(ColumnDefinition metadata, Consumer<Notice> noticeConsumer) {
      try {
         if (((SyncMessageReader)this.reader).getNextNonNoticeMessageType() == 13) {
            XMessage mess = this.reader.readMessage(null, 13);
            List<Notice> notices;
            if (noticeConsumer != null && (notices = mess.getNotices()) != null) {
               notices.stream().forEach(noticeConsumer::accept);
            }

            XProtocolRow res = new XProtocolRow((MysqlxResultset.Row)mess.getMessage());
            res.setMetadata(metadata);
            return res;
         } else {
            return null;
         }
      } catch (XProtocolError var6) {
         this.currentResultStreamer = null;
         throw var6;
      } catch (IOException var7) {
         this.currentResultStreamer = null;
         throw new XProtocolError(var7.getMessage(), var7);
      }
   }

   public boolean supportsPreparedStatements() {
      return this.supportsPreparedStatements;
   }

   public boolean readyForPreparingStatements() {
      if (this.retryPrepareStatementCountdown == 0) {
         return true;
      } else {
         --this.retryPrepareStatementCountdown;
         return false;
      }
   }

   public int getNewPreparedStatementId(PreparableStatement<?> preparableStatement) {
      if (!this.supportsPreparedStatements) {
         throw new XProtocolError("The connected MySQL server does not support prepared statements.");
      } else {
         int preparedStatementId = this.preparedStatementIds.allocateSequentialId();
         this.preparableStatementFinalizerReferences
            .put(
               preparedStatementId,
               new PreparableStatement.PreparableStatementFinalizer(preparableStatement, this.preparableStatementRefQueue, preparedStatementId)
            );
         return preparedStatementId;
      }
   }

   public void freePreparedStatementId(int preparedStatementId) {
      if (!this.supportsPreparedStatements) {
         throw new XProtocolError("The connected MySQL server does not support prepared statements.");
      } else {
         this.preparedStatementIds.releaseSequentialId(preparedStatementId);
         this.preparableStatementFinalizerReferences.remove(preparedStatementId);
      }
   }

   public boolean failedPreparingStatement(int preparedStatementId, XProtocolError e) {
      this.freePreparedStatementId(preparedStatementId);
      if (e.getErrorCode() == 1461) {
         this.retryPrepareStatementCountdown = RETRY_PREPARE_STATEMENT_COUNTDOWN;
         return true;
      } else if (e.getErrorCode() == 1047 && this.preparableStatementFinalizerReferences.isEmpty()) {
         this.supportsPreparedStatements = false;
         this.retryPrepareStatementCountdown = 0;
         this.preparedStatementIds = null;
         this.preparableStatementRefQueue = null;
         this.preparableStatementFinalizerReferences = null;
         return true;
      } else {
         return false;
      }
   }

   protected void newCommand() {
      if (this.currentResultStreamer != null) {
         try {
            this.currentResultStreamer.finishStreaming();
         } finally {
            this.currentResultStreamer = null;
         }
      }

      Reference<? extends PreparableStatement<?>> ref;
      if (this.supportsPreparedStatements) {
         while((ref = this.preparableStatementRefQueue.poll()) != null) {
            PreparableStatement.PreparableStatementFinalizer psf = (PreparableStatement.PreparableStatementFinalizer)ref;
            psf.clear();

            try {
               this.sender.send(((XMessageBuilder)this.messageBuilder).buildPrepareDeallocate(psf.getPreparedStatementId()));
               this.readQueryResult(new OkBuilder());
            } catch (XProtocolError var12) {
               if (var12.getErrorCode() != 5110) {
                  throw var12;
               }
            } finally {
               this.freePreparedStatementId(psf.getPreparedStatementId());
            }
         }
      }

   }

   public <M extends Message, R extends QueryResult> R query(M message, ResultBuilder<R> resultBuilder) {
      this.send(message, 0);
      R res = this.readQueryResult(resultBuilder);
      if (ResultStreamer.class.isAssignableFrom(res.getClass())) {
         this.currentResultStreamer = (ResultStreamer)res;
      }

      return res;
   }

   public <M extends Message, R extends QueryResult> CompletableFuture<R> queryAsync(M message, ResultBuilder<R> resultBuilder) {
      this.newCommand();
      CompletableFuture<R> f = new CompletableFuture();
      MessageListener<XMessage> l = new ResultMessageListener<>(this.messageToProtocolEntityFactory, resultBuilder, f);
      this.sender.send((XMessage)message, f, () -> this.reader.pushMessageListener(l));
      return f;
   }

   public boolean isOpen() {
      return this.managedResource != null;
   }

   @Override
   public void close() throws IOException {
      try {
         this.send(this.messageBuilder.buildClose(), 0);
         this.readQueryResult(new OkBuilder());
      } catch (Exception var10) {
      } finally {
         try {
            if (this.managedResource == null) {
               throw new ConnectionIsClosedException();
            }

            this.managedResource.close();
            this.managedResource = null;
         } catch (IOException var9) {
            throw new CJCommunicationsException(var9);
         }
      }

   }

   public boolean isSqlResultPending() {
      try {
         switch(((SyncMessageReader)this.reader).getNextNonNoticeMessageType()) {
            case 12:
               return true;
            case 16:
               this.reader.readMessage(null, 16);
            default:
               return false;
         }
      } catch (IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   public void setMaxAllowedPacket(int maxAllowedPacket) {
      this.sender.setMaxAllowedPacket(maxAllowedPacket);
   }

   @Override
   public void send(Message message, int packetLen) {
      this.newCommand();
      this.sender.send((XMessage)message);
   }

   @Override
   public ServerCapabilities readServerCapabilities() {
      try {
         this.sender.send(((XMessageBuilder)this.messageBuilder).buildCapabilitiesGet());
         return new XServerCapabilities(
            (Map<String, MysqlxDatatypes.Any>)((MysqlxConnection.Capabilities)this.reader.readMessage(null, 2).getMessage())
               .getCapabilitiesList()
               .stream()
               .collect(Collectors.toMap(MysqlxConnection.Capability::getName, MysqlxConnection.Capability::getValue))
         );
      } catch (AssertionFailedException | IOException var2) {
         throw new XProtocolError(var2.getMessage(), var2);
      }
   }

   @Override
   public void reset() {
      this.newCommand();
      this.propertySet.reset();
      if (this.useSessionResetKeepOpen == null) {
         try {
            this.send(((XMessageBuilder)this.messageBuilder).buildExpectOpen(), 0);
            this.readQueryResult(new OkBuilder());
            this.useSessionResetKeepOpen = true;
         } catch (XProtocolError var2) {
            if (var2.getErrorCode() != 5168 && var2.getErrorCode() != 5160) {
               throw var2;
            }

            this.useSessionResetKeepOpen = false;
         }
      }

      if (this.useSessionResetKeepOpen) {
         this.send(((XMessageBuilder)this.messageBuilder).buildSessionResetKeepOpen(), 0);
         this.readQueryResult(new OkBuilder());
      } else {
         this.send(((XMessageBuilder)this.messageBuilder).buildSessionResetAndClose(), 0);
         this.readQueryResult(new OkBuilder());
         if (this.clientCapabilities.containsKey(XServerCapabilities.KEY_SESSION_CONNECT_ATTRS)) {
            Map<String, Object> reducedClientCapabilities = new HashMap();
            reducedClientCapabilities.put(
               XServerCapabilities.KEY_SESSION_CONNECT_ATTRS, this.clientCapabilities.get(XServerCapabilities.KEY_SESSION_CONNECT_ATTRS)
            );
            if (reducedClientCapabilities.size() > 0) {
               this.sendCapabilities(reducedClientCapabilities);
            }
         }

         this.authProvider.changeUser(this.currUser, this.currPassword, this.currDatabase);
      }

      if (this.supportsPreparedStatements) {
         this.retryPrepareStatementCountdown = 0;
         this.preparedStatementIds = new SequentialIdLease();
         this.preparableStatementRefQueue = new ReferenceQueue();
         this.preparableStatementFinalizerReferences = new TreeMap();
      }

   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void changeDatabase(String database) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean versionMeetsMinimum(int major, int minor, int subminor) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage readMessage(XMessage reuse) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage checkErrorMessage() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public XMessage sendCommand(Message queryPacket, boolean skipCheck, int timeoutMillis) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public <T extends ProtocolEntity> T read(Class<T> requiredClass, ProtocolEntityFactory<T, XMessage> protocolEntityFactory) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public <T extends ProtocolEntity> T read(
      Class<Resultset> requiredClass,
      int maxRows,
      boolean streamResults,
      XMessage resultPacket,
      boolean isBinaryEncoded,
      ColumnDefinition metadata,
      ProtocolEntityFactory<T, XMessage> protocolEntityFactory
   ) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getQueryComment() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setQueryComment(String comment) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public Supplier<ValueEncoder> getValueEncoderSupplier(Object obj) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }
}
