package io.netty.handler.codec.http;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryStringDecoder {
   private static final int DEFAULT_MAX_PARAMS = 1024;
   private final Charset charset;
   private final String uri;
   private final int maxParams;
   private final boolean semicolonIsNormalChar;
   private int pathEndIdx;
   private String path;
   private Map<String, List<String>> params;

   public QueryStringDecoder(String uri) {
      this(uri, HttpConstants.DEFAULT_CHARSET);
   }

   public QueryStringDecoder(String uri, boolean hasPath) {
      this(uri, HttpConstants.DEFAULT_CHARSET, hasPath);
   }

   public QueryStringDecoder(String uri, Charset charset) {
      this(uri, charset, true);
   }

   public QueryStringDecoder(String uri, Charset charset, boolean hasPath) {
      this(uri, charset, hasPath, 1024);
   }

   public QueryStringDecoder(String uri, Charset charset, boolean hasPath, int maxParams) {
      this(uri, charset, hasPath, maxParams, false);
   }

   public QueryStringDecoder(String uri, Charset charset, boolean hasPath, int maxParams, boolean semicolonIsNormalChar) {
      this.uri = ObjectUtil.checkNotNull(uri, "uri");
      this.charset = ObjectUtil.checkNotNull(charset, "charset");
      this.maxParams = ObjectUtil.checkPositive(maxParams, "maxParams");
      this.semicolonIsNormalChar = semicolonIsNormalChar;
      this.pathEndIdx = hasPath ? -1 : 0;
   }

   public QueryStringDecoder(URI uri) {
      this(uri, HttpConstants.DEFAULT_CHARSET);
   }

   public QueryStringDecoder(URI uri, Charset charset) {
      this(uri, charset, 1024);
   }

   public QueryStringDecoder(URI uri, Charset charset, int maxParams) {
      this(uri, charset, maxParams, false);
   }

   public QueryStringDecoder(URI uri, Charset charset, int maxParams, boolean semicolonIsNormalChar) {
      String rawPath = uri.getRawPath();
      if (rawPath == null) {
         rawPath = "";
      }

      String rawQuery = uri.getRawQuery();
      this.uri = rawQuery == null ? rawPath : rawPath + '?' + rawQuery;
      this.charset = ObjectUtil.checkNotNull(charset, "charset");
      this.maxParams = ObjectUtil.checkPositive(maxParams, "maxParams");
      this.semicolonIsNormalChar = semicolonIsNormalChar;
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
         this.params = decodeParams(this.uri, this.pathEndIdx(), this.charset, this.maxParams, this.semicolonIsNormalChar);
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

   private static Map<String, List<String>> decodeParams(String s, int from, Charset charset, int paramsLimit, boolean semicolonIsNormalChar) {
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
         label44:
         for(i = from; i < len; ++i) {
            switch(s.charAt(i)) {
               case '#':
                  break label44;
               case ';':
                  if (semicolonIsNormalChar) {
                     break;
                  }
               case '&':
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

   public static String decodeComponent(String s) {
      return decodeComponent(s, HttpConstants.DEFAULT_CHARSET);
   }

   public static String decodeComponent(String s, Charset charset) {
      return s == null ? "" : decodeComponent(s, 0, s.length(), charset, false);
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
            int decodedCapacity = (toExcluded - firstEscaped) / 3;
            byte[] buf = PlatformDependent.allocateUninitializedArray(decodedCapacity);
            StringBuilder strBuf = new StringBuilder(len);
            strBuf.append(s, from, firstEscaped);

            label55:
            for(int i = firstEscaped; i < toExcluded; ++i) {
               char c = s.charAt(i);
               if (c != '%') {
                  strBuf.append(c == '+' && !isPath ? ' ' : c);
               } else {
                  int bufIdx = 0;

                  while(i + 3 <= toExcluded) {
                     buf[bufIdx++] = StringUtil.decodeHexByte(s, i + 1);
                     i += 3;
                     if (i >= toExcluded || s.charAt(i) != '%') {
                        --i;
                        strBuf.append(new String(buf, 0, bufIdx, charset));
                        continue label55;
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
}
