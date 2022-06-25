package io.micronaut.http.uri;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.ArgumentUtils;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Internal
final class QueryStringDecoder {
   private static final int DEFAULT_MAX_PARAMS = 1024;
   private final Charset charset;
   private final String uri;
   private final int maxParams;
   private int pathEndIdx;
   private String path;
   private Map<String, List<String>> params;

   QueryStringDecoder(String uri) {
      this(uri, StandardCharsets.UTF_8);
   }

   QueryStringDecoder(String uri, boolean hasPath) {
      this(uri, StandardCharsets.UTF_8, hasPath);
   }

   QueryStringDecoder(String uri, Charset charset) {
      this(uri, charset, true);
   }

   QueryStringDecoder(String uri, Charset charset, boolean hasPath) {
      this(uri, charset, hasPath, 1024);
   }

   QueryStringDecoder(String uri, Charset charset, boolean hasPath, int maxParams) {
      this.uri = (String)Objects.requireNonNull(uri, "uri");
      this.charset = (Charset)Objects.requireNonNull(charset, "charset");
      this.maxParams = Objects.requireNonNull(maxParams, "maxParams");
      this.pathEndIdx = hasPath ? -1 : 0;
   }

   QueryStringDecoder(URI uri) {
      this(uri, StandardCharsets.UTF_8);
   }

   QueryStringDecoder(URI uri, Charset charset) {
      this(uri, charset, 1024);
   }

   QueryStringDecoder(URI uri, Charset charset, int maxParams) {
      String rawPath = uri.getRawPath();
      if (rawPath == null) {
         rawPath = "";
      }

      String rawQuery = uri.getRawQuery();
      this.uri = rawQuery == null ? rawPath : rawPath + '?' + rawQuery;
      this.charset = (Charset)Objects.requireNonNull(charset, "charset");
      this.maxParams = ArgumentUtils.requirePositive("maxParams", maxParams);
      this.pathEndIdx = rawPath.length();
   }

   public String toString() {
      return this.uri();
   }

   public String uri() {
      return this.uri;
   }

   public String path() {
      if (this.path == null) {
         this.path = decodeComponent(this.uri, 0, this.pathEndIdx(), this.charset, true);
      }

      return this.path;
   }

   public Map<String, List<String>> parameters() {
      if (this.params == null) {
         this.params = decodeParams(this.uri, this.pathEndIdx(), this.charset, this.maxParams);
      }

      return this.params;
   }

   public String rawPath() {
      return this.uri.substring(0, this.pathEndIdx());
   }

   public String rawQuery() {
      int start = this.pathEndIdx() + 1;
      return start < this.uri.length() ? this.uri.substring(start) : "";
   }

   private int pathEndIdx() {
      if (this.pathEndIdx == -1) {
         this.pathEndIdx = findPathEndIndex(this.uri);
      }

      return this.pathEndIdx;
   }

   private static Map<String, List<String>> decodeParams(String s, int from, Charset charset, int paramsLimit) {
      int len = s.length();
      if (from >= len) {
         return Collections.emptyMap();
      } else {
         if (s.charAt(from) == '?') {
            ++from;
         }

         Map<String, List<String>> params = new LinkedHashMap();
         int nameStart = from;
         int valueStart = -1;

         int i;
         label40:
         for(i = from; i < len; ++i) {
            switch(s.charAt(i)) {
               case '#':
                  break label40;
               case '&':
               case ';':
                  if (addParam(s, nameStart, valueStart, i, params, charset) && --paramsLimit == 0) {
                     return params;
                  }

                  nameStart = i + 1;
                  break;
               case '=':
                  if (nameStart == i) {
                     nameStart = i + 1;
                  } else if (valueStart < nameStart) {
                     valueStart = i + 1;
                  }
            }
         }

         addParam(s, nameStart, valueStart, i, params, charset);
         return params;
      }
   }

   public static String decodeComponent(final String s) {
      return decodeComponent(s, StandardCharsets.UTF_8);
   }

   private static String decodeComponent(final String s, final Charset charset) {
      return s == null ? "" : decodeComponent(s, 0, s.length(), charset, false);
   }

   private static boolean addParam(String s, int nameStart, int valueStart, int valueEnd, Map<String, List<String>> params, Charset charset) {
      if (nameStart >= valueEnd) {
         return false;
      } else {
         if (valueStart <= nameStart) {
            valueStart = valueEnd + 1;
         }

         String name = decodeComponent(s, nameStart, valueStart - 1, charset, false);
         String value = decodeComponent(s, valueStart, valueEnd, charset, false);
         List<String> values = (List)params.get(name);
         if (values == null) {
            values = new ArrayList(1);
            params.put(name, values);
         }

         values.add(value);
         return true;
      }
   }

   private static String decodeComponent(String s, int from, int toExcluded, Charset charset, boolean isPath) {
      int len = toExcluded - from;
      if (len <= 0) {
         return "";
      } else {
         int firstEscaped = -1;

         for(int i = from; i < toExcluded; ++i) {
            char c = s.charAt(i);
            if (c == '%' || c == '+' && !isPath) {
               firstEscaped = i;
               break;
            }
         }

         if (firstEscaped == -1) {
            return s.substring(from, toExcluded);
         } else {
            CharsetDecoder decoder = charset.newDecoder();
            int decodedCapacity = (toExcluded - firstEscaped) / 3;
            ByteBuffer byteBuf = ByteBuffer.allocate(decodedCapacity);
            CharBuffer charBuf = CharBuffer.allocate(decodedCapacity);
            StringBuilder strBuf = new StringBuilder(len);
            strBuf.append(s, from, firstEscaped);

            label72:
            for(int i = firstEscaped; i < toExcluded; ++i) {
               char c = s.charAt(i);
               if (c != '%') {
                  strBuf.append(c == '+' && !isPath ? ' ' : c);
               } else {
                  byteBuf.clear();

                  while(i + 3 <= toExcluded) {
                     byteBuf.put(decodeHexByte(s, i + 1));
                     i += 3;
                     if (i >= toExcluded || s.charAt(i) != '%') {
                        --i;
                        byteBuf.flip();
                        charBuf.clear();
                        CoderResult result = decoder.reset().decode(byteBuf, charBuf, true);

                        try {
                           if (!result.isUnderflow()) {
                              result.throwException();
                           }

                           result = decoder.flush(charBuf);
                           if (!result.isUnderflow()) {
                              result.throwException();
                           }
                        } catch (CharacterCodingException var16) {
                           throw new IllegalStateException(var16);
                        }

                        strBuf.append(charBuf.flip());
                        continue label72;
                     }
                  }

                  throw new IllegalArgumentException("unterminated escape sequence at index " + i + " of: " + s);
               }
            }

            return strBuf.toString();
         }
      }
   }

   private static int findPathEndIndex(String uri) {
      int len = uri.length();

      for(int i = 0; i < len; ++i) {
         char c = uri.charAt(i);
         if (c == '?' || c == '#') {
            return i;
         }
      }

      return len;
   }

   private static byte decodeHexByte(CharSequence s, int pos) {
      int hi = decodeHexNibble(s.charAt(pos));
      int lo = decodeHexNibble(s.charAt(pos + 1));
      if (hi != -1 && lo != -1) {
         return (byte)((hi << 4) + lo);
      } else {
         throw new IllegalArgumentException(String.format("invalid hex byte '%s' at index %d of '%s'", s.subSequence(pos, pos + 2), pos, s));
      }
   }

   private static int decodeHexNibble(final char c) {
      if (c >= '0' && c <= '9') {
         return c - 48;
      } else if (c >= 'A' && c <= 'F') {
         return c - 55;
      } else {
         return c >= 97 && c <= 102 ? c - 87 : -1;
      }
   }
}
