package com.mysql.cj.jdbc;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.QueryInfo;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.ResultSetImpl;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.protocol.a.result.ByteArrayRow;
import com.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.Row;
import com.mysql.cj.util.SearchMode;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CallableStatement extends ClientPreparedStatement implements java.sql.CallableStatement {
   private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
   private static final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
   private boolean callingStoredFunction = false;
   private ResultSetInternalMethods functionReturnValueResults;
   private boolean hasOutputParams = false;
   private ResultSetInternalMethods outputParameterResults;
   protected boolean outputParamWasNull = false;
   private int[] parameterIndexToRsIndex;
   protected CallableStatement.CallableStatementParamInfo paramInfo;
   private CallableStatement.CallableStatementParam returnValueParam;
   private boolean noAccessToProcedureBodies;
   private int[] placeholderToParameterIndexMap;

   private static String mangleParameterName(String origParameterName) {
      if (origParameterName == null) {
         return null;
      } else {
         int offset = 0;
         if (origParameterName.length() > 0 && origParameterName.charAt(0) == '@') {
            offset = 1;
         }

         StringBuilder paramNameBuf = new StringBuilder("@com_mysql_jdbc_outparam_".length() + origParameterName.length());
         paramNameBuf.append("@com_mysql_jdbc_outparam_");
         paramNameBuf.append(origParameterName.substring(offset));
         return paramNameBuf.toString();
      }
   }

   public CallableStatement(JdbcConnection conn, CallableStatement.CallableStatementParamInfo paramInfo) throws SQLException {
      super(conn, paramInfo.nativeSql, paramInfo.dbInUse);
      this.paramInfo = paramInfo;
      this.callingStoredFunction = this.paramInfo.isFunctionCall;
      if (this.callingStoredFunction) {
         ((PreparedQuery)this.query).setParameterCount(((PreparedQuery)this.query).getParameterCount() + 1);
      }

      this.retrieveGeneratedKeys = true;
      this.noAccessToProcedureBodies = conn.getPropertySet().getBooleanProperty(PropertyKey.noAccessToProcedureBodies).getValue();
   }

   protected static CallableStatement getInstance(JdbcConnection conn, String sql, String db, boolean isFunctionCall) throws SQLException {
      return new CallableStatement(conn, sql, db, isFunctionCall);
   }

   protected static CallableStatement getInstance(JdbcConnection conn, CallableStatement.CallableStatementParamInfo paramInfo) throws SQLException {
      return new CallableStatement(conn, paramInfo);
   }

   private void generateParameterMap() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.paramInfo != null) {
               int parameterCountFromMetaData = this.paramInfo.getParameterCount();
               if (this.callingStoredFunction) {
                  --parameterCountFromMetaData;
               }

               PreparedQuery q = (PreparedQuery)this.query;
               if (this.paramInfo != null && q.getParameterCount() != parameterCountFromMetaData) {
                  this.placeholderToParameterIndexMap = new int[q.getParameterCount()];
                  int startPos = this.callingStoredFunction
                     ? StringUtils.indexOfIgnoreCase(q.getOriginalSql(), "SELECT")
                     : StringUtils.indexOfIgnoreCase(q.getOriginalSql(), "CALL");
                  if (startPos != -1) {
                     int parenOpenPos = q.getOriginalSql().indexOf(40, startPos + 4);
                     if (parenOpenPos != -1) {
                        int parenClosePos = StringUtils.indexOfIgnoreCase(parenOpenPos, q.getOriginalSql(), ")", "'", "'", SearchMode.__FULL);
                        if (parenClosePos != -1) {
                           List<?> parsedParameters = StringUtils.split(q.getOriginalSql().substring(parenOpenPos + 1, parenClosePos), ",", "'\"", "'\"", true);
                           int numParsedParameters = parsedParameters.size();
                           if (numParsedParameters != q.getParameterCount()) {
                           }

                           int placeholderCount = 0;

                           for(int i = 0; i < numParsedParameters; ++i) {
                              if (((String)parsedParameters.get(i)).equals("?")) {
                                 this.placeholderToParameterIndexMap[placeholderCount++] = i;
                              }
                           }
                        }
                     }
                  }
               }

            }
         }
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   public CallableStatement(JdbcConnection conn, String sql, String db, boolean isFunctionCall) throws SQLException {
      super(conn, sql, db);
      this.callingStoredFunction = isFunctionCall;
      if (!this.callingStoredFunction) {
         if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL")) {
            this.fakeParameterTypes(false);
         } else {
            this.determineParameterTypes();
         }

         this.generateParameterMap();
      } else {
         this.determineParameterTypes();
         this.generateParameterMap();
         ((PreparedQuery)this.query).setParameterCount(((PreparedQuery)this.query).getParameterCount() + 1);
      }

      this.retrieveGeneratedKeys = true;
      this.noAccessToProcedureBodies = conn.getPropertySet().getBooleanProperty(PropertyKey.noAccessToProcedureBodies).getValue();
   }

   @Override
   public void addBatch() throws SQLException {
      try {
         this.setOutParams();
         super.addBatch();
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   private CallableStatement.CallableStatementParam checkIsOutputParam(int paramIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
               if (paramIndex == 1) {
                  if (this.returnValueParam == null) {
                     this.returnValueParam = new CallableStatement.CallableStatementParam(
                        "", 0, false, true, MysqlType.VARCHAR.getJdbcType(), "VARCHAR", 0, 0, (short)2, 5
                     );
                  }

                  return this.returnValueParam;
               }

               --paramIndex;
            }

            this.checkParameterIndexBounds(paramIndex);
            int localParamIndex = paramIndex - 1;
            if (this.placeholderToParameterIndexMap != null) {
               localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
            }

            CallableStatement.CallableStatementParam paramDescriptor = this.paramInfo.getParameter(localParamIndex);
            if (this.noAccessToProcedureBodies) {
               paramDescriptor.isOut = true;
               paramDescriptor.isIn = true;
               paramDescriptor.inOutModifier = 2;
            } else if (!paramDescriptor.isOut) {
               throw SQLError.createSQLException(Messages.getString("CallableStatement.9", new Object[]{paramIndex}), "S1009", this.getExceptionInterceptor());
            }

            this.hasOutputParams = true;
            return paramDescriptor;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   private void checkParameterIndexBounds(int paramIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.paramInfo.checkBounds(paramIndex);
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   private void checkStreamability() throws SQLException {
      if (this.hasOutputParams && this.createStreamingResultSet()) {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.14"), "S1C00", this.getExceptionInterceptor());
      }
   }

   @Override
   public void clearParameters() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            super.clearParameters();

            try {
               if (this.outputParameterResults != null) {
                  this.outputParameterResults.close();
               }
            } finally {
               this.outputParameterResults = null;
            }

         }
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.getExceptionInterceptor());
      }
   }

   private void fakeParameterTypes(boolean isReallyProcedure) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            String encoding = this.connection.getSession().getServerSession().getCharsetSettings().getMetadataEncoding();
            int collationIndex = this.connection.getSession().getServerSession().getCharsetSettings().getMetadataCollationIndex();
            Field[] fields = new Field[]{
               new Field("", "PROCEDURE_CAT", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "PROCEDURE_SCHEM", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "PROCEDURE_NAME", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "COLUMN_NAME", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "COLUMN_TYPE", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "DATA_TYPE", collationIndex, encoding, MysqlType.SMALLINT, 0),
               new Field("", "TYPE_NAME", collationIndex, encoding, MysqlType.CHAR, 0),
               new Field("", "PRECISION", collationIndex, encoding, MysqlType.INT, 0),
               new Field("", "LENGTH", collationIndex, encoding, MysqlType.INT, 0),
               new Field("", "SCALE", collationIndex, encoding, MysqlType.SMALLINT, 0),
               new Field("", "RADIX", collationIndex, encoding, MysqlType.SMALLINT, 0),
               new Field("", "NULLABLE", collationIndex, encoding, MysqlType.SMALLINT, 0),
               new Field("", "REMARKS", collationIndex, encoding, MysqlType.CHAR, 0)
            };
            String procName = isReallyProcedure ? this.extractProcedureName() : null;
            byte[] procNameAsBytes = null;
            procNameAsBytes = procName == null ? null : StringUtils.getBytes(procName, "UTF-8");
            ArrayList<Row> resultRows = new ArrayList();

            for(int i = 0; i < ((PreparedQuery)this.query).getParameterCount(); ++i) {
               byte[][] row = new byte[][]{
                  null,
                  null,
                  procNameAsBytes,
                  this.s2b(String.valueOf(i)),
                  this.s2b(String.valueOf(1)),
                  this.s2b(String.valueOf(MysqlType.VARCHAR.getJdbcType())),
                  this.s2b(MysqlType.VARCHAR.getName()),
                  this.s2b(Integer.toString(65535)),
                  this.s2b(Integer.toString(65535)),
                  this.s2b(Integer.toString(0)),
                  this.s2b(Integer.toString(10)),
                  this.s2b(Integer.toString(2)),
                  null
               };
               resultRows.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }

            ResultSet paramTypesRs = this.resultSetFactory
               .createFromResultsetRows(1007, 1004, new ResultsetRowsStatic(resultRows, new DefaultColumnDefinition(fields)));
            this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
         }
      } catch (CJException var14) {
         throw SQLExceptionsMapping.translateException(var14, this.getExceptionInterceptor());
      }
   }

   private void determineParameterTypes() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSet paramTypesRs = null;

            try {
               String procName = this.extractProcedureName();
               String quotedId = this.session.getIdentifierQuoteString();
               List<?> parseList = StringUtils.splitDBdotName(procName, "", quotedId, this.session.getServerSession().isNoBackslashEscapesSet());
               String tmpDb = "";
               if (parseList.size() == 2) {
                  tmpDb = (String)parseList.get(0);
                  procName = (String)parseList.get(1);
               }

               java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
               boolean useDb = false;
               if (tmpDb.length() <= 0) {
                  useDb = true;
               }

               paramTypesRs = this.session.getPropertySet().getEnumProperty(PropertyKey.databaseTerm).getValue() == PropertyDefinitions.DatabaseTerm.SCHEMA
                  ? dbmd.getProcedureColumns(null, useDb ? this.getCurrentDatabase() : tmpDb, procName, "%")
                  : dbmd.getProcedureColumns(useDb ? this.getCurrentDatabase() : tmpDb, null, procName, "%");
               boolean hasResults = false;

               try {
                  if (paramTypesRs.next()) {
                     paramTypesRs.previous();
                     hasResults = true;
                  }
               } catch (Exception var23) {
               }

               if (hasResults) {
                  this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
               } else {
                  this.fakeParameterTypes(true);
               }
            } finally {
               SQLException sqlExRethrow = null;
               if (paramTypesRs != null) {
                  try {
                     paramTypesRs.close();
                  } catch (SQLException var22) {
                     sqlExRethrow = var22;
                  }

                  ResultSet var27 = null;
               }

               if (sqlExRethrow != null) {
                  throw sqlExRethrow;
               }

            }

         }
      } catch (CJException var26) {
         throw SQLExceptionsMapping.translateException(var26, this.getExceptionInterceptor());
      }
   }

   private void convertGetProcedureColumnsToInternalDescriptors(ResultSet paramTypesRs) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.paramInfo = new CallableStatement.CallableStatementParamInfo(paramTypesRs);
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public boolean execute() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            boolean returnVal = false;
            this.checkStreamability();
            this.setInOutParamsOnServer();
            this.setOutParams();
            returnVal = super.execute();
            if (this.callingStoredFunction) {
               this.functionReturnValueResults = this.results;
               this.functionReturnValueResults.next();
               this.results = null;
            }

            this.retrieveOutParams();
            return !this.callingStoredFunction ? returnVal : false;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public ResultSet executeQuery() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.checkStreamability();
            ResultSet execResults = null;
            this.setInOutParamsOnServer();
            this.setOutParams();
            execResults = super.executeQuery();
            this.retrieveOutParams();
            return execResults;
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public int executeUpdate() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeUpdate());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   private String extractProcedureName() throws SQLException {
      String sanitizedSql = StringUtils.stripCommentsAndHints(
         ((PreparedQuery)this.query).getOriginalSql(), "`'\"", "`'\"", !this.session.getServerSession().isNoBackslashEscapesSet()
      );
      int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
      int offset = 5;
      if (endCallIndex == -1) {
         endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
         offset = 7;
      }

      if (endCallIndex == -1) {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), "S1000", this.getExceptionInterceptor());
      } else {
         StringBuilder nameBuf = new StringBuilder();
         String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
         int statementLength = trimmedStatement.length();

         for(int i = 0; i < statementLength; ++i) {
            char c = trimmedStatement.charAt(i);
            if (Character.isWhitespace(c) || c == '(' || c == '?') {
               break;
            }

            nameBuf.append(c);
         }

         return nameBuf.toString();
      }
   }

   protected String fixParameterName(String paramNameIn) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (paramNameIn == null) {
               paramNameIn = "nullpn";
            }

            if (this.noAccessToProcedureBodies) {
               throw SQLError.createSQLException(Messages.getString("CallableStatement.23"), "S1009", this.getExceptionInterceptor());
            } else {
               return mangleParameterName(paramNameIn);
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public Array getArray(int i) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(i);
            Array retValue = rs.getArray(this.mapOutputParameterIndexToRsIndex(i));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Array getArray(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Array retValue = rs.getArray(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   @Deprecated
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex), scale);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public BigDecimal getBigDecimal(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            BigDecimal retValue = rs.getBigDecimal(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public java.sql.Blob getBlob(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            java.sql.Blob retValue = rs.getBlob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public java.sql.Blob getBlob(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            java.sql.Blob retValue = rs.getBlob(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public boolean getBoolean(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            boolean retValue = rs.getBoolean(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public boolean getBoolean(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            boolean retValue = rs.getBoolean(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public byte getByte(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            byte retValue = rs.getByte(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public byte getByte(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            byte retValue = rs.getByte(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public byte[] getBytes(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            byte[] retValue = rs.getBytes(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public byte[] getBytes(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            byte[] retValue = rs.getBytes(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public java.sql.Clob getClob(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            java.sql.Clob retValue = rs.getClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public java.sql.Clob getClob(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            java.sql.Clob retValue = rs.getClob(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Date getDate(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Date getDate(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Date retValue = rs.getDate(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Date getDate(String parameterName, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Date retValue = rs.getDate(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public double getDouble(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            double retValue = rs.getDouble(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public double getDouble(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            double retValue = rs.getDouble(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public float getFloat(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            float retValue = rs.getFloat(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public float getFloat(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            float retValue = rs.getFloat(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public int getInt(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            int retValue = rs.getInt(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public int getInt(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            int retValue = rs.getInt(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public long getLong(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            long retValue = rs.getLong(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public long getLong(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            long retValue = rs.getLong(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   protected int getNamedParamIndex(String paramName, boolean forOut) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.noAccessToProcedureBodies) {
               throw SQLError.createSQLException(
                  "No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", this.getExceptionInterceptor()
               );
            } else if (paramName != null && paramName.length() != 0) {
               CallableStatement.CallableStatementParam namedParamInfo;
               if (this.paramInfo != null && (namedParamInfo = this.paramInfo.getParameter(paramName)) != null) {
                  if (forOut && !namedParamInfo.isOut) {
                     throw SQLError.createSQLException(
                        Messages.getString("CallableStatement.5", new Object[]{paramName}), "S1009", this.getExceptionInterceptor()
                     );
                  } else if (this.placeholderToParameterIndexMap == null) {
                     return namedParamInfo.index + 1;
                  } else {
                     for(int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                        if (this.placeholderToParameterIndexMap[i] == namedParamInfo.index) {
                           return i + 1;
                        }
                     }

                     throw SQLError.createSQLException(
                        Messages.getString("CallableStatement.6", new Object[]{paramName}), "S1009", this.getExceptionInterceptor()
                     );
                  }
               } else {
                  throw SQLError.createSQLException(Messages.getString("CallableStatement.3", new Object[]{paramName}), "S1009", this.getExceptionInterceptor());
               }
            } else {
               throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), "S1009", this.getExceptionInterceptor());
            }
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Object getObject(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            CallableStatement.CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Object retVal = rs.getObjectStoredProc(this.mapOutputParameterIndexToRsIndex(parameterIndex), paramDescriptor.desiredMysqlType.getJdbcType());
            this.outputParamWasNull = rs.wasNull();
            return retVal;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Object retVal = rs.getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), map);
            this.outputParamWasNull = rs.wasNull();
            return retVal;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Object getObject(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Object retValue = rs.getObject(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Object retValue = rs.getObject(this.fixParameterName(parameterName), map);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            T retVal = ((ResultSetImpl)rs).getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), type);
            this.outputParamWasNull = rs.wasNull();
            return retVal;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            T retValue = ((ResultSetImpl)rs).getObject(this.fixParameterName(parameterName), type);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   protected ResultSetInternalMethods getOutputParameters(int paramIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.outputParamWasNull = false;
            if (paramIndex == 1 && this.callingStoredFunction && this.returnValueParam != null) {
               return this.functionReturnValueResults;
            } else if (this.outputParameterResults == null) {
               if (this.paramInfo.numberOfParameters() == 0) {
                  throw SQLError.createSQLException(Messages.getString("CallableStatement.7"), "S1009", this.getExceptionInterceptor());
               } else {
                  throw SQLError.createSQLException(Messages.getString("CallableStatement.8"), "S1000", this.getExceptionInterceptor());
               }
            } else {
               return this.outputParameterResults;
            }
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.placeholderToParameterIndexMap == null ? this.paramInfo : new CallableStatement.CallableStatementParamInfo(this.paramInfo);
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public Ref getRef(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Ref retValue = rs.getRef(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Ref getRef(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Ref retValue = rs.getRef(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public short getShort(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            short retValue = rs.getShort(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public short getShort(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            short retValue = rs.getShort(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public String getString(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            String retValue = rs.getString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public String getString(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            String retValue = rs.getString(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Time getTime(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Time getTime(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Time retValue = rs.getTime(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Time getTime(String parameterName, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Time retValue = rs.getTime(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Timestamp getTimestamp(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public Timestamp getTimestamp(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   public URL getURL(int parameterIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
            URL retValue = rs.getURL(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public URL getURL(String parameterName) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = this.getOutputParameters(0);
            URL retValue = rs.getURL(this.fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
            return retValue;
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected int mapOutputParameterIndexToRsIndex(int paramIndex) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.returnValueParam != null && paramIndex == 1) {
               return 1;
            } else {
               this.checkParameterIndexBounds(paramIndex);
               int localParamIndex = paramIndex - 1;
               if (this.placeholderToParameterIndexMap != null) {
                  localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
               }

               int rsIndex = this.parameterIndexToRsIndex[localParamIndex];
               if (rsIndex == Integer.MIN_VALUE) {
                  throw SQLError.createSQLException(
                     Messages.getString("CallableStatement.21", new Object[]{paramIndex}), "S1009", this.getExceptionInterceptor()
                  );
               } else {
                  return rsIndex + 1;
               }
            }
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   protected void registerOutParameter(int parameterIndex, MysqlType mysqlType) throws SQLException {
      CallableStatement.CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
      paramDescriptor.desiredMysqlType = mysqlType;
   }

   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
      try {
         try {
            MysqlType mt = MysqlType.getByJdbcType(sqlType);
            this.registerOutParameter(parameterIndex, mt);
         } catch (FeatureNotAvailableException var5) {
            throw SQLError.createSQLFeatureNotSupportedException(
               Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(sqlType), "S1C00", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(parameterIndex, (MysqlType)sqlType);
         } else {
            this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber());
         }

      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   protected void registerOutParameter(int parameterIndex, MysqlType mysqlType, int scale) throws SQLException {
      this.registerOutParameter(parameterIndex, mysqlType);
   }

   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
      try {
         this.registerOutParameter(parameterIndex, sqlType);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(parameterIndex, (MysqlType)sqlType, scale);
         } else {
            this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), scale);
         }

      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected void registerOutParameter(int parameterIndex, MysqlType mysqlType, String typeName) throws SQLException {
      this.registerOutParameter(parameterIndex, mysqlType);
   }

   public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
      try {
         try {
            MysqlType mt = MysqlType.getByJdbcType(sqlType);
            this.registerOutParameter(parameterIndex, mt, typeName);
         } catch (FeatureNotAvailableException var6) {
            throw SQLError.createSQLFeatureNotSupportedException(
               Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(sqlType), "S1C00", this.getExceptionInterceptor()
            );
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(parameterIndex, (MysqlType)sqlType, typeName);
         } else {
            this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), typeName);
         }

      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), (MysqlType)sqlType);
         } else {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType.getVendorTypeNumber());
         }

      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
      try {
         this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType, scale);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), (MysqlType)sqlType, scale);
         } else {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType.getVendorTypeNumber(), scale);
         }

      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
      try {
         this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType, typeName);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
      try {
         if (sqlType instanceof MysqlType) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), (MysqlType)sqlType, typeName);
         } else {
            this.registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), typeName);
         }

      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   private void retrieveOutParams() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            int numParameters = this.paramInfo.numberOfParameters();
            this.parameterIndexToRsIndex = new int[numParameters];

            for(int i = 0; i < numParameters; ++i) {
               this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
            }

            int localParamIndex = 0;
            if (numParameters > 0) {
               StringBuilder outParameterQuery = new StringBuilder("SELECT ");
               boolean firstParam = true;
               boolean hadOutputParams = false;

               for(CallableStatement.CallableStatementParam retrParamInfo : this.paramInfo) {
                  if (retrParamInfo.isOut) {
                     hadOutputParams = true;
                     this.parameterIndexToRsIndex[retrParamInfo.index] = localParamIndex++;
                     if (retrParamInfo.paramName == null) {
                        retrParamInfo.paramName = "nullnp" + retrParamInfo.index;
                     }

                     String outParameterName = mangleParameterName(retrParamInfo.paramName);
                     if (!firstParam) {
                        outParameterQuery.append(",");
                     } else {
                        firstParam = false;
                     }

                     if (!outParameterName.startsWith("@")) {
                        outParameterQuery.append('@');
                     }

                     outParameterQuery.append(outParameterName);
                  }
               }

               if (hadOutputParams) {
                  Statement outParameterStmt = null;
                  ResultSet outParamRs = null;

                  try {
                     outParameterStmt = this.connection.createStatement();
                     outParamRs = outParameterStmt.executeQuery(outParameterQuery.toString());
                     this.outputParameterResults = this.resultSetFactory
                        .createFromResultsetRows(outParamRs.getConcurrency(), outParamRs.getType(), ((ResultSetInternalMethods)outParamRs).getRows());
                     if (!this.outputParameterResults.next()) {
                        this.outputParameterResults.close();
                        this.outputParameterResults = null;
                     }
                  } finally {
                     if (outParameterStmt != null) {
                        outParameterStmt.close();
                     }

                  }
               } else {
                  this.outputParameterResults = null;
               }
            } else {
               this.outputParameterResults = null;
            }

         }
      } catch (CJException var18) {
         throw SQLExceptionsMapping.translateException(var18, this.getExceptionInterceptor());
      }
   }

   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
      try {
         this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
      try {
         this.setBigDecimal(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
      try {
         this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setBoolean(String parameterName, boolean x) throws SQLException {
      try {
         this.setBoolean(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setByte(String parameterName, byte x) throws SQLException {
      try {
         this.setByte(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setBytes(String parameterName, byte[] x) throws SQLException {
      try {
         this.setBytes(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
      try {
         this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setDate(String parameterName, Date x) throws SQLException {
      try {
         this.setDate(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
      try {
         this.setDate(this.getNamedParamIndex(parameterName, false), x, cal);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setDouble(String parameterName, double x) throws SQLException {
      try {
         this.setDouble(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setFloat(String parameterName, float x) throws SQLException {
      try {
         this.setFloat(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   private void setInOutParamsOnServer() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.paramInfo.numParameters > 0) {
               for(CallableStatement.CallableStatementParam inParamInfo : this.paramInfo) {
                  if (inParamInfo.isOut && inParamInfo.isIn) {
                     if (inParamInfo.paramName == null) {
                        inParamInfo.paramName = "nullnp" + inParamInfo.index;
                     }

                     String inOutParameterName = mangleParameterName(inParamInfo.paramName);
                     StringBuilder queryBuf = new StringBuilder(4 + inOutParameterName.length() + 1 + 1);
                     queryBuf.append("SET ");
                     queryBuf.append(inOutParameterName);
                     queryBuf.append("=?");
                     ClientPreparedStatement setPstmt = null;

                     try {
                        setPstmt = (ClientPreparedStatement)this.connection.clientPrepareStatement(queryBuf.toString()).unwrap(ClientPreparedStatement.class);
                        setPstmt.getQueryBindings().setFromBindValue(0, ((PreparedQuery)this.query).getQueryBindings().getBindValues()[inParamInfo.index]);
                        setPstmt.executeUpdate();
                     } finally {
                        if (setPstmt != null) {
                           setPstmt.close();
                        }

                     }
                  }
               }
            }

         }
      } catch (CJException var15) {
         throw SQLExceptionsMapping.translateException(var15, this.getExceptionInterceptor());
      }
   }

   public void setInt(String parameterName, int x) throws SQLException {
      try {
         this.setInt(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setLong(String parameterName, long x) throws SQLException {
      try {
         this.setLong(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setNull(String parameterName, int sqlType) throws SQLException {
      try {
         this.setNull(this.getNamedParamIndex(parameterName, false), sqlType);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
      try {
         this.setNull(this.getNamedParamIndex(parameterName, false), sqlType, typeName);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setObject(String parameterName, Object x) throws SQLException {
      try {
         this.setObject(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
      try {
         this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType);
         }
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.getExceptionInterceptor());
      }
   }

   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
      try {
         this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType, scale);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType, scaleOrLength);
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.getExceptionInterceptor());
      }
   }

   private void setOutParams() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.paramInfo.numParameters > 0) {
               for(CallableStatement.CallableStatementParam outParamInfo : this.paramInfo) {
                  if (!this.callingStoredFunction && outParamInfo.isOut) {
                     if (outParamInfo.paramName == null) {
                        outParamInfo.paramName = "nullnp" + outParamInfo.index;
                     }

                     String outParameterName = mangleParameterName(outParamInfo.paramName);
                     int outParamIndex = 0;
                     if (this.placeholderToParameterIndexMap == null) {
                        outParamIndex = outParamInfo.index + 1;
                     } else {
                        boolean found = false;

                        for(int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                           if (this.placeholderToParameterIndexMap[i] == outParamInfo.index) {
                              outParamIndex = i + 1;
                              found = true;
                              break;
                           }
                        }

                        if (!found) {
                           throw SQLError.createSQLException(
                              Messages.getString("CallableStatement.21", new Object[]{outParamInfo.paramName}), "S1009", this.getExceptionInterceptor()
                           );
                        }
                     }

                     this.setBytes(outParamIndex, StringUtils.getBytes(outParameterName, this.charEncoding), false);
                  }
               }
            }

         }
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.getExceptionInterceptor());
      }
   }

   public void setShort(String parameterName, short x) throws SQLException {
      try {
         this.setShort(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setString(String parameterName, String x) throws SQLException {
      try {
         this.setString(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setTime(String parameterName, Time x) throws SQLException {
      try {
         this.setTime(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
      try {
         this.setTime(this.getNamedParamIndex(parameterName, false), x, cal);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
      try {
         this.setTimestamp(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
      try {
         this.setTimestamp(this.getNamedParamIndex(parameterName, false), x, cal);
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setURL(String parameterName, URL val) throws SQLException {
      try {
         this.setURL(this.getNamedParamIndex(parameterName, false), val);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public boolean wasNull() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            return this.outputParamWasNull;
         }
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   @Override
   public int[] executeBatch() throws SQLException {
      try {
         return Util.truncateAndConvertToInt(this.executeLargeBatch());
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   @Override
   protected int getParameterIndexOffset() {
      return this.callingStoredFunction ? -1 : super.getParameterIndexOffset();
   }

   public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
      try {
         this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
      try {
         this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
      try {
         this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
      try {
         this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setBlob(String parameterName, java.sql.Blob x) throws SQLException {
      try {
         this.setBlob(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
      try {
         this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
      try {
         this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
      try {
         this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
      try {
         this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setClob(String parameterName, java.sql.Clob x) throws SQLException {
      try {
         this.setClob(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setClob(String parameterName, Reader reader) throws SQLException {
      try {
         this.setClob(this.getNamedParamIndex(parameterName, false), reader);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setClob(String parameterName, Reader reader, long length) throws SQLException {
      try {
         this.setClob(this.getNamedParamIndex(parameterName, false), reader, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
      try {
         this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
      try {
         this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   private boolean checkReadOnlyProcedure() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            if (this.noAccessToProcedureBodies) {
               return false;
            } else if (this.paramInfo.isReadOnlySafeChecked) {
               return this.paramInfo.isReadOnlySafeProcedure;
            } else {
               ResultSet rs = null;
               PreparedStatement ps = null;

               label176: {
                  boolean var23;
                  try {
                     String procName = this.extractProcedureName();
                     String db = this.getCurrentDatabase();
                     if (procName.indexOf(".") != -1) {
                        db = procName.substring(0, procName.indexOf("."));
                        if (StringUtils.startsWithIgnoreCaseAndWs(db, "`") && db.trim().endsWith("`")) {
                           db = db.substring(1, db.length() - 1);
                        }

                        procName = procName.substring(procName.indexOf(".") + 1);
                        procName = StringUtils.toString(StringUtils.stripEnclosure(StringUtils.getBytes(procName), "`", "`"));
                     }

                     ps = this.connection
                        .prepareStatement("SELECT SQL_DATA_ACCESS FROM information_schema.routines WHERE routine_schema = ? AND routine_name = ?");
                     ps.setMaxRows(0);
                     ps.setFetchSize(0);
                     ps.setString(1, db);
                     ps.setString(2, procName);
                     rs = ps.executeQuery();
                     if (!rs.next()) {
                        break label176;
                     }

                     String sqlDataAccess = rs.getString(1);
                     if (!"READS SQL DATA".equalsIgnoreCase(sqlDataAccess) && !"NO SQL".equalsIgnoreCase(sqlDataAccess)) {
                        break label176;
                     }

                     synchronized(this.paramInfo) {
                        this.paramInfo.isReadOnlySafeChecked = true;
                        this.paramInfo.isReadOnlySafeProcedure = true;
                     }

                     var23 = true;
                  } catch (SQLException var18) {
                     break label176;
                  } finally {
                     if (rs != null) {
                        rs.close();
                     }

                     if (ps != null) {
                        ps.close();
                     }

                  }

                  return var23;
               }

               this.paramInfo.isReadOnlySafeChecked = false;
               this.paramInfo.isReadOnlySafeProcedure = false;
               return false;
            }
         }
      } catch (CJException var21) {
         throw SQLExceptionsMapping.translateException(var21, this.getExceptionInterceptor());
      }
   }

   @Override
   protected boolean checkReadOnlySafeStatement() throws SQLException {
      if (QueryInfo.isReadOnlySafeQuery(((PreparedQuery)this.query).getOriginalSql(), this.session.getServerSession().isNoBackslashEscapesSet())) {
         String sql = ((PreparedQuery)this.query).getOriginalSql();
         int statementKeywordPos = QueryInfo.indexOfStatementKeyword(sql, this.session.getServerSession().isNoBackslashEscapesSet());
         if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL", statementKeywordPos)
            && !StringUtils.startsWithIgnoreCaseAndWs(sql, "SELECT", statementKeywordPos)) {
            return true;
         } else {
            return !this.connection.isReadOnly() ? true : this.checkReadOnlyProcedure();
         }
      } else {
         return !this.connection.isReadOnly();
      }
   }

   public RowId getRowId(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         RowId retValue = rs.getRowId(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public RowId getRowId(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         RowId retValue = rs.getRowId(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public void setRowId(String parameterName, RowId x) throws SQLException {
      try {
         this.setRowId(this.getNamedParamIndex(parameterName, false), x);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNString(String parameterName, String value) throws SQLException {
      try {
         this.setNString(this.getNamedParamIndex(parameterName, false), value);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNClob(String parameterName, java.sql.NClob value) throws SQLException {
      try {
         this.setNClob(this.getNamedParamIndex(parameterName, false), value);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNClob(String parameterName, Reader reader) throws SQLException {
      try {
         this.setNClob(this.getNamedParamIndex(parameterName, false), reader);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
      try {
         this.setNClob(this.getNamedParamIndex(parameterName, false), reader, length);
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.getExceptionInterceptor());
      }
   }

   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
      try {
         this.setSQLXML(this.getNamedParamIndex(parameterName, false), xmlObject);
      } catch (CJException var4) {
         throw SQLExceptionsMapping.translateException(var4, this.getExceptionInterceptor());
      }
   }

   public SQLXML getSQLXML(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         SQLXML retValue = rs.getSQLXML(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public SQLXML getSQLXML(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         SQLXML retValue = rs.getSQLXML(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public String getNString(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         String retValue = rs.getNString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public String getNString(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         String retValue = rs.getNString(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public Reader getNCharacterStream(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         Reader retValue = rs.getNCharacterStream(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public Reader getNCharacterStream(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         Reader retValue = rs.getNCharacterStream(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public Reader getCharacterStream(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         Reader retValue = rs.getCharacterStream(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public Reader getCharacterStream(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         Reader retValue = rs.getCharacterStream(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public java.sql.NClob getNClob(int parameterIndex) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
         java.sql.NClob retValue = rs.getNClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   public java.sql.NClob getNClob(String parameterName) throws SQLException {
      try {
         ResultSetInternalMethods rs = this.getOutputParameters(0);
         java.sql.NClob retValue = rs.getNClob(this.fixParameterName(parameterName));
         this.outputParamWasNull = rs.wasNull();
         return retValue;
      } catch (CJException var5) {
         throw SQLExceptionsMapping.translateException(var5, this.getExceptionInterceptor());
      }
   }

   protected byte[] s2b(String s) {
      return s == null ? null : StringUtils.getBytes(s, this.charEncoding);
   }

   @Override
   public long executeLargeUpdate() throws SQLException {
      try {
         synchronized(this.checkClosed().getConnectionMutex()) {
            long returnVal = -1L;
            this.checkStreamability();
            if (this.callingStoredFunction) {
               this.execute();
               return -1L;
            } else {
               this.setInOutParamsOnServer();
               this.setOutParams();
               returnVal = super.executeLargeUpdate();
               this.retrieveOutParams();
               return returnVal;
            }
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.getExceptionInterceptor());
      }
   }

   @Override
   public long[] executeLargeBatch() throws SQLException {
      try {
         if (this.hasOutputParams) {
            throw SQLError.createSQLException("Can't call executeBatch() on CallableStatement with OUTPUT parameters", "S1009", this.getExceptionInterceptor());
         } else {
            return super.executeLargeBatch();
         }
      } catch (CJException var2) {
         throw SQLExceptionsMapping.translateException(var2, this.getExceptionInterceptor());
      }
   }

   protected static class CallableStatementParam {
      int index;
      int inOutModifier;
      boolean isIn;
      boolean isOut;
      int jdbcType;
      short nullability;
      String paramName;
      int precision;
      int scale;
      String typeName;
      MysqlType desiredMysqlType = MysqlType.UNKNOWN;

      CallableStatementParam(
         String name, int idx, boolean in, boolean out, int jdbcType, String typeName, int precision, int scale, short nullability, int inOutModifier
      ) {
         this.paramName = name;
         this.isIn = in;
         this.isOut = out;
         this.index = idx;
         this.jdbcType = jdbcType;
         this.typeName = typeName;
         this.precision = precision;
         this.scale = scale;
         this.nullability = nullability;
         this.inOutModifier = inOutModifier;
      }

      protected Object clone() throws CloneNotSupportedException {
         return super.clone();
      }
   }

   public class CallableStatementParamInfo implements ParameterMetaData {
      String dbInUse;
      boolean isFunctionCall;
      String nativeSql;
      int numParameters;
      List<CallableStatement.CallableStatementParam> parameterList;
      Map<String, CallableStatement.CallableStatementParam> parameterMap;
      boolean isReadOnlySafeProcedure = false;
      boolean isReadOnlySafeChecked = false;

      CallableStatementParamInfo(CallableStatement.CallableStatementParamInfo fullParamInfo) {
         this.nativeSql = ((PreparedQuery)CallableStatement.this.query).getOriginalSql();
         this.dbInUse = CallableStatement.this.getCurrentDatabase();
         this.isFunctionCall = fullParamInfo.isFunctionCall;
         int[] localParameterMap = CallableStatement.this.placeholderToParameterIndexMap;
         int parameterMapLength = localParameterMap.length;
         this.isReadOnlySafeProcedure = fullParamInfo.isReadOnlySafeProcedure;
         this.isReadOnlySafeChecked = fullParamInfo.isReadOnlySafeChecked;
         this.parameterList = new ArrayList(fullParamInfo.numParameters);
         this.parameterMap = new HashMap(fullParamInfo.numParameters);
         if (this.isFunctionCall) {
            this.parameterList.add(fullParamInfo.parameterList.get(0));
         }

         int offset = this.isFunctionCall ? 1 : 0;

         for(int i = 0; i < parameterMapLength; ++i) {
            if (localParameterMap[i] != 0) {
               CallableStatement.CallableStatementParam param = (CallableStatement.CallableStatementParam)fullParamInfo.parameterList
                  .get(localParameterMap[i] + offset);
               this.parameterList.add(param);
               this.parameterMap.put(param.paramName, param);
            }
         }

         this.numParameters = this.parameterList.size();
      }

      CallableStatementParamInfo(ResultSet paramTypesRs) throws SQLException {
         boolean hadRows = paramTypesRs.last();
         this.nativeSql = ((PreparedQuery)CallableStatement.this.query).getOriginalSql();
         this.dbInUse = CallableStatement.this.getCurrentDatabase();
         this.isFunctionCall = CallableStatement.this.callingStoredFunction;
         if (hadRows) {
            this.numParameters = paramTypesRs.getRow();
            this.parameterList = new ArrayList(this.numParameters);
            this.parameterMap = new HashMap(this.numParameters);
            paramTypesRs.beforeFirst();
            this.addParametersFromDBMD(paramTypesRs);
         } else {
            this.numParameters = 0;
         }

         if (this.isFunctionCall) {
            ++this.numParameters;
         }

      }

      private void addParametersFromDBMD(ResultSet paramTypesRs) throws SQLException {
         int i = 0;

         while(paramTypesRs.next()) {
            String paramName = paramTypesRs.getString(4);
            int inOutModifier;
            switch(paramTypesRs.getInt(5)) {
               case 1:
                  inOutModifier = 1;
                  break;
               case 2:
                  inOutModifier = 2;
                  break;
               case 3:
               default:
                  inOutModifier = 0;
                  break;
               case 4:
               case 5:
                  inOutModifier = 4;
            }

            boolean isOutParameter = false;
            boolean isInParameter = false;
            if (i == 0 && this.isFunctionCall) {
               isOutParameter = true;
               isInParameter = false;
            } else if (inOutModifier == 2) {
               isOutParameter = true;
               isInParameter = true;
            } else if (inOutModifier == 1) {
               isOutParameter = false;
               isInParameter = true;
            } else if (inOutModifier == 4) {
               isOutParameter = true;
               isInParameter = false;
            }

            int jdbcType = paramTypesRs.getInt(6);
            String typeName = paramTypesRs.getString(7);
            int precision = paramTypesRs.getInt(8);
            int scale = paramTypesRs.getInt(10);
            short nullability = paramTypesRs.getShort(12);
            CallableStatement.CallableStatementParam paramInfoToAdd = new CallableStatement.CallableStatementParam(
               paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier
            );
            this.parameterList.add(paramInfoToAdd);
            this.parameterMap.put(paramName, paramInfoToAdd);
         }

      }

      protected void checkBounds(int paramIndex) throws SQLException {
         int localParamIndex = paramIndex - 1;
         if (paramIndex < 0 || localParamIndex >= this.numParameters) {
            throw SQLError.createSQLException(
               Messages.getString("CallableStatement.11", new Object[]{paramIndex, this.numParameters}),
               "S1009",
               CallableStatement.this.getExceptionInterceptor()
            );
         }
      }

      protected Object clone() throws CloneNotSupportedException {
         return super.clone();
      }

      CallableStatement.CallableStatementParam getParameter(int index) {
         return (CallableStatement.CallableStatementParam)this.parameterList.get(index);
      }

      CallableStatement.CallableStatementParam getParameter(String name) {
         return (CallableStatement.CallableStatementParam)this.parameterMap.get(name);
      }

      public String getParameterClassName(int arg0) throws SQLException {
         try {
            String mysqlTypeName = this.getParameterTypeName(arg0);
            MysqlType mysqlType = MysqlType.getByName(mysqlTypeName);
            switch(mysqlType) {
               case YEAR:
                  if (!CallableStatement.this.session.getPropertySet().getBooleanProperty(PropertyKey.yearIsDateType).getValue()) {
                     return Short.class.getName();
                  }

                  return mysqlType.getClassName();
               default:
                  return mysqlType.getClassName();
            }
         } catch (CJException var5) {
            throw SQLExceptionsMapping.translateException(var5);
         }
      }

      public int getParameterCount() throws SQLException {
         try {
            return this.parameterList == null ? 0 : this.parameterList.size();
         } catch (CJException var2) {
            throw SQLExceptionsMapping.translateException(var2);
         }
      }

      public int getParameterMode(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).inOutModifier;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public int getParameterType(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).jdbcType;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public String getParameterTypeName(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).typeName;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public int getPrecision(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).precision;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public int getScale(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).scale;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public int isNullable(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).nullability;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public boolean isSigned(int arg0) throws SQLException {
         try {
            this.checkBounds(arg0);
            return false;
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      Iterator<CallableStatement.CallableStatementParam> iterator() {
         return this.parameterList.iterator();
      }

      int numberOfParameters() {
         return this.numParameters;
      }

      public boolean isWrapperFor(Class<?> iface) throws SQLException {
         try {
            CallableStatement.this.checkClosed();
            return iface.isInstance(this);
         } catch (CJException var3) {
            throw SQLExceptionsMapping.translateException(var3);
         }
      }

      public <T> T unwrap(Class<T> iface) throws SQLException {
         try {
            try {
               return (T)iface.cast(this);
            } catch (ClassCastException var4) {
               throw SQLError.createSQLException(
                  Messages.getString("Common.UnableToUnwrap", new Object[]{iface.toString()}), "S1009", CallableStatement.this.getExceptionInterceptor()
               );
            }
         } catch (CJException var5) {
            throw SQLExceptionsMapping.translateException(var5);
         }
      }
   }
}
