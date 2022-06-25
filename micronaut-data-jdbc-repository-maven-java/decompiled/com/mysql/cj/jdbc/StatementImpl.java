package com.mysql.cj.jdbc;

import com.mysql.cj.CancelQueryTask;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.NativeSession;
import com.mysql.cj.PingTarget;
import com.mysql.cj.Query;
import com.mysql.cj.QueryAttributesBindings;
import com.mysql.cj.QueryInfo;
import com.mysql.cj.QueryReturnType;
import com.mysql.cj.Session;
import com.mysql.cj.SimpleQuery;
import com.mysql.cj.TransactionEventHandler;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.CJTimeoutException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.exceptions.StatementIsClosedException;
import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.CachedResultSetMetaData;
import com.mysql.cj.jdbc.result.ResultSetFactory;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.result.ByteArrayRow;
import com.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.Row;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatementImpl implements JdbcStatement {
   protected static final String PING_MARKER = "/* ping */";
   public static final byte USES_VARIABLES_FALSE = 0;
   public static final byte USES_VARIABLES_TRUE = 1;
   public static final byte USES_VARIABLES_UNKNOWN = -1;
   protected NativeMessageBuilder commandBuilder = null;
   protected String charEncoding = null;
   protected volatile JdbcConnection connection = null;
   protected boolean doEscapeProcessing = true;
   protected boolean isClosed = false;
   protected long lastInsertId = -1L;
   protected int maxFieldSize = (Integer)PropertyDefinitions.getPropertyDefinition(PropertyKey.maxAllowedPacket).getDefaultValue();
   public int maxRows = -1;
   protected Set<ResultSetInternalMethods> openResults = new HashSet();
   protected boolean pedantic = false;
   protected boolean profileSQL = false;
   protected ResultSetInternalMethods results = null;
   protected ResultSetInternalMethods generatedKeysResults = null;
   protected int resultSetConcurrency = 0;
   protected long updateCount = -1L;
   protected boolean useUsageAdvisor = false;
   protected SQLWarning warningChain = null;
   protected boolean holdResultsOpenOverClose = false;
   protected ArrayList<Row> batchedGeneratedKeys = null;
   protected boolean retrieveGeneratedKeys = false;
   protected boolean continueBatchOnError = false;
   protected PingTarget pingTarget = null;
   protected ExceptionInterceptor exceptionInterceptor;
   protected boolean lastQueryIsOnDupKeyUpdate = false;
   private boolean isImplicitlyClosingResults = false;
   protected RuntimeProperty<Boolean> dontTrackOpenResources;
   protected RuntimeProperty<Boolean> dumpQueriesOnException;
   protected boolean logSlowQueries = false;
   protected RuntimeProperty<Boolean> rewriteBatchedStatements;
   protected RuntimeProperty<Integer> maxAllowedPacket;
   protected boolean dontCheckOnDuplicateKeyUpdateInSQL;
   protected ResultSetFactory resultSetFactory;
   protected Query query;
   protected NativeSession session = null;
   private Resultset.Type originalResultSetType = Resultset.Type.FORWARD_ONLY;
   private int originalFetchSize = 0;
   private boolean isPoolable = false;
   private boolean closeOnCompletion = false;

   public StatementImpl(JdbcConnection c, String db) throws SQLException {
      if (c != null && !c.isClosed()) {
         this.connection = c;
         this.session = (NativeSession)c.getSession();
         this.exceptionInterceptor = c.getExceptionInterceptor();
         this.commandBuilder = new NativeMessageBuilder(this.session.getServerSession().supportsQueryAttributes());

         try {
            this.initQuery();
         } catch (CJException var6) {
            throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
         }

         this.query.setCurrentDatabase(db);
         JdbcPropertySet pset = c.getPropertySet();
         this.dontTrackOpenResources = pset.getBooleanProperty(PropertyKey.dontTrackOpenResources);
         this.dumpQueriesOnException = pset.getBooleanProperty(PropertyKey.dumpQueriesOnException);
         this.continueBatchOnError = pset.getBooleanProperty(PropertyKey.continueBatchOnError).getValue();
         this.pedantic = pset.getBooleanProperty(PropertyKey.pedantic).getValue();
         this.rewriteBatchedStatements = pset.getBooleanProperty(PropertyKey.rewriteBatchedStatements);
         this.charEncoding = (String)pset.getStringProperty(PropertyKey.characterEncoding).getValue();
         this.profileSQL = pset.getBooleanProperty(PropertyKey.profileSQL).getValue();
         this.useUsageAdvisor = pset.getBooleanProperty(PropertyKey.useUsageAdvisor).getValue();
         this.logSlowQueries = pset.getBooleanProperty(PropertyKey.logSlowQueries).getValue();
         this.maxAllowedPacket = pset.getIntegerProperty(PropertyKey.maxAllowedPacket);
         this.dontCheckOnDuplicateKeyUpdateInSQL = pset.getBooleanProperty(PropertyKey.dontCheckOnDuplicateKeyUpdateInSQL).getValue();
         this.doEscapeProcessing = pset.getBooleanProperty(PropertyKey.enableEscapeProcessing).getValue();
         this.maxFieldSize = this.maxAllowedPacket.getValue();
         if (!this.dontTrackOpenResources.getValue()) {
            c.registerStatement(this);
         }

         int defaultFetchSize = pset.getIntegerProperty(PropertyKey.defaultFetchSize).getValue();
         if (defaultFetchSize != 0) {
            this.setFetchSize(defaultFetchSize);
         }

         int maxRowsConn = pset.getIntegerProperty(PropertyKey.maxRows).getValue();
         if (maxRowsConn != -1) {
            this.setMaxRows(maxRowsConn);
         }

         this.holdResultsOpenOverClose = pset.getBooleanProperty(PropertyKey.holdResultsOpenOverStatementClose).getValue();
         this.resultSetFactory = new ResultSetFactory(this.connection, this);
      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
      }
   }

   protected void initQuery() {
      this.query = new SimpleQuery(this.session);
   }

   public void addBatch(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (sql != null) {
               this.query.addBatch(sql);
            }

         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void addBatch(Object batch) {
      this.query.addBatch(batch);
   }

   @Override
   public List<Object> getBatchedArgs() {
      return this.query.getBatchedArgs();
   }

   public void cancel() throws SQLException {
      try {
         if (this.query.getStatementExecuting().get()) {
            if (!this.isClosed && this.connection != null) {
               NativeSession newSession = null;

               try {
                  HostInfo hostInfo = this.session.getHostInfo();
                  String database = hostInfo.getDatabase();
                  String user = hostInfo.getUser();
                  String password = hostInfo.getPassword();
                  newSession = new NativeSession(this.session.getHostInfo(), this.session.getPropertySet());
                  newSession.connect(hostInfo, user, password, database, 30000, new TransactionEventHandler() {
                     @Override
                     public void transactionCompleted() {
                     }

                     @Override
                     public void transactionBegun() {
                     }
                  });
                  newSession.getProtocol()
                     .sendCommand(
                        new NativeMessageBuilder(newSession.getServerSession().supportsQueryAttributes())
                           .buildComQuery(newSession.getSharedSendPacket(), "KILL QUERY " + this.session.getThreadId()),
                        false,
                        0
                     );
                  this.setCancelStatus(Query.CancelStatus.CANCELED_BY_USER);
               } catch (IOException var11) {
                  throw SQLExceptionsMapping.translateException(var11, this.exceptionInterceptor);
               } finally {
                  if (newSession != null) {
                     newSession.forceClose();
                  }

               }
            }

         }
      } catch (CJException var13) {
         throw SQLExceptionsMapping.translateException(var13, this.getExceptionInterceptor());
      }
   }

   protected JdbcConnection checkClosed() {
      JdbcConnection c = this.connection;
      if (c == null) {
         throw (StatementIsClosedException)ExceptionFactory.createException(
            StatementIsClosedException.class, Messages.getString("Statement.AlreadyClosed"), this.getExceptionInterceptor()
         );
      } else {
         return c;
      }
   }

   protected boolean isResultSetProducingQuery(String sql) {
      QueryReturnType queryReturnType = QueryInfo.getQueryReturnType(sql, this.session.getServerSession().isNoBackslashEscapesSet());
      return queryReturnType == QueryReturnType.PRODUCES_RESULT_SET || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET;
   }

   protected boolean isNonResultSetProducingQuery(String sql) {
      QueryReturnType queryReturnType = QueryInfo.getQueryReturnType(sql, this.session.getServerSession().isNoBackslashEscapesSet());
      return queryReturnType == QueryReturnType.DOES_NOT_PRODUCE_RESULT_SET || queryReturnType == QueryReturnType.MAY_PRODUCE_RESULT_SET;
   }

   protected void checkNullOrEmptyQuery(String sql) throws SQLException {
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", this.getExceptionInterceptor());
      } else if (sql.length() == 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", this.getExceptionInterceptor());
      }
   }

   public void clearBatch() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.clearBatchedArgs();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearBatchedArgs() {
      this.query.clearBatchedArgs();
   }

   public void clearWarnings() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.setClearWarningsCalled(true);
            this.warningChain = null;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void close() throws SQLException {
      try {
         this.realClose(true, true);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected void closeAllOpenResults() throws SQLException {
      JdbcConnection locallyScopedConn = this.connection;
      if (locallyScopedConn != null) {
         synchronized(locallyScopedConn.getConnectionMutex()) {
            if (this.openResults != null) {
               for(ResultSetInternalMethods element : this.openResults) {
                  try {
                     element.realClose(false);
                  } catch (SQLException var7) {
                     AssertionFailedException.shouldNotHappen(var7);
                  }
               }

               this.openResults.clear();
            }

         }
      }
   }

   protected void implicitlyCloseAllOpenResults() throws SQLException {
      this.isImplicitlyClosingResults = true;

      try {
         if (!this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue()) {
            if (this.results != null) {
               this.results.realClose(false);
            }

            if (this.generatedKeysResults != null) {
               this.generatedKeysResults.realClose(false);
            }

            this.closeAllOpenResults();
         }
      } finally {
         this.isImplicitlyClosingResults = false;
      }

   }

   @Override
   public void removeOpenResultSet(ResultSetInternalMethods rs) {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               if (this.openResults != null) {
                  this.openResults.remove(rs);
               }

               boolean hasMoreResults = rs.getNextResultset() != null;
               if (this.results == rs && !hasMoreResults) {
                  this.results = null;
               }

               if (this.generatedKeysResults == rs) {
                  this.generatedKeysResults = null;
               }

               if (!this.isImplicitlyClosingResults && !hasMoreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }
            }
         } catch (StatementIsClosedException var7) {
         }

      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Override
   public int getOpenResultSetCount() {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               return this.openResults != null ? this.openResults.size() : 0;
            }
         } catch (StatementIsClosedException var5) {
            return 0;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   private void checkAndPerformCloseOnCompletionAction() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.isCloseOnCompletion()
               && !this.dontTrackOpenResources.getValue()
               && this.getOpenResultSetCount() == 0
               && (this.results == null || !this.results.hasRows() || this.results.isClosed())
               && (this.generatedKeysResults == null || !this.generatedKeysResults.hasRows() || this.generatedKeysResults.isClosed())) {
               this.realClose(false, false);
            }
         }
      } catch (SQLException var4) {
      }

   }

   private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            PreparedStatement pStmt = this.connection.prepareStatement(sql, this.query.getResultType().getIntValue(), this.resultSetConcurrency);
            pStmt.setFetchSize(this.query.getResultFetchSize());
            if (this.getQueryTimeout() > 0) {
               pStmt.setQueryTimeout(this.getQueryTimeout());
            }

            if (this.maxRows > -1) {
               pStmt.setMaxRows(this.maxRows);
            }

            this.statementBegins();
            pStmt.execute();
            ResultSetInternalMethods rs = ((JdbcStatement)pStmt).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((JdbcPreparedStatement)pStmt);
            this.results = rs;
            return rs;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected boolean createStreamingResultSet() {
      return this.query.getResultType() == Resultset.Type.FORWARD_ONLY
         && this.resultSetConcurrency == 1007
         && this.query.getResultFetchSize() == Integer.MIN_VALUE;
   }

   @Override
   public void enableStreamingResults() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.originalResultSetType = this.query.getResultType();
            this.originalFetchSize = this.query.getResultFetchSize();
            this.setFetchSize(Integer.MIN_VALUE);
            this.setResultSetType(Resultset.Type.FORWARD_ONLY);
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public void disableStreamingResults() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.query.getResultFetchSize() == Integer.MIN_VALUE && this.query.getResultType() == Resultset.Type.FORWARD_ONLY) {
               this.setFetchSize(this.originalFetchSize);
               this.setResultSetType(this.originalResultSetType);
            }

         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected void setupStreamingTimeout(JdbcConnection con) throws SQLException {
      int netTimeoutForStreamingResults = this.session.getPropertySet().getIntegerProperty(PropertyKey.netTimeoutForStreamingResults).getValue();
      if (this.createStreamingResultSet() && netTimeoutForStreamingResults > 0) {
         this.executeSimpleNonQuery(con, "SET net_write_timeout=" + netTimeoutForStreamingResults);
      }

   }

   @Override
   public CancelQueryTask startQueryTimer(Query stmtToCancel, int timeout) {
      return this.query.startQueryTimer(stmtToCancel, timeout);
   }

   @Override
   public void stopQueryTimer(CancelQueryTask timeoutTask, boolean rethrowCancelReason, boolean checkCancelTimeout) {
      this.query.stopQueryTimer(timeoutTask, rethrowCancelReason, checkCancelTimeout);
   }

   public boolean execute(String sql) throws SQLException {
      try {
         return this.executeInternal(sql, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   private boolean executeInternal(String sql, boolean returnGeneratedKeys) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         synchronized(locallyScopedConn.getConnectionMutex()) {
            this.checkClosed();
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            this.implicitlyCloseAllOpenResults();
            if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
               this.doPingInstead();
               return true;
            } else {
               this.retrieveGeneratedKeys = returnGeneratedKeys;
               this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys
                  && QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet()) == 'I'
                  && this.containsOnDuplicateKeyInString(sql);
               if (!QueryInfo.isReadOnlySafeQuery(sql, this.session.getServerSession().isNoBackslashEscapesSet()) && locallyScopedConn.isReadOnly()) {
                  throw SQLError.createSQLException(
                     Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", this.getExceptionInterceptor()
                  );
               } else {
                  boolean var31;
                  try {
                     this.setupStreamingTimeout(locallyScopedConn);
                     if (this.doEscapeProcessing) {
                        Object escapedSqlResult = EscapeProcessor.escapeSQL(
                           sql,
                           this.session.getServerSession().getSessionTimeZone(),
                           this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                           this.session.getServerSession().isServerTruncatesFracSecs(),
                           this.getExceptionInterceptor()
                        );
                        sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                     }

                     CachedResultSetMetaData cachedMetaData = null;
                     ResultSetInternalMethods rs = null;
                     this.batchedGeneratedKeys = null;
                     if (this.useServerFetch()) {
                        rs = this.createResultSetUsingServerFetch(sql);
                     } else {
                        CancelQueryTask timeoutTask = null;
                        String oldDb = null;

                        try {
                           timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                           if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                              oldDb = locallyScopedConn.getDatabase();
                              locallyScopedConn.setDatabase(this.getCurrentDatabase());
                           }

                           if (locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                              cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                           }

                           locallyScopedConn.setSessionMaxRows(this.isResultSetProducingQuery(sql) ? this.maxRows : -1);
                           this.statementBegins();
                           rs = ((NativeSession)locallyScopedConn.getSession())
                              .execSQL(this, sql, this.maxRows, null, this.createStreamingResultSet(), this.getResultSetFactory(), cachedMetaData, false);
                           if (timeoutTask != null) {
                              this.stopQueryTimer(timeoutTask, true, true);
                              timeoutTask = null;
                           }
                        } catch (OperationCancelledException | CJTimeoutException var24) {
                           throw SQLExceptionsMapping.translateException(var24, this.exceptionInterceptor);
                        } finally {
                           this.stopQueryTimer(timeoutTask, false, false);
                           if (oldDb != null) {
                              locallyScopedConn.setDatabase(oldDb);
                           }

                        }
                     }

                     if (rs != null) {
                        this.lastInsertId = rs.getUpdateID();
                        this.results = rs;
                        rs.setFirstCharOfQuery(QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet()));
                        if (rs.hasRows()) {
                           if (cachedMetaData != null) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                           } else if (this.session.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                              locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                           }
                        }
                     }

                     var31 = rs != null && rs.hasRows();
                  } finally {
                     this.query.getStatementExecuting().set(false);
                  }

                  return var31;
               }
            }
         }
      } catch (CJException var28) {
         throw SQLExceptionsMapping.translateException(var28, this.getExceptionInterceptor());
      }
   }

   @Override
   public void statementBegins() {
      this.query.statementBegins();
   }

   @Override
   public void resetCancelledState() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.resetCancelledState();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
      try {
         return this.executeInternal(sql, returnGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyIndices != null && generatedKeyIndices.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
      try {
         return this.executeInternal(sql, generatedKeyNames != null && generatedKeyNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public int[] executeBatch() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeBatchInternal());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected long[] executeBatchInternal() throws SQLException {
      // $FF: Couldn't be decompiled
      // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: [{5}:381] Cannot replace {2}:379 with {15}:427 because it wasn't found in [{15}:380]
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.replaceStatement(Statement.java:524)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractIfBlockIntoLoop(LoopExtractHelper.java:304)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractFirstIf(LoopExtractHelper.java:196)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoop(LoopExtractHelper.java:87)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:56)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoopsRec(LoopExtractHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.LoopExtractHelper.extractLoops(LoopExtractHelper.java:22)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessorRunnable.codeToJava(MethodProcessorRunnable.java:228)
      //
      // Bytecode:
      // 000: aload 0
      // 001: invokevirtual com/mysql/cj/jdbc/StatementImpl.checkClosed ()Lcom/mysql/cj/jdbc/JdbcConnection;
      // 004: astore 1
      // 005: aload 1
      // 006: invokeinterface com/mysql/cj/jdbc/JdbcConnection.getConnectionMutex ()Ljava/lang/Object; 1
      // 00b: dup
      // 00c: astore 2
      // 00d: monitorenter
      // 00e: aload 1
      // 00f: invokeinterface com/mysql/cj/jdbc/JdbcConnection.isReadOnly ()Z 1
      // 014: ifeq 03b
      // 017: new java/lang/StringBuilder
      // 01a: dup
      // 01b: invokespecial java/lang/StringBuilder.<init> ()V
      // 01e: ldc "Statement.34"
      // 020: invokestatic com/mysql/cj/Messages.getString (Ljava/lang/String;)Ljava/lang/String;
      // 023: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 026: ldc "Statement.35"
      // 028: invokestatic com/mysql/cj/Messages.getString (Ljava/lang/String;)Ljava/lang/String;
      // 02b: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 02e: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 031: ldc "S1009"
      // 033: aload 0
      // 034: invokevirtual com/mysql/cj/jdbc/StatementImpl.getExceptionInterceptor ()Lcom/mysql/cj/exceptions/ExceptionInterceptor;
      // 037: invokestatic com/mysql/cj/jdbc/exceptions/SQLError.createSQLException (Ljava/lang/String;Ljava/lang/String;Lcom/mysql/cj/exceptions/ExceptionInterceptor;)Ljava/sql/SQLException;
      // 03a: athrow
      // 03b: aload 0
      // 03c: invokevirtual com/mysql/cj/jdbc/StatementImpl.implicitlyCloseAllOpenResults ()V
      // 03f: aload 0
      // 040: getfield com/mysql/cj/jdbc/StatementImpl.query Lcom/mysql/cj/Query;
      // 043: invokeinterface com/mysql/cj/Query.getBatchedArgs ()Ljava/util/List; 1
      // 048: astore 3
      // 049: aload 3
      // 04a: ifnull 056
      // 04d: aload 3
      // 04e: invokeinterface java/util/List.size ()I 1
      // 053: ifne 05c
      // 056: bipush 0
      // 057: newarray 11
      // 059: aload 2
      // 05a: monitorexit
      // 05b: areturn
      // 05c: aload 0
      // 05d: invokevirtual com/mysql/cj/jdbc/StatementImpl.getTimeoutInMillis ()I
      // 060: istore 4
      // 062: aload 0
      // 063: bipush 0
      // 064: invokevirtual com/mysql/cj/jdbc/StatementImpl.setTimeoutInMillis (I)V
      // 067: aconst_null
      // 068: astore 5
      // 06a: aload 0
      // 06b: invokevirtual com/mysql/cj/jdbc/StatementImpl.resetCancelledState ()V
      // 06e: aload 0
      // 06f: invokevirtual com/mysql/cj/jdbc/StatementImpl.statementBegins ()V
      // 072: aload 0
      // 073: bipush 1
      // 074: putfield com/mysql/cj/jdbc/StatementImpl.retrieveGeneratedKeys Z
      // 077: aconst_null
      // 078: astore 6
      // 07a: aload 3
      // 07b: ifnull 20f
      // 07e: aload 3
      // 07f: invokeinterface java/util/List.size ()I 1
      // 084: istore 7
      // 086: aload 0
      // 087: new java/util/ArrayList
      // 08a: dup
      // 08b: aload 3
      // 08c: invokeinterface java/util/List.size ()I 1
      // 091: invokespecial java/util/ArrayList.<init> (I)V
      // 094: putfield com/mysql/cj/jdbc/StatementImpl.batchedGeneratedKeys Ljava/util/ArrayList;
      // 097: aload 1
      // 098: invokeinterface com/mysql/cj/jdbc/JdbcConnection.getPropertySet ()Lcom/mysql/cj/jdbc/JdbcPropertySet; 1
      // 09d: getstatic com/mysql/cj/conf/PropertyKey.allowMultiQueries Lcom/mysql/cj/conf/PropertyKey;
      // 0a0: invokeinterface com/mysql/cj/jdbc/JdbcPropertySet.getBooleanProperty (Lcom/mysql/cj/conf/PropertyKey;)Lcom/mysql/cj/conf/RuntimeProperty; 2
      // 0a5: invokeinterface com/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 0aa: checkcast java/lang/Boolean
      // 0ad: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0b0: istore 8
      // 0b2: iload 8
      // 0b4: ifne 0d9
      // 0b7: aload 1
      // 0b8: invokeinterface com/mysql/cj/jdbc/JdbcConnection.getPropertySet ()Lcom/mysql/cj/jdbc/JdbcPropertySet; 1
      // 0bd: getstatic com/mysql/cj/conf/PropertyKey.rewriteBatchedStatements Lcom/mysql/cj/conf/PropertyKey;
      // 0c0: invokeinterface com/mysql/cj/jdbc/JdbcPropertySet.getBooleanProperty (Lcom/mysql/cj/conf/PropertyKey;)Lcom/mysql/cj/conf/RuntimeProperty; 2
      // 0c5: invokeinterface com/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 0ca: checkcast java/lang/Boolean
      // 0cd: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 0d0: ifeq 10d
      // 0d3: iload 7
      // 0d5: bipush 4
      // 0d6: if_icmple 10d
      // 0d9: aload 0
      // 0da: iload 8
      // 0dc: iload 7
      // 0de: iload 4
      // 0e0: invokespecial com/mysql/cj/jdbc/StatementImpl.executeBatchUsingMultiQueries (ZII)[J
      // 0e3: astore 9
      // 0e5: aload 0
      // 0e6: getfield com/mysql/cj/jdbc/StatementImpl.query Lcom/mysql/cj/Query;
      // 0e9: invokeinterface com/mysql/cj/Query.getStatementExecuting ()Ljava/util/concurrent/atomic/AtomicBoolean; 1
      // 0ee: bipush 0
      // 0ef: invokevirtual java/util/concurrent/atomic/AtomicBoolean.set (Z)V
      // 0f2: aload 0
      // 0f3: aload 5
      // 0f5: bipush 0
      // 0f6: bipush 0
      // 0f7: invokevirtual com/mysql/cj/jdbc/StatementImpl.stopQueryTimer (Lcom/mysql/cj/CancelQueryTask;ZZ)V
      // 0fa: aload 0
      // 0fb: invokevirtual com/mysql/cj/jdbc/StatementImpl.resetCancelledState ()V
      // 0fe: aload 0
      // 0ff: iload 4
      // 101: invokevirtual com/mysql/cj/jdbc/StatementImpl.setTimeoutInMillis (I)V
      // 104: aload 0
      // 105: invokevirtual com/mysql/cj/jdbc/StatementImpl.clearBatch ()V
      // 108: aload 2
      // 109: monitorexit
      // 10a: aload 9
      // 10c: areturn
      // 10d: aload 0
      // 10e: aload 0
      // 10f: iload 4
      // 111: invokevirtual com/mysql/cj/jdbc/StatementImpl.startQueryTimer (Lcom/mysql/cj/Query;I)Lcom/mysql/cj/CancelQueryTask;
      // 114: astore 5
      // 116: iload 7
      // 118: newarray 11
      // 11a: astore 6
      // 11c: bipush 0
      // 11d: istore 9
      // 11f: iload 9
      // 121: iload 7
      // 123: if_icmpge 134
      // 126: aload 6
      // 128: iload 9
      // 12a: ldc2_w -3
      // 12d: lastore
      // 12e: iinc 9 1
      // 131: goto 11f
      // 134: aconst_null
      // 135: astore 9
      // 137: bipush 0
      // 138: istore 10
      // 13a: bipush 0
      // 13b: istore 10
      // 13d: iload 10
      // 13f: iload 7
      // 141: if_icmpge 1fe
      // 144: aload 3
      // 145: iload 10
      // 147: invokeinterface java/util/List.get (I)Ljava/lang/Object; 2
      // 14c: checkcast java/lang/String
      // 14f: astore 11
      // 151: aload 6
      // 153: iload 10
      // 155: aload 0
      // 156: aload 11
      // 158: bipush 1
      // 159: bipush 1
      // 15a: invokevirtual com/mysql/cj/jdbc/StatementImpl.executeUpdateInternal (Ljava/lang/String;ZZ)J
      // 15d: lastore
      // 15e: aload 5
      // 160: ifnull 167
      // 163: aload 0
      // 164: invokevirtual com/mysql/cj/jdbc/StatementImpl.checkCancelTimeout ()V
      // 167: aload 0
      // 168: aload 0
      // 169: getfield com/mysql/cj/jdbc/StatementImpl.results Lcom/mysql/cj/jdbc/result/ResultSetInternalMethods;
      // 16c: invokeinterface com/mysql/cj/jdbc/result/ResultSetInternalMethods.getFirstCharOfQuery ()C 1
      // 171: bipush 73
      // 173: if_icmpne 183
      // 176: aload 0
      // 177: aload 11
      // 179: invokevirtual com/mysql/cj/jdbc/StatementImpl.containsOnDuplicateKeyInString (Ljava/lang/String;)Z
      // 17c: ifeq 183
      // 17f: bipush 1
      // 180: goto 184
      // 183: bipush 0
      // 184: invokevirtual com/mysql/cj/jdbc/StatementImpl.getBatchedGeneratedKeys (I)V
      // 187: goto 1f8
      // 18a: astore 11
      // 18c: aload 6
      // 18e: iload 10
      // 190: ldc2_w -3
      // 193: lastore
      // 194: aload 0
      // 195: getfield com/mysql/cj/jdbc/StatementImpl.continueBatchOnError Z
      // 198: ifeq 1bb
      // 19b: aload 11
      // 19d: instanceof com/mysql/cj/jdbc/exceptions/MySQLTimeoutException
      // 1a0: ifne 1bb
      // 1a3: aload 11
      // 1a5: instanceof com/mysql/cj/jdbc/exceptions/MySQLStatementCancelledException
      // 1a8: ifne 1bb
      // 1ab: aload 0
      // 1ac: aload 11
      // 1ae: invokevirtual com/mysql/cj/jdbc/StatementImpl.hasDeadlockOrTimeoutRolledBackTx (Ljava/sql/SQLException;)Z
      // 1b1: ifne 1bb
      // 1b4: aload 11
      // 1b6: astore 9
      // 1b8: goto 1f8
      // 1bb: iload 10
      // 1bd: newarray 11
      // 1bf: astore 12
      // 1c1: aload 0
      // 1c2: aload 11
      // 1c4: invokevirtual com/mysql/cj/jdbc/StatementImpl.hasDeadlockOrTimeoutRolledBackTx (Ljava/sql/SQLException;)Z
      // 1c7: ifeq 1e6
      // 1ca: bipush 0
      // 1cb: istore 13
      // 1cd: iload 13
      // 1cf: aload 12
      // 1d1: arraylength
      // 1d2: if_icmpge 1e3
      // 1d5: aload 12
      // 1d7: iload 13
      // 1d9: ldc2_w -3
      // 1dc: lastore
      // 1dd: iinc 13 1
      // 1e0: goto 1cd
      // 1e3: goto 1f1
      // 1e6: aload 6
      // 1e8: bipush 0
      // 1e9: aload 12
      // 1eb: bipush 0
      // 1ec: iload 10
      // 1ee: invokestatic java/lang/System.arraycopy (Ljava/lang/Object;ILjava/lang/Object;II)V
      // 1f1: aload 11
      // 1f3: astore 9
      // 1f5: goto 1fe
      // 1f8: iinc 10 1
      // 1fb: goto 13d
      // 1fe: aload 9
      // 200: ifnull 20f
      // 203: aload 9
      // 205: aload 6
      // 207: aload 0
      // 208: invokevirtual com/mysql/cj/jdbc/StatementImpl.getExceptionInterceptor ()Lcom/mysql/cj/exceptions/ExceptionInterceptor;
      // 20b: invokestatic com/mysql/cj/jdbc/exceptions/SQLError.createBatchUpdateException (Ljava/sql/SQLException;[JLcom/mysql/cj/exceptions/ExceptionInterceptor;)Ljava/sql/SQLException;
      // 20e: athrow
      // 20f: aload 5
      // 211: ifnull 21f
      // 214: aload 0
      // 215: aload 5
      // 217: bipush 1
      // 218: bipush 1
      // 219: invokevirtual com/mysql/cj/jdbc/StatementImpl.stopQueryTimer (Lcom/mysql/cj/CancelQueryTask;ZZ)V
      // 21c: aconst_null
      // 21d: astore 5
      // 21f: aload 6
      // 221: ifnull 229
      // 224: aload 6
      // 226: goto 22c
      // 229: bipush 0
      // 22a: newarray 11
      // 22c: astore 7
      // 22e: aload 0
      // 22f: getfield com/mysql/cj/jdbc/StatementImpl.query Lcom/mysql/cj/Query;
      // 232: invokeinterface com/mysql/cj/Query.getStatementExecuting ()Ljava/util/concurrent/atomic/AtomicBoolean; 1
      // 237: bipush 0
      // 238: invokevirtual java/util/concurrent/atomic/AtomicBoolean.set (Z)V
      // 23b: aload 0
      // 23c: aload 5
      // 23e: bipush 0
      // 23f: bipush 0
      // 240: invokevirtual com/mysql/cj/jdbc/StatementImpl.stopQueryTimer (Lcom/mysql/cj/CancelQueryTask;ZZ)V
      // 243: aload 0
      // 244: invokevirtual com/mysql/cj/jdbc/StatementImpl.resetCancelledState ()V
      // 247: aload 0
      // 248: iload 4
      // 24a: invokevirtual com/mysql/cj/jdbc/StatementImpl.setTimeoutInMillis (I)V
      // 24d: aload 0
      // 24e: invokevirtual com/mysql/cj/jdbc/StatementImpl.clearBatch ()V
      // 251: aload 2
      // 252: monitorexit
      // 253: aload 7
      // 255: areturn
      // 256: astore 14
      // 258: aload 0
      // 259: getfield com/mysql/cj/jdbc/StatementImpl.query Lcom/mysql/cj/Query;
      // 25c: invokeinterface com/mysql/cj/Query.getStatementExecuting ()Ljava/util/concurrent/atomic/AtomicBoolean; 1
      // 261: bipush 0
      // 262: invokevirtual java/util/concurrent/atomic/AtomicBoolean.set (Z)V
      // 265: aload 14
      // 267: athrow
      // 268: astore 15
      // 26a: aload 0
      // 26b: aload 5
      // 26d: bipush 0
      // 26e: bipush 0
      // 26f: invokevirtual com/mysql/cj/jdbc/StatementImpl.stopQueryTimer (Lcom/mysql/cj/CancelQueryTask;ZZ)V
      // 272: aload 0
      // 273: invokevirtual com/mysql/cj/jdbc/StatementImpl.resetCancelledState ()V
      // 276: aload 0
      // 277: iload 4
      // 279: invokevirtual com/mysql/cj/jdbc/StatementImpl.setTimeoutInMillis (I)V
      // 27c: aload 0
      // 27d: invokevirtual com/mysql/cj/jdbc/StatementImpl.clearBatch ()V
      // 280: aload 15
      // 282: athrow
      // 283: astore 16
      // 285: aload 2
      // 286: monitorexit
      // 287: aload 16
      // 289: athrow
   }

   protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex) {
      int vendorCode = ex.getErrorCode();
      switch(vendorCode) {
         case 1205:
            return false;
         case 1206:
         case 1213:
            return true;
         default:
            return false;
      }
   }

   private long[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout) throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.checkClosed();
         synchronized(locallyScopedConn.getConnectionMutex()) {
            if (!multiQueriesEnabled) {
               this.session.enableMultiQueries();
            }

            Statement batchStmt = null;
            CancelQueryTask timeoutTask = null;

            long[] var55;
            try {
               long[] updateCounts = new long[nbrCommands];

               for(int i = 0; i < nbrCommands; ++i) {
                  updateCounts[i] = -3L;
               }

               int commandIndex = 0;
               StringBuilder queryBuf = new StringBuilder();
               batchStmt = locallyScopedConn.createStatement();
               JdbcStatement jdbcBatchedStmt = (JdbcStatement)batchStmt;
               this.getQueryAttributesBindings().runThroughAll(a -> jdbcBatchedStmt.setAttribute(a.getName(), a.getValue()));
               timeoutTask = this.startQueryTimer((StatementImpl)batchStmt, individualStatementTimeout);
               int counter = 0;
               String connectionEncoding = (String)locallyScopedConn.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue();
               int numberOfBytesPerChar = StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")
                  ? 3
                  : (this.session.getServerSession().getCharsetSettings().isMultibyteCharset(connectionEncoding) ? 2 : 1);
               int escapeAdjust = 1;
               batchStmt.setEscapeProcessing(this.doEscapeProcessing);
               if (this.doEscapeProcessing) {
                  escapeAdjust = 2;
               }

               SQLException sqlEx = null;
               int argumentSetsInBatchSoFar = 0;

               for(commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                  String nextQuery = (String)this.query.getBatchedArgs().get(commandIndex);
                  if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.maxAllowedPacket.getValue()) {
                     try {
                        batchStmt.execute(queryBuf.toString(), 1);
                     } catch (SQLException var47) {
                        sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, var47);
                     }

                     counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                     queryBuf = new StringBuilder();
                     argumentSetsInBatchSoFar = 0;
                  }

                  queryBuf.append(nextQuery);
                  queryBuf.append(";");
                  ++argumentSetsInBatchSoFar;
               }

               if (queryBuf.length() > 0) {
                  try {
                     batchStmt.execute(queryBuf.toString(), 1);
                  } catch (SQLException var46) {
                     sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, var46);
                  }

                  counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
               }

               if (timeoutTask != null) {
                  this.stopQueryTimer(timeoutTask, true, true);
                  timeoutTask = null;
               }

               if (sqlEx != null) {
                  throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
               }

               var55 = updateCounts != null ? updateCounts : new long[0];
            } finally {
               this.stopQueryTimer(timeoutTask, false, false);
               this.resetCancelledState();

               try {
                  if (batchStmt != null) {
                     batchStmt.close();
                  }
               } finally {
                  if (!multiQueriesEnabled) {
                     this.session.disableMultiQueries();
                  }

               }

            }

            return var55;
         }
      } catch (CJException var51) {
         throw SQLExceptionsMapping.translateException(var51, this.getExceptionInterceptor());
      }
   }

   protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, long[] updateCounts) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
            boolean doGenKeys = this.batchedGeneratedKeys != null;
            byte[][] row = (byte[][])null;
            if (doGenKeys) {
               long generatedKey = batchedStatement.getLastInsertID();
               row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
               this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }

            while(batchedStatement.getMoreResults() || batchedStatement.getLargeUpdateCount() != -1L) {
               updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
               if (doGenKeys) {
                  long generatedKey = batchedStatement.getLastInsertID();
                  row = new byte[][]{StringUtils.getBytes(Long.toString(generatedKey))};
                  this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
               }
            }

            return updateCountCounter;
         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, long[] updateCounts, SQLException ex) throws BatchUpdateException, SQLException {
      for(int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; --j) {
         updateCounts[j] = -3L;
      }

      if (this.continueBatchOnError
         && !(ex instanceof MySQLTimeoutException)
         && !(ex instanceof MySQLStatementCancelledException)
         && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
         return ex;
      } else {
         long[] newUpdateCounts = new long[endOfBatchIndex];
         System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
         throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
      }
   }

   public ResultSet executeQuery(String sql) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            JdbcConnection locallyScopedConn = this.connection;
            this.retrieveGeneratedKeys = false;
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            this.implicitlyCloseAllOpenResults();
            if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
               this.doPingInstead();
               return this.results;
            } else {
               this.setupStreamingTimeout(locallyScopedConn);
               if (this.doEscapeProcessing) {
                  Object escapedSqlResult = EscapeProcessor.escapeSQL(
                     sql,
                     this.session.getServerSession().getSessionTimeZone(),
                     this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                     this.session.getServerSession().isServerTruncatesFracSecs(),
                     this.getExceptionInterceptor()
                  );
                  sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
               }

               if (!this.isResultSetProducingQuery(sql)) {
                  throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
               } else {
                  CachedResultSetMetaData cachedMetaData = null;
                  if (this.useServerFetch()) {
                     this.results = this.createResultSetUsingServerFetch(sql);
                     return this.results;
                  } else {
                     CancelQueryTask timeoutTask = null;
                     String oldDb = null;

                     try {
                        timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                        if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                           oldDb = locallyScopedConn.getDatabase();
                           locallyScopedConn.setDatabase(this.getCurrentDatabase());
                        }

                        if (locallyScopedConn.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                           cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                        }

                        locallyScopedConn.setSessionMaxRows(this.maxRows);
                        this.statementBegins();
                        this.results = ((NativeSession)locallyScopedConn.getSession())
                           .execSQL(this, sql, this.maxRows, null, this.createStreamingResultSet(), this.getResultSetFactory(), cachedMetaData, false);
                        if (timeoutTask != null) {
                           this.stopQueryTimer(timeoutTask, true, true);
                           timeoutTask = null;
                        }
                     } catch (OperationCancelledException | CJTimeoutException var15) {
                        throw SQLExceptionsMapping.translateException(var15, this.exceptionInterceptor);
                     } finally {
                        this.query.getStatementExecuting().set(false);
                        this.stopQueryTimer(timeoutTask, false, false);
                        if (oldDb != null) {
                           locallyScopedConn.setDatabase(oldDb);
                        }

                     }

                     this.lastInsertId = this.results.getUpdateID();
                     if (cachedMetaData != null) {
                        locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                     } else if (this.connection.getPropertySet().getBooleanProperty(PropertyKey.cacheResultSetMetadata).getValue()) {
                        locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                     }

                     return this.results;
                  }
               }
            }
         }
      } catch (CJException var18) {
         throw SQLExceptionsMapping.translateException(var18, this.getExceptionInterceptor());
      }
   }

   protected void doPingInstead() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.pingTarget != null) {
               try {
                  this.pingTarget.doPing();
               } catch (SQLException var5) {
                  throw var5;
               } catch (Exception var6) {
                  throw SQLError.createSQLException(var6.getMessage(), "08S01", var6, this.getExceptionInterceptor());
               }
            } else {
               this.connection.ping();
            }

            ResultSetInternalMethods fakeSelectOneResultSet = this.generatePingResultSet();
            this.results = fakeSelectOneResultSet;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            String encoding = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
            int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field(null, "1", collationIndex, encoding, MysqlType.BIGINT, 1)};
            ArrayList<Row> rows = new ArrayList();
            byte[] colVal = new byte[]{49};
            rows.add(new ByteArrayRow(new byte[][]{colVal}, this.getExceptionInterceptor()));
            return this.resultSetFactory.createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rows, new DefaultColumnDefinition(fields)));
         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   public void executeSimpleNonQuery(JdbcConnection c, String nonQuery) throws SQLException {
      try {
         synchronized(c.getConnectionMutex()) {
            ((NativeSession)c.getSession()).execSQL(this, nonQuery, -1, null, false, this.getResultSetFactory(), null, false).close();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   public int executeUpdate(String sql) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql));
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   protected long executeUpdateInternal(String sql, boolean isBatch, boolean returnGeneratedKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            JdbcConnection locallyScopedConn = this.connection;
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            char firstStatementChar = QueryInfo.firstCharOfStatementUc(sql, this.session.getServerSession().isNoBackslashEscapesSet());
            if (!this.isNonResultSetProducingQuery(sql)) {
               throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", this.getExceptionInterceptor());
            } else {
               this.retrieveGeneratedKeys = returnGeneratedKeys;
               this.lastQueryIsOnDupKeyUpdate = returnGeneratedKeys && firstStatementChar == 'I' && this.containsOnDuplicateKeyInString(sql);
               ResultSetInternalMethods rs = null;
               if (this.doEscapeProcessing) {
                  Object escapedSqlResult = EscapeProcessor.escapeSQL(
                     sql,
                     this.session.getServerSession().getSessionTimeZone(),
                     this.session.getServerSession().getCapabilities().serverSupportsFracSecs(),
                     this.session.getServerSession().isServerTruncatesFracSecs(),
                     this.getExceptionInterceptor()
                  );
                  sql = escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
               }

               if (locallyScopedConn.isReadOnly(false)) {
                  throw SQLError.createSQLException(
                     Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", this.getExceptionInterceptor()
                  );
               } else {
                  this.implicitlyCloseAllOpenResults();
                  CancelQueryTask timeoutTask = null;
                  String oldDb = null;

                  try {
                     timeoutTask = this.startQueryTimer(this, this.getTimeoutInMillis());
                     if (!locallyScopedConn.getDatabase().equals(this.getCurrentDatabase())) {
                        oldDb = locallyScopedConn.getDatabase();
                        locallyScopedConn.setDatabase(this.getCurrentDatabase());
                     }

                     locallyScopedConn.setSessionMaxRows(-1);
                     this.statementBegins();
                     rs = ((NativeSession)locallyScopedConn.getSession()).execSQL(this, sql, -1, null, false, this.getResultSetFactory(), null, isBatch);
                     if (timeoutTask != null) {
                        this.stopQueryTimer(timeoutTask, true, true);
                        timeoutTask = null;
                     }
                  } catch (OperationCancelledException | CJTimeoutException var18) {
                     throw SQLExceptionsMapping.translateException(var18, this.exceptionInterceptor);
                  } finally {
                     this.stopQueryTimer(timeoutTask, false, false);
                     if (oldDb != null) {
                        locallyScopedConn.setDatabase(oldDb);
                     }

                     if (!isBatch) {
                        this.query.getStatementExecuting().set(false);
                     }

                  }

                  this.results = rs;
                  rs.setFirstCharOfQuery(firstStatementChar);
                  this.updateCount = rs.getUpdateCount();
                  this.lastInsertId = rs.getUpdateID();
                  return this.updateCount;
               }
            }
         }
      } catch (CJException var21) {
         throw SQLExceptionsMapping.translateException(var21, this.getExceptionInterceptor());
      }
   }

   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, autoGeneratedKeys));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnIndexes));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnNames));
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public Connection getConnection() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.connection;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getFetchDirection() throws SQLException {
      try {
         return 1000;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public int getFetchSize() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.query.getResultFetchSize();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public ResultSet getGeneratedKeys() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (!this.retrieveGeneratedKeys) {
               throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", this.getExceptionInterceptor());
            } else if (this.batchedGeneratedKeys == null) {
               return this.lastQueryIsOnDupKeyUpdate
                  ? (this.generatedKeysResults = this.getGeneratedKeysInternal(1L))
                  : (this.generatedKeysResults = this.getGeneratedKeysInternal());
            } else {
               String encoding = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
               int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
               Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encoding, MysqlType.BIGINT_UNSIGNED, 20)};
               this.generatedKeysResults = this.resultSetFactory
                  .createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(this.batchedGeneratedKeys, new DefaultColumnDefinition(fields)));
               return this.generatedKeysResults;
            }
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
      long numKeys = this.getLargeUpdateCount();
      return this.getGeneratedKeysInternal(numKeys);
   }

   protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            String encoding = this.session.getServerSession().getCharsetSettings().getMetadataEncoding();
            int collationIndex = this.session.getServerSession().getCharsetSettings().getMetadataCollationIndex();
            Field[] fields = new Field[]{new Field("", "GENERATED_KEY", collationIndex, encoding, MysqlType.BIGINT_UNSIGNED, 20)};
            ArrayList<Row> rowSet = new ArrayList();
            long beginAt = this.getLastInsertID();
            if (this.results != null) {
               String serverInfo = this.results.getServerInfo();
               if (numKeys > 0L && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
                  numKeys = this.getRecordCountFromInfo(serverInfo);
               }

               if (beginAt != 0L && numKeys > 0L) {
                  for(int i = 0; (long)i < numKeys; ++i) {
                     byte[][] row = new byte[1][];
                     if (beginAt > 0L) {
                        row[0] = StringUtils.getBytes(Long.toString(beginAt));
                     } else {
                        byte[] asBytes = new byte[]{
                           (byte)((int)(beginAt >>> 56)),
                           (byte)((int)(beginAt >>> 48)),
                           (byte)((int)(beginAt >>> 40)),
                           (byte)((int)(beginAt >>> 32)),
                           (byte)((int)(beginAt >>> 24)),
                           (byte)((int)(beginAt >>> 16)),
                           (byte)((int)(beginAt >>> 8)),
                           (byte)((int)(beginAt & 255L))
                        };
                        BigInteger val = new BigInteger(1, asBytes);
                        row[0] = val.toString().getBytes();
                     }

                     rowSet.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                     beginAt += (long)this.connection.getAutoIncrementIncrement();
                  }
               }
            }

            return this.resultSetFactory.createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(rowSet, new DefaultColumnDefinition(fields)));
         }
      } catch (CJException var18) {
         throw SQLExceptionsMapping.translateException(var18, this.getExceptionInterceptor());
      }
   }

   public long getLastInsertID() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.lastInsertId;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public long getLongUpdateCount() {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return -1L;
            } else {
               return this.results.hasRows() ? -1L : this.updateCount;
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getMaxFieldSize() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.maxFieldSize;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getMaxRows() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.maxRows <= 0 ? 0 : this.maxRows;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public boolean getMoreResults() throws SQLException {
      try {
         return this.getMoreResults(1);
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public boolean getMoreResults(int current) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return false;
            } else {
               boolean streamingMode = this.createStreamingResultSet();
               if (streamingMode && this.results.hasRows()) {
                  while(this.results.next()) {
                  }
               }

               ResultSetInternalMethods nextResultSet = (ResultSetInternalMethods)this.results.getNextResultset();
               switch(current) {
                  case 1:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.realClose(false);
                        }

                        this.results.clearNextResultset();
                     }
                     break;
                  case 2:
                     if (!this.dontTrackOpenResources.getValue()) {
                        this.openResults.add(this.results);
                     }

                     this.results.clearNextResultset();
                     break;
                  case 3:
                     if (this.results != null) {
                        if (!streamingMode && !this.dontTrackOpenResources.getValue()) {
                           this.results.realClose(false);
                        }

                        this.results.clearNextResultset();
                     }

                     this.closeAllOpenResults();
                     break;
                  default:
                     throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", this.getExceptionInterceptor());
               }

               this.results = nextResultSet;
               if (this.results == null) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else if (this.results.hasRows()) {
                  this.updateCount = -1L;
                  this.lastInsertId = -1L;
               } else {
                  this.updateCount = this.results.getUpdateCount();
                  this.lastInsertId = this.results.getUpdateID();
               }

               boolean moreResults = this.results != null && this.results.hasRows();
               if (!moreResults) {
                  this.checkAndPerformCloseOnCompletionAction();
               }

               return moreResults;
            }
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public int getQueryTimeout() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.getTimeoutInMillis() / 1000;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   private long getRecordCountFromInfo(String serverInfo) {
      StringBuilder recordsBuf = new StringBuilder();
      long recordsCount = 0L;
      long duplicatesCount = 0L;
      char c = '\u0000';
      int length = serverInfo.length();

      int i;
      for(i = 0; i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      recordsBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         recordsBuf.append(c);
         ++i;
      }

      recordsCount = Long.parseLong(recordsBuf.toString());

      StringBuilder duplicatesBuf;
      for(duplicatesBuf = new StringBuilder(); i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      duplicatesBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         duplicatesBuf.append(c);
         ++i;
      }

      duplicatesCount = Long.parseLong(duplicatesBuf.toString());
      return recordsCount - duplicatesCount;
   }

   public ResultSet getResultSet() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.results != null && this.results.hasRows() ? this.results : null;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getResultSetConcurrency() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.resultSetConcurrency;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getResultSetHoldability() throws SQLException {
      try {
         return 1;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSetInternalMethods getResultSetInternal() {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               return this.results;
            }
         } catch (StatementIsClosedException var5) {
            return this.results;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public int getResultSetType() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.query.getResultType().getIntValue();
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public int getUpdateCount() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.getLargeUpdateCount());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public SQLWarning getWarnings() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.isClearWarningsCalled()) {
               return null;
            } else {
               SQLWarning pendingWarningsFromServer = this.session.getProtocol().convertShowWarningsToSQLWarnings(false);
               if (this.warningChain != null) {
                  this.warningChain.setNextWarning(pendingWarningsFromServer);
               } else {
                  this.warningChain = pendingWarningsFromServer;
               }

               return this.warningChain;
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
      JdbcConnection locallyScopedConn = this.connection;
      if (locallyScopedConn != null && !this.isClosed) {
         if (!this.dontTrackOpenResources.getValue()) {
            locallyScopedConn.unregisterStatement(this);
         }

         if (this.useUsageAdvisor && !calledExplicitly) {
            this.session.getProfilerEventHandler().processEvent((byte)0, this.session, this, null, 0L, new Throwable(), Messages.getString("Statement.63"));
         }

         if (closeOpenResults) {
            closeOpenResults = !this.holdResultsOpenOverClose && !this.dontTrackOpenResources.getValue();
         }

         if (closeOpenResults) {
            if (this.results != null) {
               try {
                  this.results.close();
               } catch (Exception var6) {
               }
            }

            if (this.generatedKeysResults != null) {
               try {
                  this.generatedKeysResults.close();
               } catch (Exception var5) {
               }
            }

            this.closeAllOpenResults();
         }

         this.clearAttributes();
         this.isClosed = true;
         this.closeQuery();
         this.results = null;
         this.generatedKeysResults = null;
         this.connection = null;
         this.session = null;
         this.warningChain = null;
         this.openResults = null;
         this.batchedGeneratedKeys = null;
         this.pingTarget = null;
         this.resultSetFactory = null;
      }
   }

   public void setCursorName(String name) throws SQLException {
      try {
         ;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public void setEscapeProcessing(boolean enable) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.doEscapeProcessing = enable;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setFetchDirection(int direction) throws SQLException {
      try {
         switch(direction) {
            case 1000:
            case 1001:
            case 1002:
               return;
            default:
               throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", this.getExceptionInterceptor());
         }
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public void setFetchSize(int rows) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if ((rows >= 0 || rows == Integer.MIN_VALUE) && (this.maxRows <= 0 || rows <= this.getMaxRows())) {
               this.query.setResultFetchSize(rows);
            } else {
               throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", this.getExceptionInterceptor());
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            }
         } catch (StatementIsClosedException var6) {
         }

      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   public void setMaxFieldSize(int max) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (max < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", this.getExceptionInterceptor());
            } else {
               int maxBuf = this.maxAllowedPacket.getValue();
               if (max > maxBuf) {
                  throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{(long)maxBuf}), "S1009", this.getExceptionInterceptor());
               } else {
                  this.maxFieldSize = max;
               }
            }
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   public void setMaxRows(int max) throws SQLException {
      try {
         this.setLargeMaxRows((long)max);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public void setQueryTimeout(int seconds) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (seconds < 0) {
               throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", this.getExceptionInterceptor());
            } else {
               this.setTimeoutInMillis(seconds * 1000);
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   void setResultSetConcurrency(int concurrencyFlag) throws SQLException {
      try {
         try {
            synchronized(this.checkClosed().getConnectionMutex()) {
               this.resultSetConcurrency = concurrencyFlag;
               this.resultSetFactory = new ResultSetFactory(this.connection, this);
            }
         } catch (StatementIsClosedException var6) {
         }

      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   void setResultSetType(Resultset.Type typeFlag) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.query.setResultType(typeFlag);
            this.resultSetFactory = new ResultSetFactory(this.connection, this);
         }
      } catch (StatementIsClosedException var5) {
      }

   }

   void setResultSetType(int typeFlag) throws SQLException {
      this.query.setResultType(Resultset.Type.fromValue(typeFlag, Resultset.Type.FORWARD_ONLY));
   }

   protected void getBatchedGeneratedKeys(Statement batchedStatement) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
               ResultSet rs = null;

               try {
                  rs = batchedStatement.getGeneratedKeys();

                  while(rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  if (rs != null) {
                     rs.close();
                  }

               }
            }

         }
      } catch (CJException var12) {
         throw SQLExceptionsMapping.translateException(var12, this.getExceptionInterceptor());
      }
   }

   protected void getBatchedGeneratedKeys(int maxKeys) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
               ResultSet rs = null;

               try {
                  rs = maxKeys == 0 ? this.getGeneratedKeysInternal() : this.getGeneratedKeysInternal((long)maxKeys);

                  while(rs.next()) {
                     this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}, this.getExceptionInterceptor()));
                  }
               } finally {
                  this.isImplicitlyClosingResults = true;

                  try {
                     if (rs != null) {
                        rs.close();
                     }
                  } finally {
                     this.isImplicitlyClosingResults = false;
                  }

               }
            }

         }
      } catch (CJException var27) {
         throw SQLExceptionsMapping.translateException(var27, this.getExceptionInterceptor());
      }
   }

   private boolean useServerFetch() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.session.getPropertySet().getBooleanProperty(PropertyKey.useCursorFetch).getValue()
               && this.query.getResultFetchSize() > 0
               && this.query.getResultType() == Resultset.Type.FORWARD_ONLY;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public boolean isClosed() throws SQLException {
      try {
         JdbcConnection locallyScopedConn = this.connection;
         if (locallyScopedConn == null) {
            return true;
         } else {
            synchronized(locallyScopedConn.getConnectionMutex()) {
               return this.isClosed;
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public boolean isPoolable() throws SQLException {
      try {
         this.checkClosed();
         return this.isPoolable;
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public void setPoolable(boolean poolable) throws SQLException {
      try {
         this.checkClosed();
         this.isPoolable = poolable;
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      try {
         this.checkClosed();
         return iface.isInstance(this);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public <T> T unwrap(Class<T> iface) throws SQLException {
      try {
         try {
            return (T)iface.cast(this);
         } catch (ClassCastException var4) {
            throw SQLError.createSQLException(
               Messages.getString("Common.UnableToUnwrap", new Object[]{iface.toString()}), "S1009", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public InputStream getLocalInfileInputStream() {
      return this.session.getLocalInfileInputStream();
   }

   @Override
   public void setLocalInfileInputStream(InputStream stream) {
      this.session.setLocalInfileInputStream(stream);
   }

   @Override
   public void setPingTarget(PingTarget pingTarget) {
      this.pingTarget = pingTarget;
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   protected boolean containsOnDuplicateKeyInString(String sql) {
      return (!this.dontCheckOnDuplicateKeyUpdateInSQL || this.rewriteBatchedStatements.getValue())
         && QueryInfo.containsOnDuplicateKeyUpdateClause(sql, this.session.getServerSession().isNoBackslashEscapesSet());
   }

   public void closeOnCompletion() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.closeOnCompletion = true;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public boolean isCloseOnCompletion() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.closeOnCompletion;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public long[] executeLargeBatch() throws SQLException {
      try {
         return this.executeBatchInternal();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public long executeLargeUpdate(String sql) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, false);
      } catch (CJException var3) {
         throw SQLExceptionsMapping.translateException(var3, this.getExceptionInterceptor());
      }
   }

   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, autoGeneratedKeys == 1);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnIndexes != null && columnIndexes.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         return this.executeUpdateInternal(sql, false, columnNames != null && columnNames.length > 0);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public long getLargeMaxRows() throws SQLException {
      try {
         return (long)this.getMaxRows();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   public long getLargeUpdateCount() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
               return -1L;
            } else {
               return this.results.hasRows() ? -1L : this.results.getUpdateCount();
            }
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setLargeMaxRows(long max) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (max <= 50000000L && max >= 0L) {
               if (max == 0L) {
                  max = -1L;
               }

               this.maxRows = (int)max;
            } else {
               throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", this.getExceptionInterceptor());
            }
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public String getCurrentDatabase() {
      return this.query.getCurrentDatabase();
   }

   public long getServerStatementId() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, Messages.getString("Statement.65"));
   }

   @Override
   public <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory() {
      return this.resultSetFactory;
   }

   @Override
   public int getId() {
      return this.query.getId();
   }

   @Override
   public void setCancelStatus(Query.CancelStatus cs) {
      this.query.setCancelStatus(cs);
   }

   @Override
   public void checkCancelTimeout() {
      try {
         this.query.checkCancelTimeout();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   public Session getSession() {
      return this.session;
   }

   @Override
   public Object getCancelTimeoutMutex() {
      return this.query.getCancelTimeoutMutex();
   }

   @Override
   public void closeQuery() {
      if (this.query != null) {
         this.query.closeQuery();
      }

   }

   @Override
   public int getResultFetchSize() {
      return this.query.getResultFetchSize();
   }

   @Override
   public void setResultFetchSize(int fetchSize) {
      this.query.setResultFetchSize(fetchSize);
   }

   @Override
   public Resultset.Type getResultType() {
      return this.query.getResultType();
   }

   @Override
   public void setResultType(Resultset.Type resultSetType) {
      this.query.setResultType(resultSetType);
   }

   @Override
   public int getTimeoutInMillis() {
      return this.query.getTimeoutInMillis();
   }

   @Override
   public void setTimeoutInMillis(int timeoutInMillis) {
      this.query.setTimeoutInMillis(timeoutInMillis);
   }

   @Override
   public long getExecuteTime() {
      return this.query.getExecuteTime();
   }

   @Override
   public void setExecuteTime(long executeTime) {
      this.query.setExecuteTime(executeTime);
   }

   @Override
   public AtomicBoolean getStatementExecuting() {
      return this.query.getStatementExecuting();
   }

   @Override
   public void setCurrentDatabase(String currentDb) {
      this.query.setCurrentDatabase(currentDb);
   }

   @Override
   public boolean isClearWarningsCalled() {
      return this.query.isClearWarningsCalled();
   }

   @Override
   public void setClearWarningsCalled(boolean clearWarningsCalled) {
      this.query.setClearWarningsCalled(clearWarningsCalled);
   }

   @Override
   public Query getQuery() {
      return this.query;
   }

   @Override
   public QueryAttributesBindings getQueryAttributesBindings() {
      return this.query.getQueryAttributesBindings();
   }

   @Override
   public void setAttribute(String name, Object value) {
      this.getQueryAttributesBindings().setAttribute(name, value);
   }

   @Override
   public void clearAttributes() {
      QueryAttributesBindings qab = this.getQueryAttributesBindings();
      if (qab != null) {
         qab.clearAttributes();
      }

   }
}
