package io.netty.handler.codec.http;

import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.ValueConverter;
import io.netty.util.AsciiString;
import io.netty.util.HashingStrategy;
import io.netty.util.internal.StringUtil;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class CombinedHttpHeaders extends DefaultHttpHeaders {
   public CombinedHttpHeaders(boolean validate) {
      super(new CombinedHttpHeaders.CombinedHttpHeadersImpl(AsciiString.CASE_INSENSITIVE_HASHER, valueConverter(validate), nameValidator(validate)));
   }

   @Override
   public boolean containsValue(CharSequence name, CharSequence value, boolean ignoreCase) {
      return super.containsValue(name, StringUtil.trimOws(value), ignoreCase);
   }

   private static final class CombinedHttpHeadersImpl extends DefaultHeaders<CharSequence, CharSequence, CombinedHttpHeaders.CombinedHttpHeadersImpl> {
      private static final int VALUE_LENGTH_ESTIMATE = 10;
      private CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<Object> objectEscaper;
      private CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<CharSequence> charSequenceEscaper;

      private CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<Object> objectEscaper() {
         if (this.objectEscaper == null) {
            this.objectEscaper = new CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<Object>() {
               @Override
               public CharSequence escape(Object value) {
                  return StringUtil.escapeCsv((CharSequence)CombinedHttpHeadersImpl.this.valueConverter().convertObject(value), true);
               }
            };
         }

         return this.objectEscaper;
      }

      private CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<CharSequence> charSequenceEscaper() {
         if (this.charSequenceEscaper == null) {
            this.charSequenceEscaper = new CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<CharSequence>() {
               public CharSequence escape(CharSequence value) {
                  return StringUtil.escapeCsv(value, true);
               }
            };
         }

         return this.charSequenceEscaper;
      }

      CombinedHttpHeadersImpl(
         HashingStrategy<CharSequence> nameHashingStrategy,
         ValueConverter<CharSequence> valueConverter,
         DefaultHeaders.NameValidator<CharSequence> nameValidator
      ) {
         super(nameHashingStrategy, valueConverter, nameValidator);
      }

      public Iterator<CharSequence> valueIterator(CharSequence name) {
         Iterator<CharSequence> itr = super.valueIterator(name);
         if (itr.hasNext() && !cannotBeCombined(name)) {
            Iterator<CharSequence> unescapedItr = StringUtil.unescapeCsvFields((CharSequence)itr.next()).iterator();
            if (itr.hasNext()) {
               throw new IllegalStateException("CombinedHttpHeaders should only have one value");
            } else {
               return unescapedItr;
            }
         } else {
            return itr;
         }
      }

      public List<CharSequence> getAll(CharSequence name) {
         List<CharSequence> values = super.getAll(name);
         if (!values.isEmpty() && !cannotBeCombined(name)) {
            if (values.size() != 1) {
               throw new IllegalStateException("CombinedHttpHeaders should only have one value");
            } else {
               return StringUtil.unescapeCsvFields((CharSequence)values.get(0));
            }
         } else {
            return values;
         }
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl add(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
         if (headers == this) {
            throw new IllegalArgumentException("can't add to itself.");
         } else {
            if (headers instanceof CombinedHttpHeaders.CombinedHttpHeadersImpl) {
               if (this.isEmpty()) {
                  this.addImpl(headers);
               } else {
                  for(Entry<? extends CharSequence, ? extends CharSequence> header : headers) {
                     this.addEscapedValue((CharSequence)header.getKey(), (CharSequence)header.getValue());
                  }
               }
            } else {
               for(Entry<? extends CharSequence, ? extends CharSequence> header : headers) {
                  this.add((CharSequence)header.getKey(), (CharSequence)header.getValue());
               }
            }

            return this;
         }
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl set(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
         if (headers == this) {
            return this;
         } else {
            this.clear();
            return this.add(headers);
         }
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl setAll(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
         if (headers == this) {
            return this;
         } else {
            for(CharSequence key : headers.names()) {
               this.remove(key);
            }

            return this.add(headers);
         }
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl add(CharSequence name, CharSequence value) {
         return this.addEscapedValue(name, this.charSequenceEscaper().escape(value));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl add(CharSequence name, CharSequence... values) {
         return this.addEscapedValue(name, commaSeparate(this.charSequenceEscaper(), values));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl add(CharSequence name, Iterable<? extends CharSequence> values) {
         return this.addEscapedValue(name, commaSeparate(this.charSequenceEscaper(), values));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl addObject(CharSequence name, Object value) {
         return this.addEscapedValue(name, commaSeparate(this.objectEscaper(), value));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl addObject(CharSequence name, Iterable<?> values) {
         return this.addEscapedValue(name, commaSeparate(this.objectEscaper(), values));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl addObject(CharSequence name, Object... values) {
         return this.addEscapedValue(name, commaSeparate(this.objectEscaper(), values));
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl set(CharSequence name, CharSequence... values) {
         super.set(name, commaSeparate(this.charSequenceEscaper(), values));
         return this;
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl set(CharSequence name, Iterable<? extends CharSequence> values) {
         super.set(name, commaSeparate(this.charSequenceEscaper(), values));
         return this;
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl setObject(CharSequence name, Object value) {
         super.set(name, commaSeparate(this.objectEscaper(), value));
         return this;
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl setObject(CharSequence name, Object... values) {
         super.set(name, commaSeparate(this.objectEscaper(), values));
         return this;
      }

      public CombinedHttpHeaders.CombinedHttpHeadersImpl setObject(CharSequence name, Iterable<?> values) {
         super.set(name, commaSeparate(this.objectEscaper(), values));
         return this;
      }

      private static boolean cannotBeCombined(CharSequence name) {
         return HttpHeaderNames.SET_COOKIE.contentEqualsIgnoreCase(name);
      }

      private CombinedHttpHeaders.CombinedHttpHeadersImpl addEscapedValue(CharSequence name, CharSequence escapedValue) {
         CharSequence currentValue = (CharSequence)super.get(name);
         if (currentValue != null && !cannotBeCombined(name)) {
            super.set(name, commaSeparateEscapedValues(currentValue, escapedValue));
         } else {
            super.add(name, escapedValue);
         }

         return this;
      }

      private static <T> CharSequence commaSeparate(CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<T> escaper, T... values) {
         StringBuilder sb = new StringBuilder(values.length * 10);
         if (values.length > 0) {
            int end = values.length - 1;

            for(int i = 0; i < end; ++i) {
               sb.append(escaper.escape(values[i])).append(',');
            }

            sb.append(escaper.escape(values[end]));
         }

         return sb;
      }

      private static <T> CharSequence commaSeparate(CombinedHttpHeaders.CombinedHttpHeadersImpl.CsvValueEscaper<T> escaper, Iterable<? extends T> values) {
         StringBuilder sb = values instanceof Collection ? new StringBuilder(((Collection)values).size() * 10) : new StringBuilder();
         Iterator<? extends T> iterator = values.iterator();
         if (iterator.hasNext()) {
            T next;
            for(next = (T)iterator.next(); iterator.hasNext(); next = (T)iterator.next()) {
               sb.append(escaper.escape(next)).append(',');
            }

            sb.append(escaper.escape(next));
         }

         return sb;
      }

      private static CharSequence commaSeparateEscapedValues(CharSequence currentValue, CharSequence value) {
         return new StringBuilder(currentValue.length() + 1 + value.length()).append(currentValue).append(',').append(value);
      }

      private interface CsvValueEscaper<T> {
         CharSequence escape(T var1);
      }
   }
}
