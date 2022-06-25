package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import org.flywaydb.core.internal.sqlscript.Delimiter;

public class PeekingReader extends FilterReader {
   private int[] peekBuffer = new int[256];
   private int peekMax = 0;
   private int peekBufferOffset = 0;
   private final boolean supportsPeekingMultipleLines;

   PeekingReader(Reader in, boolean supportsPeekingMultipleLines) {
      super(in);
      this.supportsPeekingMultipleLines = supportsPeekingMultipleLines;
   }

   public int read() throws IOException {
      ++this.peekBufferOffset;
      return super.read();
   }

   public void swallow() throws IOException {
      this.read();
   }

   public void swallow(int n) throws IOException {
      for(int i = 0; i < n; ++i) {
         this.read();
      }

   }

   private int peek() throws IOException {
      if (this.peekBufferOffset >= this.peekMax) {
         this.refillPeekBuffer();
      }

      return this.peekBuffer[this.peekBufferOffset];
   }

   private void refillPeekBuffer() throws IOException {
      this.mark(this.peekBuffer.length);
      this.peekMax = this.peekBuffer.length;
      this.peekBufferOffset = 0;

      for(int i = 0; i < this.peekBuffer.length; ++i) {
         int read = super.read();
         this.peekBuffer[i] = read;
         if (!this.supportsPeekingMultipleLines && read == 10) {
            this.peekMax = i;
            break;
         }
      }

      this.reset();
   }

   public boolean peek(char c) throws IOException {
      int r = this.peek();
      return r != -1 && c == (char)r;
   }

   public boolean peek(char c1, char c2) throws IOException {
      int r = this.peek();
      return r != -1 && (c1 == (char)r || c2 == (char)r);
   }

   public boolean peekNumeric() throws IOException {
      int r = this.peek();
      return this.isNumeric(r);
   }

   private boolean isNumeric(int r) {
      return r != -1 && (char)r >= '0' && (char)r <= '9';
   }

   public boolean peekWhitespace() throws IOException {
      int r = this.peek();
      return this.isWhitespace(r);
   }

   private boolean isWhitespace(int r) {
      return r != -1 && Character.isWhitespace((char)r);
   }

   public boolean peekKeywordPart(ParserContext context) throws IOException {
      int r = this.peek();
      return this.isKeywordPart(r, context);
   }

   private boolean isKeywordPart(int r, ParserContext context) {
      return r != -1 && ((char)r == '_' || (char)r == '$' || Character.isLetterOrDigit((char)r) || context.isLetter((char)r));
   }

   public boolean peek(String str) throws IOException {
      return str.equals(this.peek(str.length()));
   }

   public String peek(int numChars) throws IOException {
      return this.peek(numChars, false);
   }

   public String peek(int numChars, boolean peekMultipleLines) throws IOException {
      if (numChars >= this.peekBuffer.length) {
         this.resizePeekBuffer(numChars);
      }

      if (this.peekBufferOffset + numChars >= this.peekMax) {
         this.refillPeekBuffer();
      }

      StringBuilder result = new StringBuilder();
      int prevR = -1;

      for(int i = 0; i < numChars; ++i) {
         int r = this.peekBuffer[this.peekBufferOffset + i];
         if (r == -1 || this.peekBufferOffset + i > this.peekMax || !peekMultipleLines && prevR == 10) {
            break;
         }

         result.append((char)r);
         prevR = r;
      }

      return result.length() == 0 ? null : result.toString();
   }

   private void resizePeekBuffer(int newSize) {
      this.peekBuffer = Arrays.copyOf(this.peekBuffer, newSize + this.peekBufferOffset);
   }

   public void swallowUntilExcluding(char delimiter1, char delimiter2) throws IOException {
      while(!this.peek(delimiter1, delimiter2)) {
         int r = this.read();
         if (r != -1) {
            continue;
         }
         break;
      }

   }

   public String readUntilExcluding(char delimiter1, char delimiter2) throws IOException {
      StringBuilder result = new StringBuilder();

      while(!this.peek(delimiter1, delimiter2)) {
         int r = this.read();
         if (r == -1) {
            break;
         }

         result.append((char)r);
      }

      return result.toString();
   }

   public void swallowUntilIncludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
      this.swallowUntilIncludingWithEscape(delimiter, selfEscape, '\u0000');
   }

   public void swallowUntilIncludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
      while(true) {
         int r = this.read();
         if (r != -1) {
            char c = (char)r;
            if (escape != 0 && c == escape) {
               this.swallow();
               continue;
            }

            if (c != delimiter) {
               continue;
            }

            if (selfEscape && this.peek(delimiter)) {
               this.swallow();
               continue;
            }
         }

         return;
      }
   }

   public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
      return this.readUntilExcludingWithEscape(delimiter, selfEscape, '\u0000');
   }

   public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
      StringBuilder result = new StringBuilder();

      while(true) {
         int r = this.read();
         if (r == -1) {
            break;
         }

         char c = (char)r;
         if (escape != 0 && c == escape) {
            int r2 = this.read();
            if (r2 == -1) {
               result.append(escape);
               break;
            }

            char c2 = (char)r2;
            result.append(c2);
         } else if (c == delimiter) {
            if (!selfEscape || !this.peek(delimiter)) {
               break;
            }

            result.append(delimiter);
            result.append(delimiter);
            this.read();
         } else {
            result.append(c);
         }
      }

      return result.toString();
   }

   public void swallowUntilExcluding(String str) throws IOException {
      while(!this.peek(str)) {
         int r = this.read();
         if (r != -1) {
            continue;
         }
         break;
      }

   }

   public String readUntilExcluding(String... strings) throws IOException {
      StringBuilder result = new StringBuilder();

      while(true) {
         for(String str : strings) {
            if (this.peek(str)) {
               return result.toString();
            }
         }

         int r = this.read();
         if (r == -1) {
            return result.toString();
         }

         result.append((char)r);
      }
   }

   public String readUntilIncluding(char delimiter) throws IOException {
      StringBuilder result = new StringBuilder();

      char c;
      do {
         int r = this.read();
         if (r == -1) {
            break;
         }

         c = (char)r;
         result.append(c);
      } while(c != delimiter);

      return result.toString();
   }

   public String readKeywordPart(Delimiter delimiter, ParserContext context) throws IOException {
      StringBuilder result = new StringBuilder();

      while(true) {
         boolean isDelimiter = delimiter != null && (result.length() == 0 || !delimiter.shouldBeAloneOnLine()) && this.peek(delimiter.getDelimiter());
         boolean shouldAppend = !isDelimiter && this.peekKeywordPart(context);
         if (!shouldAppend) {
            return result.toString();
         }

         result.append((char)this.read());
      }
   }

   public String readNumeric() throws IOException {
      StringBuilder result = new StringBuilder();

      while(this.peekNumeric()) {
         result.append((char)this.read());
      }

      return result.toString();
   }

   public String readWhitespace() throws IOException {
      StringBuilder result = new StringBuilder();

      while(this.peekWhitespace()) {
         result.append((char)this.read());
      }

      return result.toString();
   }
}
