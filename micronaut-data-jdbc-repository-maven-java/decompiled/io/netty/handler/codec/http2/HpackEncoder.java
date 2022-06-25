package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.MathUtil;
import java.util.Arrays;
import java.util.Map.Entry;

final class HpackEncoder {
   static final int NOT_FOUND = -1;
   static final int HUFF_CODE_THRESHOLD = 512;
   private final HpackEncoder.HeaderEntry[] headerFields;
   private final HpackEncoder.HeaderEntry head = new HpackEncoder.HeaderEntry(-1, AsciiString.EMPTY_STRING, AsciiString.EMPTY_STRING, Integer.MAX_VALUE, null);
   private final HpackHuffmanEncoder hpackHuffmanEncoder = new HpackHuffmanEncoder();
   private final byte hashMask;
   private final boolean ignoreMaxHeaderListSize;
   private final int huffCodeThreshold;
   private long size;
   private long maxHeaderTableSize;
   private long maxHeaderListSize;

   HpackEncoder() {
      this(false);
   }

   HpackEncoder(boolean ignoreMaxHeaderListSize) {
      this(ignoreMaxHeaderListSize, 16, 512);
   }

   HpackEncoder(boolean ignoreMaxHeaderListSize, int arraySizeHint, int huffCodeThreshold) {
      this.ignoreMaxHeaderListSize = ignoreMaxHeaderListSize;
      this.maxHeaderTableSize = 4096L;
      this.maxHeaderListSize = 4294967295L;
      this.headerFields = new HpackEncoder.HeaderEntry[MathUtil.findNextPositivePowerOfTwo(Math.max(2, Math.min(arraySizeHint, 128)))];
      this.hashMask = (byte)(this.headerFields.length - 1);
      this.head.before = this.head.after = this.head;
      this.huffCodeThreshold = huffCodeThreshold;
   }

   public void encodeHeaders(int streamId, ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector) throws Http2Exception {
      if (this.ignoreMaxHeaderListSize) {
         this.encodeHeadersIgnoreMaxHeaderListSize(out, headers, sensitivityDetector);
      } else {
         this.encodeHeadersEnforceMaxHeaderListSize(streamId, out, headers, sensitivityDetector);
      }

   }

   private void encodeHeadersEnforceMaxHeaderListSize(
      int streamId, ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector
   ) throws Http2Exception {
      long headerSize = 0L;

      for(Entry<CharSequence, CharSequence> header : headers) {
         CharSequence name = (CharSequence)header.getKey();
         CharSequence value = (CharSequence)header.getValue();
         headerSize += HpackHeaderField.sizeOf(name, value);
         if (headerSize > this.maxHeaderListSize) {
            Http2CodecUtil.headerListSizeExceeded(streamId, this.maxHeaderListSize, false);
         }
      }

      this.encodeHeadersIgnoreMaxHeaderListSize(out, headers, sensitivityDetector);
   }

   private void encodeHeadersIgnoreMaxHeaderListSize(ByteBuf out, Http2Headers headers, Http2HeadersEncoder.SensitivityDetector sensitivityDetector) throws Http2Exception {
      for(Entry<CharSequence, CharSequence> header : headers) {
         CharSequence name = (CharSequence)header.getKey();
         CharSequence value = (CharSequence)header.getValue();
         this.encodeHeader(out, name, value, sensitivityDetector.isSensitive(name, value), HpackHeaderField.sizeOf(name, value));
      }

   }

   private void encodeHeader(ByteBuf out, CharSequence name, CharSequence value, boolean sensitive, long headerSize) {
      if (sensitive) {
         int nameIndex = this.getNameIndex(name);
         this.encodeLiteral(out, name, value, HpackUtil.IndexType.NEVER, nameIndex);
      } else if (this.maxHeaderTableSize == 0L) {
         int staticTableIndex = HpackStaticTable.getIndexInsensitive(name, value);
         if (staticTableIndex == -1) {
            int nameIndex = HpackStaticTable.getIndex(name);
            this.encodeLiteral(out, name, value, HpackUtil.IndexType.NONE, nameIndex);
         } else {
            encodeInteger(out, 128, 7, staticTableIndex);
         }

      } else if (headerSize > this.maxHeaderTableSize) {
         int nameIndex = this.getNameIndex(name);
         this.encodeLiteral(out, name, value, HpackUtil.IndexType.NONE, nameIndex);
      } else {
         HpackEncoder.HeaderEntry headerField = this.getEntryInsensitive(name, value);
         if (headerField != null) {
            int index = this.getIndex(headerField.index) + HpackStaticTable.length;
            encodeInteger(out, 128, 7, index);
         } else {
            int staticTableIndex = HpackStaticTable.getIndexInsensitive(name, value);
            if (staticTableIndex != -1) {
               encodeInteger(out, 128, 7, staticTableIndex);
            } else {
               this.ensureCapacity(headerSize);
               this.encodeLiteral(out, name, value, HpackUtil.IndexType.INCREMENTAL, this.getNameIndex(name));
               this.add(name, value, headerSize);
            }
         }

      }
   }

   public void setMaxHeaderTableSize(ByteBuf out, long maxHeaderTableSize) throws Http2Exception {
      if (maxHeaderTableSize < 0L || maxHeaderTableSize > 4294967295L) {
         throw Http2Exception.connectionError(
            Http2Error.PROTOCOL_ERROR, "Header Table Size must be >= %d and <= %d but was %d", 0L, 4294967295L, maxHeaderTableSize
         );
      } else if (this.maxHeaderTableSize != maxHeaderTableSize) {
         this.maxHeaderTableSize = maxHeaderTableSize;
         this.ensureCapacity(0L);
         encodeInteger(out, 32, 5, maxHeaderTableSize);
      }
   }

   public long getMaxHeaderTableSize() {
      return this.maxHeaderTableSize;
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

   private static void encodeInteger(ByteBuf out, int mask, int n, int i) {
      encodeInteger(out, mask, n, (long)i);
   }

   private static void encodeInteger(ByteBuf out, int mask, int n, long i) {
      assert n >= 0 && n <= 8 : "N: " + n;

      int nbits = 255 >>> 8 - n;
      if (i < (long)nbits) {
         out.writeByte((int)((long)mask | i));
      } else {
         out.writeByte(mask | nbits);

         long length;
         for(length = i - (long)nbits; (length & -128L) != 0L; length >>>= 7) {
            out.writeByte((int)(length & 127L | 128L));
         }

         out.writeByte((int)length);
      }

   }

   private void encodeStringLiteral(ByteBuf out, CharSequence string) {
      int huffmanLength;
      if (string.length() >= this.huffCodeThreshold && (huffmanLength = this.hpackHuffmanEncoder.getEncodedLength(string)) < string.length()) {
         encodeInteger(out, 128, 7, huffmanLength);
         this.hpackHuffmanEncoder.encode(out, string);
      } else {
         encodeInteger(out, 0, 7, string.length());
         if (string instanceof AsciiString) {
            AsciiString asciiString = (AsciiString)string;
            out.writeBytes(asciiString.array(), asciiString.arrayOffset(), asciiString.length());
         } else {
            out.writeCharSequence(string, CharsetUtil.ISO_8859_1);
         }
      }

   }

   private void encodeLiteral(ByteBuf out, CharSequence name, CharSequence value, HpackUtil.IndexType indexType, int nameIndex) {
      boolean nameIndexValid = nameIndex != -1;
      switch(indexType) {
         case INCREMENTAL:
            encodeInteger(out, 64, 6, nameIndexValid ? nameIndex : 0);
            break;
         case NONE:
            encodeInteger(out, 0, 4, nameIndexValid ? nameIndex : 0);
            break;
         case NEVER:
            encodeInteger(out, 16, 4, nameIndexValid ? nameIndex : 0);
            break;
         default:
            throw new Error("should not reach here");
      }

      if (!nameIndexValid) {
         this.encodeStringLiteral(out, name);
      }

      this.encodeStringLiteral(out, value);
   }

   private int getNameIndex(CharSequence name) {
      int index = HpackStaticTable.getIndex(name);
      if (index == -1) {
         index = this.getIndex(name);
         if (index >= 0) {
            index += HpackStaticTable.length;
         }
      }

      return index;
   }

   private void ensureCapacity(long headerSize) {
      while(this.maxHeaderTableSize - this.size < headerSize) {
         int index = this.length();
         if (index != 0) {
            this.remove();
            continue;
         }
         break;
      }

   }

   int length() {
      return this.size == 0L ? 0 : this.head.after.index - this.head.before.index + 1;
   }

   long size() {
      return this.size;
   }

   HpackHeaderField getHeaderField(int index) {
      HpackEncoder.HeaderEntry entry = this.head;

      while(index-- >= 0) {
         entry = entry.before;
      }

      return entry;
   }

   private HpackEncoder.HeaderEntry getEntryInsensitive(CharSequence name, CharSequence value) {
      if (this.length() != 0 && name != null && value != null) {
         int h = AsciiString.hashCode(name);
         int i = this.index(h);

         for(HpackEncoder.HeaderEntry e = this.headerFields[i]; e != null; e = e.next) {
            if (e.hash == h && HpackUtil.equalsVariableTime(value, e.value) && HpackUtil.equalsVariableTime(name, e.name)) {
               return e;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private int getIndex(CharSequence name) {
      if (this.length() != 0 && name != null) {
         int h = AsciiString.hashCode(name);
         int i = this.index(h);

         for(HpackEncoder.HeaderEntry e = this.headerFields[i]; e != null; e = e.next) {
            if (e.hash == h && HpackUtil.equalsConstantTime(name, e.name) != 0) {
               return this.getIndex(e.index);
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private int getIndex(int index) {
      return index == -1 ? -1 : index - this.head.before.index + 1;
   }

   private void add(CharSequence name, CharSequence value, long headerSize) {
      if (headerSize > this.maxHeaderTableSize) {
         this.clear();
      } else {
         while(this.maxHeaderTableSize - this.size < headerSize) {
            this.remove();
         }

         int h = AsciiString.hashCode(name);
         int i = this.index(h);
         HpackEncoder.HeaderEntry old = this.headerFields[i];
         HpackEncoder.HeaderEntry e = new HpackEncoder.HeaderEntry(h, name, value, this.head.before.index - 1, old);
         this.headerFields[i] = e;
         e.addBefore(this.head);
         this.size += headerSize;
      }
   }

   private HpackHeaderField remove() {
      if (this.size == 0L) {
         return null;
      } else {
         HpackEncoder.HeaderEntry eldest = this.head.after;
         int h = eldest.hash;
         int i = this.index(h);
         HpackEncoder.HeaderEntry prev = this.headerFields[i];

         HpackEncoder.HeaderEntry next;
         for(HpackEncoder.HeaderEntry e = prev; e != null; e = next) {
            next = e.next;
            if (e == eldest) {
               if (prev == eldest) {
                  this.headerFields[i] = next;
               } else {
                  prev.next = next;
               }

               eldest.remove();
               this.size -= (long)eldest.size();
               return eldest;
            }

            prev = e;
         }

         return null;
      }
   }

   private void clear() {
      Arrays.fill(this.headerFields, null);
      this.head.before = this.head.after = this.head;
      this.size = 0L;
   }

   private int index(int h) {
      return h & this.hashMask;
   }

   private static final class HeaderEntry extends HpackHeaderField {
      HpackEncoder.HeaderEntry before;
      HpackEncoder.HeaderEntry after;
      HpackEncoder.HeaderEntry next;
      int hash;
      int index;

      HeaderEntry(int hash, CharSequence name, CharSequence value, int index, HpackEncoder.HeaderEntry next) {
         super(name, value);
         this.index = index;
         this.hash = hash;
         this.next = next;
      }

      private void remove() {
         this.before.after = this.after;
         this.after.before = this.before;
         this.before = null;
         this.after = null;
         this.next = null;
      }

      private void addBefore(HpackEncoder.HeaderEntry existingEntry) {
         this.after = existingEntry;
         this.before = existingEntry.before;
         this.before.after = this;
         this.after.before = this;
      }
   }
}
