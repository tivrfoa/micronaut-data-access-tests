package io.netty.handler.codec.http;

import io.netty.handler.codec.CharSequenceValueConverter;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.DefaultHeadersImpl;
import io.netty.handler.codec.HeadersUtils;
import io.netty.handler.codec.ValueConverter;
import io.netty.util.AsciiString;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class DefaultHttpHeaders extends HttpHeaders {
   private static final int HIGHEST_INVALID_VALUE_CHAR_MASK = -16;
   private static final ByteProcessor HEADER_NAME_VALIDATOR = new ByteProcessor() {
      @Override
      public boolean process(byte value) throws Exception {
         DefaultHttpHeaders.validateHeaderNameElement(value);
         return true;
      }
   };
   static final DefaultHeaders.NameValidator<CharSequence> HttpNameValidator = new DefaultHeaders.NameValidator<CharSequence>() {
      public void validateName(CharSequence name) {
         if (name != null && name.length() != 0) {
            if (name instanceof AsciiString) {
               try {
                  ((AsciiString)name).forEachByte(DefaultHttpHeaders.HEADER_NAME_VALIDATOR);
               } catch (Exception var3) {
                  PlatformDependent.throwException(var3);
               }
            } else {
               for(int index = 0; index < name.length(); ++index) {
                  DefaultHttpHeaders.validateHeaderNameElement(name.charAt(index));
               }
            }

         } else {
            throw new IllegalArgumentException("empty headers are not allowed [" + name + "]");
         }
      }
   };
   private final DefaultHeaders<CharSequence, CharSequence, ?> headers;

   public DefaultHttpHeaders() {
      this(true);
   }

   public DefaultHttpHeaders(boolean validate) {
      this(validate, nameValidator(validate));
   }

   protected DefaultHttpHeaders(boolean validate, DefaultHeaders.NameValidator<CharSequence> nameValidator) {
      this(new DefaultHeadersImpl(AsciiString.CASE_INSENSITIVE_HASHER, valueConverter(validate), nameValidator));
   }

   protected DefaultHttpHeaders(DefaultHeaders<CharSequence, CharSequence, ?> headers) {
      this.headers = headers;
   }

   @Override
   public HttpHeaders add(HttpHeaders headers) {
      if (headers instanceof DefaultHttpHeaders) {
         this.headers.add(((DefaultHttpHeaders)headers).headers);
         return this;
      } else {
         return super.add(headers);
      }
   }

   @Override
   public HttpHeaders set(HttpHeaders headers) {
      if (headers instanceof DefaultHttpHeaders) {
         this.headers.set(((DefaultHttpHeaders)headers).headers);
         return this;
      } else {
         return super.set(headers);
      }
   }

   @Override
   public HttpHeaders add(String name, Object value) {
      this.headers.addObject(name, value);
      return this;
   }

   @Override
   public HttpHeaders add(CharSequence name, Object value) {
      this.headers.addObject(name, value);
      return this;
   }

   @Override
   public HttpHeaders add(String name, Iterable<?> values) {
      this.headers.addObject(name, values);
      return this;
   }

   @Override
   public HttpHeaders add(CharSequence name, Iterable<?> values) {
      this.headers.addObject(name, values);
      return this;
   }

   @Override
   public HttpHeaders addInt(CharSequence name, int value) {
      this.headers.addInt(name, value);
      return this;
   }

   @Override
   public HttpHeaders addShort(CharSequence name, short value) {
      this.headers.addShort(name, value);
      return this;
   }

   @Override
   public HttpHeaders remove(String name) {
      this.headers.remove(name);
      return this;
   }

   @Override
   public HttpHeaders remove(CharSequence name) {
      this.headers.remove(name);
      return this;
   }

   @Override
   public HttpHeaders set(String name, Object value) {
      this.headers.setObject(name, value);
      return this;
   }

   @Override
   public HttpHeaders set(CharSequence name, Object value) {
      this.headers.setObject(name, value);
      return this;
   }

   @Override
   public HttpHeaders set(String name, Iterable<?> values) {
      this.headers.setObject(name, values);
      return this;
   }

   @Override
   public HttpHeaders set(CharSequence name, Iterable<?> values) {
      this.headers.setObject(name, values);
      return this;
   }

   @Override
   public HttpHeaders setInt(CharSequence name, int value) {
      this.headers.setInt(name, value);
      return this;
   }

   @Override
   public HttpHeaders setShort(CharSequence name, short value) {
      this.headers.setShort(name, value);
      return this;
   }

   @Override
   public HttpHeaders clear() {
      this.headers.clear();
      return this;
   }

   @Override
   public String get(String name) {
      return this.get((CharSequence)name);
   }

   @Override
   public String get(CharSequence name) {
      return HeadersUtils.getAsString(this.headers, name);
   }

   @Override
   public Integer getInt(CharSequence name) {
      return this.headers.getInt(name);
   }

   @Override
   public int getInt(CharSequence name, int defaultValue) {
      return this.headers.getInt(name, defaultValue);
   }

   @Override
   public Short getShort(CharSequence name) {
      return this.headers.getShort(name);
   }

   @Override
   public short getShort(CharSequence name, short defaultValue) {
      return this.headers.getShort(name, defaultValue);
   }

   @Override
   public Long getTimeMillis(CharSequence name) {
      return this.headers.getTimeMillis(name);
   }

   @Override
   public long getTimeMillis(CharSequence name, long defaultValue) {
      return this.headers.getTimeMillis(name, defaultValue);
   }

   @Override
   public List<String> getAll(String name) {
      return this.getAll((CharSequence)name);
   }

   @Override
   public List<String> getAll(CharSequence name) {
      return HeadersUtils.getAllAsString(this.headers, name);
   }

   @Override
   public List<Entry<String, String>> entries() {
      if (this.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<Entry<String, String>> entriesConverted = new ArrayList(this.headers.size());

         for(Entry<String, String> entry : this) {
            entriesConverted.add(entry);
         }

         return entriesConverted;
      }
   }

   @Deprecated
   @Override
   public Iterator<Entry<String, String>> iterator() {
      return HeadersUtils.iteratorAsString(this.headers);
   }

   @Override
   public Iterator<Entry<CharSequence, CharSequence>> iteratorCharSequence() {
      return this.headers.iterator();
   }

   @Override
   public Iterator<String> valueStringIterator(CharSequence name) {
      final Iterator<CharSequence> itr = this.valueCharSequenceIterator(name);
      return new Iterator<String>() {
         public boolean hasNext() {
            return itr.hasNext();
         }

         public String next() {
            return ((CharSequence)itr.next()).toString();
         }

         public void remove() {
            itr.remove();
         }
      };
   }

   @Override
   public Iterator<CharSequence> valueCharSequenceIterator(CharSequence name) {
      return this.headers.valueIterator(name);
   }

   @Override
   public boolean contains(String name) {
      return this.contains((CharSequence)name);
   }

   @Override
   public boolean contains(CharSequence name) {
      return this.headers.contains(name);
   }

   @Override
   public boolean isEmpty() {
      return this.headers.isEmpty();
   }

   @Override
   public int size() {
      return this.headers.size();
   }

   @Override
   public boolean contains(String name, String value, boolean ignoreCase) {
      return this.contains((CharSequence)name, (CharSequence)value, ignoreCase);
   }

   @Override
   public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
      return this.headers.contains(name, value, ignoreCase ? AsciiString.CASE_INSENSITIVE_HASHER : AsciiString.CASE_SENSITIVE_HASHER);
   }

   @Override
   public Set<String> names() {
      return HeadersUtils.namesAsString(this.headers);
   }

   public boolean equals(Object o) {
      return o instanceof DefaultHttpHeaders && this.headers.equals(((DefaultHttpHeaders)o).headers, AsciiString.CASE_SENSITIVE_HASHER);
   }

   public int hashCode() {
      return this.headers.hashCode(AsciiString.CASE_SENSITIVE_HASHER);
   }

   @Override
   public HttpHeaders copy() {
      return new DefaultHttpHeaders(this.headers.copy());
   }

   private static void validateHeaderNameElement(byte value) {
      switch(value) {
         case 0:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 44:
         case 58:
         case 59:
         case 61:
            throw new IllegalArgumentException("a header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + value);
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 60:
         default:
            if (value < 0) {
               throw new IllegalArgumentException("a header name cannot contain non-ASCII character: " + value);
            }
      }
   }

   private static void validateHeaderNameElement(char value) {
      switch(value) {
         case '\u0000':
         case '\t':
         case '\n':
         case '\u000b':
         case '\f':
         case '\r':
         case '\u001c':
         case '\u001d':
         case '\u001e':
         case '\u001f':
         case ' ':
         case ',':
         case ':':
         case ';':
         case '=':
            throw new IllegalArgumentException("a header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + value);
         case '\u0001':
         case '\u0002':
         case '\u0003':
         case '\u0004':
         case '\u0005':
         case '\u0006':
         case '\u0007':
         case '\b':
         case '\u000e':
         case '\u000f':
         case '\u0010':
         case '\u0011':
         case '\u0012':
         case '\u0013':
         case '\u0014':
         case '\u0015':
         case '\u0016':
         case '\u0017':
         case '\u0018':
         case '\u0019':
         case '\u001a':
         case '\u001b':
         case '!':
         case '"':
         case '#':
         case '$':
         case '%':
         case '&':
         case '\'':
         case '(':
         case ')':
         case '*':
         case '+':
         case '-':
         case '.':
         case '/':
         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
         case '<':
         default:
            if (value > 127) {
               throw new IllegalArgumentException("a header name cannot contain non-ASCII character: " + value);
            }
      }
   }

   static ValueConverter<CharSequence> valueConverter(boolean validate) {
      return (ValueConverter<CharSequence>)(validate
         ? DefaultHttpHeaders.HeaderValueConverterAndValidator.INSTANCE
         : DefaultHttpHeaders.HeaderValueConverter.INSTANCE);
   }

   static DefaultHeaders.NameValidator<CharSequence> nameValidator(boolean validate) {
      return validate ? HttpNameValidator : DefaultHeaders.NameValidator.NOT_NULL;
   }

   private static class HeaderValueConverter extends CharSequenceValueConverter {
      static final DefaultHttpHeaders.HeaderValueConverter INSTANCE = new DefaultHttpHeaders.HeaderValueConverter();

      private HeaderValueConverter() {
      }

      @Override
      public CharSequence convertObject(Object value) {
         if (value instanceof CharSequence) {
            return (CharSequence)value;
         } else if (value instanceof Date) {
            return DateFormatter.format((Date)value);
         } else {
            return value instanceof Calendar ? DateFormatter.format(((Calendar)value).getTime()) : value.toString();
         }
      }
   }

   private static final class HeaderValueConverterAndValidator extends DefaultHttpHeaders.HeaderValueConverter {
      static final DefaultHttpHeaders.HeaderValueConverterAndValidator INSTANCE = new DefaultHttpHeaders.HeaderValueConverterAndValidator();

      @Override
      public CharSequence convertObject(Object value) {
         CharSequence seq = super.convertObject(value);
         int state = 0;

         for(int index = 0; index < seq.length(); ++index) {
            state = validateValueChar(seq, state, seq.charAt(index));
         }

         if (state != 0) {
            throw new IllegalArgumentException("a header value must not end with '\\r' or '\\n':" + seq);
         } else {
            return seq;
         }
      }

      private static int validateValueChar(CharSequence seq, int state, char character) {
         if ((character & -16) == 0) {
            switch(character) {
               case '\u0000':
                  throw new IllegalArgumentException("a header value contains a prohibited character '\u0000': " + seq);
               case '\u000b':
                  throw new IllegalArgumentException("a header value contains a prohibited character '\\v': " + seq);
               case '\f':
                  throw new IllegalArgumentException("a header value contains a prohibited character '\\f': " + seq);
            }
         }

         switch(state) {
            case 0:
               switch(character) {
                  case '\n':
                     return 2;
                  case '\r':
                     return 1;
               }
            default:
               return state;
            case 1:
               if (character == '\n') {
                  return 2;
               }

               throw new IllegalArgumentException("only '\\n' is allowed after '\\r': " + seq);
            case 2:
               switch(character) {
                  case '\t':
                  case ' ':
                     return 0;
                  default:
                     throw new IllegalArgumentException("only ' ' and '\\t' are allowed after '\\n': " + seq);
               }
         }
      }
   }
}
