package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Constants;
import com.mysql.cj.MessageBuilder;
import com.mysql.cj.Messages;
import com.mysql.cj.NativeSession;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.QueryAttributesBindings;
import com.mysql.cj.QueryBindings;
import com.mysql.cj.Session;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.util.StringUtils;
import java.util.List;

public class NativeMessageBuilder implements MessageBuilder<NativePacketPayload> {
   private boolean supportsQueryAttributes = true;

   public NativeMessageBuilder(boolean supportsQueryAttributes) {
      this.supportsQueryAttributes = supportsQueryAttributes;
   }

   public NativePacketPayload buildSqlStatement(String statement) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildSqlStatement(String statement, List<Object> args) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildClose() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, byte[] query) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(query.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
      if (this.supportsQueryAttributes) {
         packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 0L);
         packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
      }

      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, query);
      return packet;
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, String query) {
      return this.buildComQuery(sharedPacket, StringUtils.getBytes(query));
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, String query, String encoding) {
      return this.buildComQuery(sharedPacket, StringUtils.getBytes(query, encoding));
   }

   public NativePacketPayload buildComQuery(
      NativePacketPayload sharedPacket, Session sess, PreparedQuery preparedQuery, QueryBindings bindings, String characterEncoding
   ) {
      NativePacketPayload sendPacket = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      QueryAttributesBindings queryAttributesBindings = preparedQuery.getQueryAttributesBindings();
      synchronized(this) {
         BindValue[] bindValues = bindings.getBindValues();
         sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
         if (this.supportsQueryAttributes) {
            if (queryAttributesBindings.getCount() > 0) {
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, (long)queryAttributesBindings.getCount());
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
               byte[] nullBitsBuffer = new byte[(queryAttributesBindings.getCount() + 7) / 8];

               for(int i = 0; i < queryAttributesBindings.getCount(); ++i) {
                  if (queryAttributesBindings.getAttributeValue(i).isNull()) {
                     nullBitsBuffer[i >>> 3] = (byte)(nullBitsBuffer[i >>> 3] | 1 << (i & 7));
                  }
               }

               sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_VAR, nullBitsBuffer);
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
               queryAttributesBindings.runThroughAll(a -> {
                  sendPacket.writeInteger(NativeConstants.IntegerDataType.INT2, (long)a.getFieldType());
                  sendPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, a.getName().getBytes());
               });
               queryAttributesBindings.runThroughAll(a -> {
                  if (!a.isNull()) {
                     a.writeAsQueryAttribute(sendPacket);
                  }

               });
            } else {
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 0L);
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
            }
         } else if (queryAttributesBindings.getCount() > 0) {
            sess.getLog().logWarn(Messages.getString("QueryAttributes.SetButNotSupported"));
         }

         sendPacket.setTag("QUERY");
         boolean useStreamLengths = sess.getPropertySet().getBooleanProperty(PropertyKey.useStreamLengthsInPrepStmts).getValue();
         int ensurePacketSize = 0;
         String statementComment = ((NativeSession)sess).getProtocol().getQueryComment();
         byte[] commentAsBytes = null;
         if (statementComment != null) {
            commentAsBytes = StringUtils.getBytes(statementComment, characterEncoding);
            ensurePacketSize += commentAsBytes.length;
            ensurePacketSize += 6;
         }

         for(int i = 0; i < bindValues.length; ++i) {
            if (bindValues[i].isStream() && useStreamLengths) {
               ensurePacketSize = (int)((long)ensurePacketSize + bindValues[i].getScaleOrLength());
            }
         }

         if (ensurePacketSize != 0) {
            sendPacket.ensureCapacity(ensurePacketSize);
         }

         if (commentAsBytes != null) {
            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SLASH_STAR_SPACE_AS_BYTES);
            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, commentAsBytes);
            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
         }

         byte[][] staticSqlStrings = preparedQuery.getQueryInfo().getStaticSqlParts();

         for(int i = 0; i < bindValues.length; ++i) {
            bindings.checkParameterSet(i);
            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, staticSqlStrings[i]);
            bindValues[i].writeAsText(sendPacket);
         }

         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, staticSqlStrings[bindValues.length]);
         return sendPacket;
      }
   }

   public NativePacketPayload buildComInitDb(NativePacketPayload sharedPacket, byte[] dbName) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(dbName.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 2L);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, dbName);
      return packet;
   }

   public NativePacketPayload buildComInitDb(NativePacketPayload sharedPacket, String dbName) {
      return this.buildComInitDb(sharedPacket, StringUtils.getBytes(dbName));
   }

   public NativePacketPayload buildComShutdown(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 8L);
      return packet;
   }

   public NativePacketPayload buildComSetOption(NativePacketPayload sharedPacket, int val) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(3);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 27L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT2, (long)val);
      return packet;
   }

   public NativePacketPayload buildComPing(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 14L);
      return packet;
   }

   public NativePacketPayload buildComQuit(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
      return packet;
   }

   public NativePacketPayload buildComStmtPrepare(NativePacketPayload sharedPacket, byte[] query) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(query.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 22L);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, query);
      return packet;
   }

   public NativePacketPayload buildComStmtPrepare(NativePacketPayload sharedPacket, String queryString, String characterEncoding) {
      return this.buildComStmtPrepare(sharedPacket, StringUtils.getBytes(queryString, characterEncoding));
   }

   public NativePacketPayload buildComStmtClose(NativePacketPayload sharedPacket, long serverStatementId) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 25L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      return packet;
   }

   public NativePacketPayload buildComStmtReset(NativePacketPayload sharedPacket, long serverStatementId) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 26L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      return packet;
   }

   public NativePacketPayload buildComStmtFetch(NativePacketPayload sharedPacket, long serverStatementId, long numRowsToFetch) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 28L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, numRowsToFetch);
      return packet;
   }

   public NativePacketPayload buildComStmtSendLongData(NativePacketPayload sharedPacket, long serverStatementId, int parameterIndex, byte[] longData) {
      NativePacketPayload packet = this.buildComStmtSendLongDataHeader(sharedPacket, serverStatementId, parameterIndex);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, longData);
      return packet;
   }

   public NativePacketPayload buildComStmtSendLongDataHeader(NativePacketPayload sharedPacket, long serverStatementId, int parameterIndex) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 24L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT2, (long)parameterIndex);
      return packet;
   }

   public NativePacketPayload buildComStmtExecute(
      NativePacketPayload sharedPacket, long serverStatementId, byte flags, boolean sendQueryAttributes, PreparedQuery preparedQuery
   ) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      int parameterCount = preparedQuery.getParameterCount();
      QueryBindings queryBindings = preparedQuery.getQueryBindings();
      BindValue[] parameterBindings = queryBindings.getBindValues();
      QueryAttributesBindings queryAttributesBindings = preparedQuery.getQueryAttributesBindings();
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 23L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, (long)flags);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, 1L);
      int parametersAndAttributesCount = parameterCount;
      if (this.supportsQueryAttributes) {
         if (sendQueryAttributes) {
            parametersAndAttributesCount = parameterCount + queryAttributesBindings.getCount();
         }

         if (sendQueryAttributes || parametersAndAttributesCount > 0) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, (long)parametersAndAttributesCount);
         }
      }

      if (parametersAndAttributesCount > 0) {
         int nullCount = (parametersAndAttributesCount + 7) / 8;
         int nullBitsPosition = packet.getPosition();

         for(int i = 0; i < nullCount; ++i) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         }

         byte[] nullBitsBuffer = new byte[nullCount];
         if (queryBindings.getSendTypesToServer().get() || sendQueryAttributes && queryAttributesBindings.getCount() > 0) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);

            for(int i = 0; i < parameterCount; ++i) {
               packet.writeInteger(NativeConstants.IntegerDataType.INT2, (long)parameterBindings[i].getFieldType());
               if (this.supportsQueryAttributes) {
                  packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, "".getBytes());
               }
            }

            if (sendQueryAttributes) {
               queryAttributesBindings.runThroughAll(a -> {
                  packet.writeInteger(NativeConstants.IntegerDataType.INT2, (long)a.getFieldType());
                  packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, a.getName().getBytes());
               });
            }
         } else {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         }

         for(int i = 0; i < parameterCount; ++i) {
            if (!parameterBindings[i].isStream()) {
               if (!parameterBindings[i].isNull()) {
                  parameterBindings[i].writeAsBinary(packet);
               } else {
                  nullBitsBuffer[i >>> 3] = (byte)(nullBitsBuffer[i >>> 3] | 1 << (i & 7));
               }
            }
         }

         if (sendQueryAttributes) {
            for(int i = 0; i < queryAttributesBindings.getCount(); ++i) {
               if (queryAttributesBindings.getAttributeValue(i).isNull()) {
                  int b = i + parameterCount;
                  nullBitsBuffer[b >>> 3] = (byte)(nullBitsBuffer[b >>> 3] | 1 << (b & 7));
               }
            }

            queryAttributesBindings.runThroughAll(a -> {
               if (!a.isNull()) {
                  a.writeAsQueryAttribute(packet);
               }

            });
         }

         int endPosition = packet.getPosition();
         packet.setPosition(nullBitsPosition);
         packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, nullBitsBuffer);
         packet.setPosition(endPosition);
      }

      return packet;
   }
}
