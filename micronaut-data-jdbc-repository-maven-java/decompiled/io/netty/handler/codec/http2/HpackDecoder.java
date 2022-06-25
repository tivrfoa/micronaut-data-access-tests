package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;

final class HpackDecoder {
   private static final Http2Exception DECODE_ULE_128_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - decompression failure", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128(..)"
   );
   private static final Http2Exception DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - long overflow", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128(..)"
   );
   private static final Http2Exception DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - int overflow", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128ToInt(..)"
   );
   private static final Http2Exception DECODE_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decode(..)"
   );
   private static final Http2Exception INDEX_HEADER_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "indexHeader(..)"
   );
   private static final Http2Exception READ_NAME_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "readName(..)"
   );
   private static final Http2Exception INVALID_MAX_DYNAMIC_TABLE_SIZE = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR,
      "HPACK - invalid max dynamic table size",
      Http2Exception.ShutdownHint.HARD_SHUTDOWN,
      HpackDecoder.class,
      "setDynamicTableSize(..)"
   );
   private static final Http2Exception MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED = Http2Exception.newStatic(
      Http2Error.COMPRESSION_ERROR,
      "HPACK - max dynamic table size change required",
      Http2Exception.ShutdownHint.HARD_SHUTDOWN,
      HpackDecoder.class,
      "decode(..)"
   );
   private static final byte READ_HEADER_REPRESENTATION = 0;
   private static final byte READ_MAX_DYNAMIC_TABLE_SIZE = 1;
   private static final byte READ_INDEXED_HEADER = 2;
   private static final byte READ_INDEXED_HEADER_NAME = 3;
   private static final byte READ_LITERAL_HEADER_NAME_LENGTH_PREFIX = 4;
   private static final byte READ_LITERAL_HEADER_NAME_LENGTH = 5;
   private static final byte READ_LITERAL_HEADER_NAME = 6;
   private static final byte READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX = 7;
   private static final byte READ_LITERAL_HEADER_VALUE_LENGTH = 8;
   private static final byte READ_LITERAL_HEADER_VALUE = 9;
   private final HpackHuffmanDecoder huffmanDecoder = new HpackHuffmanDecoder();
   private final HpackDynamicTable hpackDynamicTable;
   private long maxHeaderListSize;
   private long maxDynamicTableSize;
   private long encoderMaxDynamicTableSize;
   private boolean maxDynamicTableSizeChangeRequired;

   HpackDecoder(long maxHeaderListSize) {
      this(maxHeaderListSize, 4096);
   }

   HpackDecoder(long maxHeaderListSize, int maxHeaderTableSize) {
      this.maxHeaderListSize = ObjectUtil.checkPositive(maxHeaderListSize, "maxHeaderListSize");
      this.maxDynamicTableSize = this.encoderMaxDynamicTableSize = (long)maxHeaderTableSize;
      this.maxDynamicTableSizeChangeRequired = false;
      this.hpackDynamicTable = new HpackDynamicTable((long)maxHeaderTableSize);
   }

   public void decode(int streamId, ByteBuf in, Http2Headers headers, boolean validateHeaders) throws Http2Exception {
      HpackDecoder.Http2HeadersSink sink = new HpackDecoder.Http2HeadersSink(streamId, headers, this.maxHeaderListSize, validateHeaders);
      this.decode(in, sink);
      sink.finish();
   }

   private void decode(ByteBuf in, HpackDecoder.Sink sink) throws Http2Exception {
      int index = 0;
      int nameLength = 0;
      int valueLength = 0;
      byte state = 0;
      boolean huffmanEncoded = false;
      CharSequence name = null;
      HpackUtil.IndexType indexType = HpackUtil.IndexType.NONE;

      while(in.isReadable()) {
         switch(state) {
            case 0:
               byte b = in.readByte();
               if (this.maxDynamicTableSizeChangeRequired && (b & 224) != 32) {
                  throw MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED;
               }

               if (b < 0) {
                  index = b & 127;
                  switch(index) {
                     case 0:
                        throw DECODE_ILLEGAL_INDEX_VALUE;
                     case 127:
                        state = 2;
                        continue;
                     default:
                        HpackHeaderField indexedHeaderx = this.getIndexedHeader(index);
                        sink.appendToHeaderList(indexedHeaderx.name, indexedHeaderx.value);
                  }
               } else if ((b & 64) == 64) {
                  indexType = HpackUtil.IndexType.INCREMENTAL;
                  index = b & 63;
                  switch(index) {
                     case 0:
                        state = 4;
                        continue;
                     case 63:
                        state = 3;
                        continue;
                     default:
                        name = this.readName(index);
                        nameLength = name.length();
                        state = 7;
                  }
               } else if ((b & 32) == 32) {
                  index = b & 31;
                  if (index == 31) {
                     state = 1;
                  } else {
                     this.setDynamicTableSize((long)index);
                     state = 0;
                  }
               } else {
                  indexType = (b & 16) == 16 ? HpackUtil.IndexType.NEVER : HpackUtil.IndexType.NONE;
                  index = b & 15;
                  switch(index) {
                     case 0:
                        state = 4;
                        continue;
                     case 15:
                        state = 3;
                        continue;
                     default:
                        name = this.readName(index);
                        nameLength = name.length();
                        state = 7;
                  }
               }
               break;
            case 1:
               this.setDynamicTableSize(decodeULE128(in, (long)index));
               state = 0;
               break;
            case 2:
               HpackHeaderField indexedHeader = this.getIndexedHeader(decodeULE128(in, index));
               sink.appendToHeaderList(indexedHeader.name, indexedHeader.value);
               state = 0;
               break;
            case 3:
               name = this.readName(decodeULE128(in, index));
               nameLength = name.length();
               state = 7;
               break;
            case 4:
               byte b = in.readByte();
               huffmanEncoded = (b & 128) == 128;
               index = b & 127;
               if (index == 127) {
                  state = 5;
               } else {
                  nameLength = index;
                  state = 6;
               }
               break;
            case 5:
               nameLength = decodeULE128(in, index);
               state = 6;
               break;
            case 6:
               if (in.readableBytes() < nameLength) {
                  throw notEnoughDataException(in);
               }

               name = this.readStringLiteral(in, nameLength, huffmanEncoded);
               state = 7;
               break;
            case 7:
               byte b = in.readByte();
               huffmanEncoded = (b & 128) == 128;
               index = b & 127;
               switch(index) {
                  case 0:
                     this.insertHeader(sink, name, AsciiString.EMPTY_STRING, indexType);
                     state = 0;
                     continue;
                  case 127:
                     state = 8;
                     continue;
                  default:
                     valueLength = index;
                     state = 9;
                     continue;
               }
            case 8:
               valueLength = decodeULE128(in, index);
               state = 9;
               break;
            case 9:
               if (in.readableBytes() < valueLength) {
                  throw notEnoughDataException(in);
               }

               CharSequence value = this.readStringLiteral(in, valueLength, huffmanEncoded);
               this.insertHeader(sink, name, value, indexType);
               state = 0;
               break;
            default:
               throw new Error("should not reach here state: " + state);
         }
      }

      if (state != 0) {
         throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "Incomplete header block fragment.");
      }
   }

   public void setMaxHeaderTableSize(long maxHeaderTableSize) throws Http2Exception {
      if (maxHeaderTableSize >= 0L && maxHeaderTableSize <= 4294967295L) {
         this.maxDynamicTableSize = maxHeaderTableSize;
         if (this.maxDynamicTableSize < this.encoderMaxDynamicTableSize) {
            this.maxDynamicTableSizeChangeRequired = true;
            this.hpackDynamicTable.setCapacity(this.maxDynamicTableSize);
         }

      } else {
         throw Http2Exception.connectionError(
            Http2Error.PROTOCOL_ERROR, "Header Table Size must be >= %d and <= %d but was %d", 0L, 4294967295L, maxHeaderTableSize
         );
      }
   }

   @Deprecated
   public void setMaxHeaderListSize(long maxHeaderListSize, long maxHeaderListSizeGoAway) throws Http2Exception {
      this.setMaxHeaderListSize(maxHeaderListSize);
   }

   public void setMaxHeaderListSize(long maxHeaderListSize) throws Http2Exception {
      if (maxHeaderListSize >= 0L && maxHeaderListSize <= 4294967295L) {
         this.maxHeaderListSize = maxHeaderListSize;
      } else {
         throw Http2Exception.connectionError(
            Http2Error.PROTOCOL_ERROR, "Header List Size must be >= %d and <= %d but was %d", 0L, 4294967295L, maxHeaderListSize
         );
      }
   }

   public long getMaxHeaderListSize() {
      return this.maxHeaderListSize;
   }

   public long getMaxHeaderTableSize() {
      return this.hpackDynamicTable.capacity();
   }

   int length() {
      return this.hpackDynamicTable.length();
   }

   long size() {
      return this.hpackDynamicTable.size();
   }

   HpackHeaderField getHeaderField(int index) {
      return this.hpackDynamicTable.getEntry(index + 1);
   }

   private void setDynamicTableSize(long dynamicTableSize) throws Http2Exception {
      if (dynamicTableSize > this.maxDynamicTableSize) {
         throw INVALID_MAX_DYNAMIC_TABLE_SIZE;
      } else {
         this.encoderMaxDynamicTableSize = dynamicTableSize;
         this.maxDynamicTableSizeChangeRequired = false;
         this.hpackDynamicTable.setCapacity(dynamicTableSize);
      }
   }

   private static HpackDecoder.HeaderType validate(int streamId, CharSequence name, HpackDecoder.HeaderType previousHeaderType, Http2Headers headers) throws Http2Exception {
      if (Http2Headers.PseudoHeaderName.hasPseudoHeaderFormat(name)) {
         if (previousHeaderType == HpackDecoder.HeaderType.REGULAR_HEADER) {
            throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Pseudo-header field '%s' found after regular header.", name);
         } else {
            Http2Headers.PseudoHeaderName pseudoHeader = Http2Headers.PseudoHeaderName.getPseudoHeader(name);
            if (pseudoHeader == null) {
               throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 pseudo-header '%s' encountered.", name);
            } else {
               HpackDecoder.HeaderType currentHeaderType = pseudoHeader.isRequestOnly()
                  ? HpackDecoder.HeaderType.REQUEST_PSEUDO_HEADER
                  : HpackDecoder.HeaderType.RESPONSE_PSEUDO_HEADER;
               if (previousHeaderType != null && currentHeaderType != previousHeaderType) {
                  throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Mix of request and response pseudo-headers.");
               } else if (contains(headers, name)) {
                  throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Duplicate HTTP/2 pseudo-header '%s' encountered.", name);
               } else {
                  return currentHeaderType;
               }
            }
         }
      } else {
         return HpackDecoder.HeaderType.REGULAR_HEADER;
      }
   }

   private static boolean contains(Http2Headers headers, CharSequence name) {
      if (headers == EmptyHttp2Headers.INSTANCE) {
         return false;
      } else if (!(headers instanceof DefaultHttp2Headers) && !(headers instanceof ReadOnlyHttp2Headers)) {
         if (Http2Headers.PseudoHeaderName.METHOD.value().equals(name)) {
            return headers.method() != null;
         } else if (Http2Headers.PseudoHeaderName.SCHEME.value().equals(name)) {
            return headers.scheme() != null;
         } else if (Http2Headers.PseudoHeaderName.AUTHORITY.value().equals(name)) {
            return headers.authority() != null;
         } else if (Http2Headers.PseudoHeaderName.PATH.value().equals(name)) {
            return headers.path() != null;
         } else if (Http2Headers.PseudoHeaderName.STATUS.value().equals(name)) {
            return headers.status() != null;
         } else {
            return false;
         }
      } else {
         return headers.contains(name);
      }
   }

   private CharSequence readName(int index) throws Http2Exception {
      if (index <= HpackStaticTable.length) {
         HpackHeaderField hpackHeaderField = HpackStaticTable.getEntry(index);
         return hpackHeaderField.name;
      } else if (index - HpackStaticTable.length <= this.hpackDynamicTable.length()) {
         HpackHeaderField hpackHeaderField = this.hpackDynamicTable.getEntry(index - HpackStaticTable.length);
         return hpackHeaderField.name;
      } else {
         throw READ_NAME_ILLEGAL_INDEX_VALUE;
      }
   }

   private HpackHeaderField getIndexedHeader(int index) throws Http2Exception {
      if (index <= HpackStaticTable.length) {
         return HpackStaticTable.getEntry(index);
      } else if (index - HpackStaticTable.length <= this.hpackDynamicTable.length()) {
         return this.hpackDynamicTable.getEntry(index - HpackStaticTable.length);
      } else {
         throw INDEX_HEADER_ILLEGAL_INDEX_VALUE;
      }
   }

   private void insertHeader(HpackDecoder.Sink sink, CharSequence name, CharSequence value, HpackUtil.IndexType indexType) {
      sink.appendToHeaderList(name, value);
      switch(indexType) {
         case INCREMENTAL:
            this.hpackDynamicTable.add(new HpackHeaderField(name, value));
         case NONE:
         case NEVER:
            return;
         default:
            throw new Error("should not reach here");
      }
   }

   private CharSequence readStringLiteral(ByteBuf in, int length, boolean huffmanEncoded) throws Http2Exception {
      if (huffmanEncoded) {
         return this.huffmanDecoder.decode(in, length);
      } else {
         byte[] buf = new byte[length];
         in.readBytes(buf);
         return new AsciiString(buf, false);
      }
   }

   private static IllegalArgumentException notEnoughDataException(ByteBuf in) {
      return new IllegalArgumentException("decode only works with an entire header block! " + in);
   }

   static int decodeULE128(ByteBuf in, int result) throws Http2Exception {
      int readerIndex = in.readerIndex();
      long v = decodeULE128(in, (long)result);
      if (v > 2147483647L) {
         in.readerIndex(readerIndex);
         throw DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION;
      } else {
         return (int)v;
      }
   }

   static long decodeULE128(ByteBuf in, long result) throws Http2Exception {
      assert result <= 127L && result >= 0L;

      boolean resultStartedAtZero = result == 0L;
      int writerIndex = in.writerIndex();
      int readerIndex = in.readerIndex();

      for(int shift = 0; readerIndex < writerIndex; shift += 7) {
         byte b = in.getByte(readerIndex);
         if (shift == 56 && ((b & 128) != 0 || b == 127 && !resultStartedAtZero)) {
            throw DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION;
         }

         if ((b & 128) == 0) {
            in.readerIndex(readerIndex + 1);
            return result + (((long)b & 127L) << shift);
         }

         result += ((long)b & 127L) << shift;
         ++readerIndex;
      }

      throw DECODE_ULE_128_DECOMPRESSION_EXCEPTION;
   }

   private static enum HeaderType {
      REGULAR_HEADER,
      REQUEST_PSEUDO_HEADER,
      RESPONSE_PSEUDO_HEADER;
   }

   private static final class Http2HeadersSink implements HpackDecoder.Sink {
      private final Http2Headers headers;
      private final long maxHeaderListSize;
      private final int streamId;
      private final boolean validate;
      private long headersLength;
      private boolean exceededMaxLength;
      private HpackDecoder.HeaderType previousType;
      private Http2Exception validationException;

      Http2HeadersSink(int streamId, Http2Headers headers, long maxHeaderListSize, boolean validate) {
         this.headers = headers;
         this.maxHeaderListSize = maxHeaderListSize;
         this.streamId = streamId;
         this.validate = validate;
      }

      @Override
      public void finish() throws Http2Exception {
         if (this.exceededMaxLength) {
            Http2CodecUtil.headerListSizeExceeded(this.streamId, this.maxHeaderListSize, true);
         } else if (this.validationException != null) {
            throw this.validationException;
         }

      }

      @Override
      public void appendToHeaderList(CharSequence name, CharSequence value) {
         this.headersLength += HpackHeaderField.sizeOf(name, value);
         this.exceededMaxLength |= this.headersLength > this.maxHeaderListSize;
         if (!this.exceededMaxLength && this.validationException == null) {
            if (this.validate) {
               try {
                  this.previousType = HpackDecoder.validate(this.streamId, name, this.previousType, this.headers);
               } catch (Http2Exception var4) {
                  this.validationException = var4;
                  return;
               }
            }

            this.headers.add(name, value);
         }
      }
   }

   private interface Sink {
      void appendToHeaderList(CharSequence var1, CharSequence var2);

      void finish() throws Http2Exception;
   }
}
