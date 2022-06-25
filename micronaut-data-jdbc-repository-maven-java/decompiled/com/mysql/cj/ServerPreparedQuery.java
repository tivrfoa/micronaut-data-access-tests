package com.mysql.cj;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.a.ColumnDefinitionFactory;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.result.Field;
import com.mysql.cj.util.StringUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;

public class ServerPreparedQuery extends ClientPreparedQuery {
   public static final int BLOB_STREAM_READ_BUF_SIZE = 8192;
   public static final byte OPEN_CURSOR_FLAG = 1;
   public static final byte PARAMETER_COUNT_AVAILABLE = 8;
   private long serverStatementId;
   private Field[] parameterFields;
   private ColumnDefinition resultFields;
   protected boolean profileSQL = false;
   protected boolean gatherPerfMetrics;
   protected boolean logSlowQueries = false;
   private boolean useAutoSlowLog;
   protected RuntimeProperty<Integer> slowQueryThresholdMillis;
   protected RuntimeProperty<Boolean> explainSlowQueries;
   protected boolean useCursorFetch = false;
   protected boolean queryWasSlow = false;
   protected NativeMessageBuilder commandBuilder = null;

   public static ServerPreparedQuery getInstance(NativeSession sess) {
      return (ServerPreparedQuery)(sess.getPropertySet().getBooleanProperty(PropertyKey.autoGenerateTestcaseScript).getValue()
         ? new ServerPreparedQueryTestcaseGenerator(sess)
         : new ServerPreparedQuery(sess));
   }

   protected ServerPreparedQuery(NativeSession sess) {
      super(sess);
      this.profileSQL = sess.getPropertySet().getBooleanProperty(PropertyKey.profileSQL).getValue();
      this.gatherPerfMetrics = sess.getPropertySet().getBooleanProperty(PropertyKey.gatherPerfMetrics).getValue();
      this.logSlowQueries = sess.getPropertySet().getBooleanProperty(PropertyKey.logSlowQueries).getValue();
      this.useAutoSlowLog = sess.getPropertySet().getBooleanProperty(PropertyKey.autoSlowLog).getValue();
      this.slowQueryThresholdMillis = sess.getPropertySet().getIntegerProperty(PropertyKey.slowQueryThresholdMillis);
      this.explainSlowQueries = sess.getPropertySet().getBooleanProperty(PropertyKey.explainSlowQueries);
      this.useCursorFetch = sess.getPropertySet().getBooleanProperty(PropertyKey.useCursorFetch).getValue();
      this.commandBuilder = (NativeMessageBuilder)sess.getProtocol().getMessageBuilder();
   }

   public void serverPrepare(String sql) throws IOException {
      this.session.checkClosed();
      synchronized(this.session) {
         long begin = this.profileSQL ? System.currentTimeMillis() : 0L;
         NativePacketPayload prepareResultPacket = this.session
            .getProtocol()
            .sendCommand(
               this.commandBuilder
                  .buildComStmtPrepare(
                     this.session.getSharedSendPacket(), sql, (String)this.session.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue()
                  ),
               false,
               0
            );
         prepareResultPacket.setPosition(1);
         this.serverStatementId = prepareResultPacket.readInteger(NativeConstants.IntegerDataType.INT4);
         int fieldCount = (int)prepareResultPacket.readInteger(NativeConstants.IntegerDataType.INT2);
         this.setParameterCount((int)prepareResultPacket.readInteger(NativeConstants.IntegerDataType.INT2));
         this.queryBindings = new NativeQueryBindings(this.parameterCount, this.session, NativeQueryBindValue::new);
         if (this.gatherPerfMetrics) {
            this.session.getProtocol().getMetricsHolder().incrementNumberOfPrepares();
         }

         if (this.profileSQL) {
            this.session
               .getProfilerEventHandler()
               .processEvent(
                  (byte)2, this.session, this, null, this.session.getCurrentTimeNanosOrMillis() - begin, new Throwable(), this.truncateQueryToLog(sql)
               );
         }

         boolean checkEOF = !this.session.getServerSession().isEOFDeprecated();
         if (this.parameterCount > 0) {
            if (checkEOF) {
               this.session.getProtocol().skipPacket();
            }

            this.parameterFields = this.session
               .getProtocol()
               .read(ColumnDefinition.class, new ColumnDefinitionFactory((long)this.parameterCount, null))
               .getFields();
         }

         if (fieldCount > 0) {
            this.resultFields = this.session.getProtocol().read(ColumnDefinition.class, new ColumnDefinitionFactory((long)fieldCount, null));
         }

      }
   }

   @Override
   public void statementBegins() {
      super.statementBegins();
      this.queryWasSlow = false;
   }

   public <T extends Resultset> T serverExecute(
      int maxRowsToRetrieve, boolean createStreamingResultSet, ColumnDefinition metadata, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) {
      if (this.session.shouldIntercept()) {
         T interceptedResults = this.session.invokeQueryInterceptorsPre(() -> this.getOriginalSql(), this, true);
         if (interceptedResults != null) {
            return interceptedResults;
         }
      }

      String queryAsString = !this.profileSQL && !this.logSlowQueries && !this.gatherPerfMetrics ? "" : this.asSql();
      return this.readExecuteResult(
         this.sendExecutePacket(this.prepareExecutePacket(), queryAsString),
         maxRowsToRetrieve,
         createStreamingResultSet,
         metadata,
         resultSetFactory,
         queryAsString
      );
   }

   public NativePacketPayload prepareExecutePacket() {
      BindValue[] bindValues = this.queryBindings.getBindValues();
      if (this.queryBindings.isLongParameterSwitchDetected()) {
         boolean firstFound = false;
         long boundTimeToCheck = 0L;

         for(int i = 0; i < this.parameterCount - 1; ++i) {
            if (bindValues[i].isStream()) {
               if (firstFound && boundTimeToCheck != bindValues[i].getBoundBeforeExecutionNum()) {
                  throw ExceptionFactory.createException(
                     Messages.getString("ServerPreparedStatement.11") + Messages.getString("ServerPreparedStatement.12"),
                     "S1C00",
                     0,
                     true,
                     null,
                     this.session.getExceptionInterceptor()
                  );
               }

               firstFound = true;
               boundTimeToCheck = bindValues[i].getBoundBeforeExecutionNum();
            }
         }

         this.serverResetStatement();
      }

      this.queryBindings.checkAllParametersSet();

      for(int i = 0; i < this.parameterCount; ++i) {
         if (bindValues[i].isStream()) {
            this.serverLongData(i, bindValues[i]);
         }
      }

      boolean sendQueryAttributes = false;
      if (this.session.getServerSession().supportsQueryAttributes()) {
         sendQueryAttributes = this.session.getServerSession().getServerVersion().meetsMinimum(new ServerVersion(8, 0, 26));
      } else if (this.queryAttributesBindings.getCount() > 0) {
         this.session.getLog().logWarn(Messages.getString("QueryAttributes.SetButNotSupported"));
      }

      byte flags = 0;
      if (this.resultFields != null
         && this.resultFields.getFields() != null
         && this.useCursorFetch
         && this.resultSetType == Resultset.Type.FORWARD_ONLY
         && this.fetchSize > 0) {
         flags = (byte)(flags | 1);
      }

      if (sendQueryAttributes) {
         flags = (byte)(flags | 8);
      }

      return this.commandBuilder.buildComStmtExecute(this.session.getSharedSendPacket(), this.serverStatementId, flags, sendQueryAttributes, this);
   }

   public NativePacketPayload sendExecutePacket(NativePacketPayload packet, String queryAsString) {
      long begin = this.session.getCurrentTimeNanosOrMillis();
      this.resetCancelledState();
      CancelQueryTask timeoutTask = null;

      NativePacketPayload var11;
      try {
         timeoutTask = this.startQueryTimer(this, this.timeoutInMillis);
         this.statementBegins();
         NativePacketPayload resultPacket = this.session.getProtocol().sendCommand(packet, false, 0);
         long queryEndTime = this.session.getCurrentTimeNanosOrMillis();
         if (timeoutTask != null) {
            this.stopQueryTimer(timeoutTask, true, true);
            timeoutTask = null;
         }

         long executeTime = queryEndTime - begin;
         this.setExecuteTime(executeTime);
         if (this.logSlowQueries) {
            this.queryWasSlow = this.useAutoSlowLog
               ? this.session.getProtocol().getMetricsHolder().checkAbonormallyLongQuery(executeTime)
               : executeTime > (long)((Integer)this.slowQueryThresholdMillis.getValue()).intValue();
            if (this.queryWasSlow) {
               this.session
                  .getProfilerEventHandler()
                  .processEvent(
                     (byte)6,
                     this.session,
                     this,
                     null,
                     executeTime,
                     new Throwable(),
                     Messages.getString(
                        "ServerPreparedStatement.15",
                        new String[]{String.valueOf(this.session.getSlowQueryThreshold()), String.valueOf(executeTime), this.originalSql, queryAsString}
                     )
                  );
            }
         }

         if (this.gatherPerfMetrics) {
            this.session.getProtocol().getMetricsHolder().registerQueryExecutionTime(executeTime);
            this.session.getProtocol().getMetricsHolder().incrementNumberOfPreparedExecutes();
         }

         if (this.profileSQL) {
            this.session
               .getProfilerEventHandler()
               .processEvent((byte)4, this.session, this, null, executeTime, new Throwable(), this.truncateQueryToLog(queryAsString));
         }

         var11 = resultPacket;
      } catch (CJException var15) {
         if (this.session.shouldIntercept()) {
            this.session.invokeQueryInterceptorsPost(() -> this.getOriginalSql(), this, null, true);
         }

         throw var15;
      } finally {
         this.statementExecuting.set(false);
         this.stopQueryTimer(timeoutTask, false, false);
      }

      return var11;
   }

   public <T extends Resultset> T readExecuteResult(
      NativePacketPayload resultPacket,
      int maxRowsToRetrieve,
      boolean createStreamingResultSet,
      ColumnDefinition metadata,
      ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory,
      String queryAsString
   ) {
      try {
         long fetchStartTime = this.profileSQL ? this.session.getCurrentTimeNanosOrMillis() : 0L;
         T rs = this.session
            .getProtocol()
            .readAllResults(maxRowsToRetrieve, createStreamingResultSet, resultPacket, true, metadata != null ? metadata : this.resultFields, resultSetFactory);
         if (this.session.shouldIntercept()) {
            T interceptedResults = this.session.invokeQueryInterceptorsPost(() -> this.getOriginalSql(), this, rs, true);
            if (interceptedResults != null) {
               rs = interceptedResults;
            }
         }

         if (this.profileSQL) {
            this.session
               .getProfilerEventHandler()
               .processEvent((byte)5, this.session, this, rs, this.session.getCurrentTimeNanosOrMillis() - fetchStartTime, new Throwable(), null);
         }

         if (this.queryWasSlow && this.explainSlowQueries.getValue()) {
            this.session.getProtocol().explainSlowQuery(queryAsString, queryAsString);
         }

         this.queryBindings.getSendTypesToServer().set(false);
         if (this.session.hadWarnings()) {
            this.session.getProtocol().scanForAndThrowDataTruncation();
         }

         return rs;
      } catch (IOException var11) {
         throw ExceptionFactory.createCommunicationsException(
            this.session.getPropertySet(),
            this.session.getServerSession(),
            this.session.getProtocol().getPacketSentTimeHolder(),
            this.session.getProtocol().getPacketReceivedTimeHolder(),
            var11,
            this.session.getExceptionInterceptor()
         );
      } catch (CJException var12) {
         if (this.session.shouldIntercept()) {
            this.session.invokeQueryInterceptorsPost(() -> this.getOriginalSql(), this, (T)null, true);
         }

         throw var12;
      }
   }

   private void serverLongData(int parameterIndex, BindValue binding) {
      synchronized(this) {
         NativePacketPayload packet = this.session.getSharedSendPacket();
         Object value = binding.getValue();
         if (value instanceof byte[]) {
            this.session
               .getProtocol()
               .sendCommand(this.commandBuilder.buildComStmtSendLongData(packet, this.serverStatementId, parameterIndex, (byte[])value), true, 0);
         } else if (value instanceof InputStream) {
            this.storeStreamOrReader(parameterIndex, packet, (InputStream)value);
         } else if (value instanceof Blob) {
            try {
               this.storeStreamOrReader(parameterIndex, packet, ((Blob)value).getBinaryStream());
            } catch (Throwable var9) {
               throw ExceptionFactory.createException(var9.getMessage(), this.session.getExceptionInterceptor());
            }
         } else if (value instanceof Reader) {
            if (binding.isNational() && !this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
               throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.31"), this.session.getExceptionInterceptor());
            }

            this.storeStreamOrReader(parameterIndex, packet, (Reader)value);
         } else {
            if (!(value instanceof Clob)) {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("ServerPreparedStatement.18") + value.getClass().getName() + "'",
                  this.session.getExceptionInterceptor()
               );
            }

            if (binding.isNational() && !this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
               throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.31"), this.session.getExceptionInterceptor());
            }

            try {
               this.storeStreamOrReader(parameterIndex, packet, ((Clob)value).getCharacterStream());
            } catch (Throwable var8) {
               throw ExceptionFactory.createException(var8.getMessage(), var8);
            }
         }

      }
   }

   @Override
   public void closeQuery() {
      this.queryBindings = null;
      this.parameterFields = null;
      this.resultFields = null;
      super.closeQuery();
   }

   public long getServerStatementId() {
      return this.serverStatementId;
   }

   public void setServerStatementId(long serverStatementId) {
      this.serverStatementId = serverStatementId;
   }

   public Field[] getParameterFields() {
      return this.parameterFields;
   }

   public void setParameterFields(Field[] parameterFields) {
      this.parameterFields = parameterFields;
   }

   public ColumnDefinition getResultFields() {
      return this.resultFields;
   }

   public void setResultFields(ColumnDefinition resultFields) {
      this.resultFields = resultFields;
   }

   private void storeStreamOrReader(int parameterIndex, NativePacketPayload packet, Closeable streamOrReader) {
      this.session.checkClosed();
      boolean isStream = InputStream.class.isAssignableFrom(streamOrReader.getClass());
      byte[] bBuf = null;
      char[] cBuf = null;
      String clobEncoding = null;
      synchronized(this.session) {
         if (isStream) {
            bBuf = new byte[8192];
         } else {
            clobEncoding = this.session.getPropertySet().getStringProperty(PropertyKey.clobCharacterEncoding).getStringValue();
            if (clobEncoding == null) {
               clobEncoding = (String)this.session.getPropertySet().getStringProperty(PropertyKey.characterEncoding).getValue();
            }

            int maxBytesChar = 2;
            if (clobEncoding != null) {
               maxBytesChar = this.session.getServerSession().getCharsetSettings().getMaxBytesPerChar(clobEncoding);
               if (maxBytesChar == 1) {
                  maxBytesChar = 2;
               }
            }

            cBuf = new char[8192 / maxBytesChar];
         }

         boolean readAny = false;
         int bytesInPacket = 0;
         int totalBytesRead = 0;
         int bytesReadAtLastSend = 0;
         int packetIsFullAt = this.session.getPropertySet().getMemorySizeProperty(PropertyKey.blobSendChunkSize).getValue();
         int numRead = 0;

         try {
            packet.setPosition(0);
            this.commandBuilder.buildComStmtSendLongDataHeader(packet, this.serverStatementId, parameterIndex);

            while((numRead = isStream ? ((InputStream)streamOrReader).read(bBuf) : ((Reader)streamOrReader).read(cBuf)) != -1) {
               readAny = true;
               if (isStream) {
                  packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, bBuf, 0, numRead);
                  bytesInPacket += numRead;
                  totalBytesRead += numRead;
               } else {
                  byte[] valueAsBytes = StringUtils.getBytes(cBuf, 0, numRead, clobEncoding);
                  packet.writeBytes(NativeConstants.StringSelfDataType.STRING_EOF, valueAsBytes);
                  bytesInPacket += valueAsBytes.length;
                  totalBytesRead += valueAsBytes.length;
               }

               if (bytesInPacket >= packetIsFullAt) {
                  bytesReadAtLastSend = totalBytesRead;
                  this.session.getProtocol().sendCommand(packet, true, 0);
                  bytesInPacket = 0;
                  packet.setPosition(0);
                  this.commandBuilder.buildComStmtSendLongDataHeader(packet, this.serverStatementId, parameterIndex);
               }
            }

            if (!readAny || totalBytesRead != bytesReadAtLastSend) {
               this.session.getProtocol().sendCommand(packet, true, 0);
            }
         } catch (IOException var25) {
            throw ExceptionFactory.createException(
               (isStream ? Messages.getString("ServerPreparedStatement.24") : Messages.getString("ServerPreparedStatement.25")) + var25.toString(),
               var25,
               this.session.getExceptionInterceptor()
            );
         } finally {
            if (this.autoClosePStmtStreams.getValue() && streamOrReader != null) {
               try {
                  streamOrReader.close();
               } catch (IOException var24) {
               }
            }

         }

      }
   }

   public void clearParameters(boolean clearServerParameters) {
      boolean hadLongData = false;
      if (this.queryBindings != null) {
         hadLongData = this.queryBindings.clearBindValues();
         this.queryBindings.setLongParameterSwitchDetected(!clearServerParameters || !hadLongData);
      }

      if (clearServerParameters && hadLongData) {
         this.serverResetStatement();
      }

   }

   public void serverResetStatement() {
      this.session.checkClosed();
      synchronized(this.session) {
         try {
            this.session.getProtocol().sendCommand(this.commandBuilder.buildComStmtReset(this.session.getSharedSendPacket(), this.serverStatementId), false, 0);
         } finally {
            this.session.clearInputStream();
         }

      }
   }

   @Override
   protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) {
      long maxSizeOfParameterSet = 0L;
      long sizeOfEntireBatch = 11L;
      boolean supportsQueryAttributes = this.session.getServerSession().supportsQueryAttributes();
      if (supportsQueryAttributes) {
         sizeOfEntireBatch += 9L;
         sizeOfEntireBatch += (long)((this.queryAttributesBindings.getCount() + 7) / 8);

         for(int i = 0; i < this.queryAttributesBindings.getCount(); ++i) {
            BindValue queryAttribute = this.queryAttributesBindings.getAttributeValue(i);
            sizeOfEntireBatch += (long)(2 + queryAttribute.getName().length()) + queryAttribute.getBinaryLength();
         }
      }

      for(int i = 0; i < numBatchedArgs; ++i) {
         long sizeOfParameterSet = (long)((this.parameterCount + 7) / 8 + this.parameterCount * 2);
         if (supportsQueryAttributes) {
            sizeOfParameterSet += (long)this.parameterCount;
         }

         BindValue[] bindValues = ((QueryBindings)this.batchedArgs.get(i)).getBindValues();

         for(int j = 0; j < bindValues.length; ++j) {
            sizeOfParameterSet += bindValues[j].getBinaryLength();
         }

         sizeOfEntireBatch += sizeOfParameterSet;
         if (sizeOfParameterSet > maxSizeOfParameterSet) {
            maxSizeOfParameterSet = sizeOfParameterSet;
         }
      }

      return new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
   }

   private String truncateQueryToLog(String sql) {
      String queryStr = null;
      int maxQuerySizeToLog = this.session.getPropertySet().getIntegerProperty(PropertyKey.maxQuerySizeToLog).getValue();
      if (sql.length() > maxQuerySizeToLog) {
         StringBuilder queryBuf = new StringBuilder(maxQuerySizeToLog + 12);
         queryBuf.append(sql.substring(0, maxQuerySizeToLog));
         queryBuf.append(Messages.getString("MysqlIO.25"));
         queryStr = queryBuf.toString();
      } else {
         queryStr = sql;
      }

      return queryStr;
   }

   @Override
   public <M extends Message> M fillSendPacket(QueryBindings bindings) {
      return null;
   }
}
