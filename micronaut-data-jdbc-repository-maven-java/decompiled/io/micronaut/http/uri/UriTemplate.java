package io.micronaut.http.uri;

import io.micronaut.core.beans.BeanMap;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriTemplate implements Comparable<UriTemplate> {
   private static final String STRING_PATTERN_SCHEME = "([^:/?#]+):";
   private static final String STRING_PATTERN_USER_INFO = "([^@\\[/?#]*)";
   private static final String STRING_PATTERN_HOST_IPV4 = "[^\\[{/?#:]*";
   private static final String STRING_PATTERN_HOST_IPV6 = "\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]";
   private static final String STRING_PATTERN_HOST = "(\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]|[^\\[{/?#:]*)";
   private static final String STRING_PATTERN_PORT = "(\\d*(?:\\{[^/]+?\\})?)";
   private static final String STRING_PATTERN_PATH = "([^#]*)";
   private static final String STRING_PATTERN_QUERY = "([^#]*)";
   private static final String STRING_PATTERN_REMAINING = "(.*)";
   private static final char QUERY_OPERATOR = '?';
   private static final char SLASH_OPERATOR = '/';
   private static final char HASH_OPERATOR = '#';
   private static final char EXPAND_MODIFIER = '*';
   private static final char OPERATOR_NONE = '0';
   private static final char VAR_START = '{';
   private static final char VAR_END = '}';
   private static final char AND_OPERATOR = '&';
   private static final String SLASH_STRING = "/";
   private static final char DOT_OPERATOR = '.';
   static final Pattern PATTERN_SCHEME = Pattern.compile("^([^:/?#]+)://.*");
   static final Pattern PATTERN_FULL_PATH = Pattern.compile("^([^#\\?]*)(\\?([^#]*))?(\\#(.*))?$");
   static final Pattern PATTERN_FULL_URI = Pattern.compile(
      "^(([^:/?#]+):)?(//(([^@\\[/?#]*)@)?(\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]|[^\\[{/?#:]*)(:(\\d*(?:\\{[^/]+?\\})?))?)?([^#]*)(\\?([^#]*))?(#(.*))?"
   );
   protected final String templateString;
   final List<UriTemplate.PathSegment> segments = new ArrayList();

   public UriTemplate(CharSequence templateString) {
      this(templateString);
   }

   protected UriTemplate(CharSequence templateString, Object... parserArguments) {
      if (templateString == null) {
         throw new IllegalArgumentException("Argument [templateString] should not be null");
      } else {
         String templateAsString = templateString.toString();
         if (templateAsString.endsWith("/")) {
            int len = templateAsString.length();
            if (len > 1) {
               templateAsString = templateAsString.substring(0, len - 1);
            }
         }

         if (PATTERN_SCHEME.matcher(templateAsString).matches()) {
            Matcher matcher = PATTERN_FULL_URI.matcher(templateAsString);
            if (!matcher.find()) {
               throw new IllegalArgumentException("Invalid URI template: " + templateString);
            }

            this.templateString = templateAsString;
            String scheme = matcher.group(2);
            if (scheme != null) {
               this.createParser(scheme + "://", parserArguments).parse(this.segments);
            }

            String userInfo = matcher.group(5);
            String host = matcher.group(6);
            String port = matcher.group(8);
            String path = matcher.group(9);
            String query = matcher.group(11);
            String fragment = matcher.group(13);
            if (userInfo != null) {
               this.createParser(userInfo, parserArguments).parse(this.segments);
            }

            if (host != null) {
               this.createParser(host, parserArguments).parse(this.segments);
            }

            if (port != null) {
               this.createParser(':' + port, parserArguments).parse(this.segments);
            }

            if (path != null) {
               if (fragment != null) {
                  this.createParser(path + '#' + fragment).parse(this.segments);
               } else {
                  this.createParser(path, parserArguments).parse(this.segments);
               }
            }

            if (query != null) {
               this.createParser(query, parserArguments).parse(this.segments);
            }
         } else {
            this.templateString = templateAsString;
            this.createParser(this.templateString, parserArguments).parse(this.segments);
         }

      }
   }

   protected UriTemplate(String templateString, List<UriTemplate.PathSegment> segments) {
      this.templateString = templateString;
      this.segments.addAll(segments);
   }

   public long getVariableSegmentCount() {
      return this.segments.stream().filter(UriTemplate.PathSegment::isVariable).count();
   }

   public long getPathVariableSegmentCount() {
      return this.segments.stream().filter(UriTemplate.PathSegment::isVariable).filter(s -> !s.isQuerySegment()).count();
   }

   public long getRawSegmentCount() {
      return this.segments.stream().filter(segment -> !segment.isVariable()).count();
   }

   public int getRawSegmentLength() {
      return this.segments.stream().filter(segment -> !segment.isVariable()).map(CharSequence::length).reduce(Integer::sum).orElse(0);
   }

   public UriTemplate nest(CharSequence uriTemplate) {
      return this.nest(uriTemplate);
   }

   public String expand(Map<String, Object> parameters) {
      StringBuilder builder = new StringBuilder(this.templateString.length());
      boolean anyPreviousHasContent = false;
      boolean anyPreviousHasOperator = false;
      boolean queryParameter = false;

      for(UriTemplate.PathSegment segment : this.segments) {
         String result = segment.expand(parameters, anyPreviousHasContent, anyPreviousHasOperator);
         if (result != null) {
            if (segment instanceof UriTemplate.UriTemplateParser.VariablePathSegment) {
               UriTemplate.UriTemplateParser.VariablePathSegment varPathSegment = (UriTemplate.UriTemplateParser.VariablePathSegment)segment;
               if (varPathSegment.isQuerySegment && !queryParameter) {
                  queryParameter = true;
                  anyPreviousHasContent = false;
                  anyPreviousHasOperator = false;
               }

               char operator = varPathSegment.getOperator();
               if (operator != '0' && result.contains(String.valueOf(operator))) {
                  anyPreviousHasOperator = true;
               }

               anyPreviousHasContent = anyPreviousHasContent || result.length() > 0;
            }

            builder.append(result);
         }
      }

      return builder.toString();
   }

   public String expand(Object bean) {
      return this.expand(BeanMap.of(bean));
   }

   public String toString() {
      return this.toString(pathSegment -> true);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         UriTemplate that = (UriTemplate)o;
         return this.templateString.equals(that.templateString);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.templateString.hashCode();
   }

   public int compareTo(UriTemplate o) {
      if (this == o) {
         return 0;
      } else {
         Integer thisVariableCount = 0;
         Integer thatVariableCount = 0;
         Integer thisRawLength = 0;
         Integer thatRawLength = 0;

         for(UriTemplate.PathSegment segment : this.segments) {
            if (segment.isVariable()) {
               if (!segment.isQuerySegment()) {
                  thisVariableCount = thisVariableCount + 1;
               }
            } else {
               thisRawLength = thisRawLength + segment.length();
            }
         }

         for(UriTemplate.PathSegment segment : o.segments) {
            if (segment.isVariable()) {
               if (!segment.isQuerySegment()) {
                  thatVariableCount = thatVariableCount + 1;
               }
            } else {
               thatRawLength = thatRawLength + segment.length();
            }
         }

         int rawCompare = thatRawLength.compareTo(thisRawLength);
         return rawCompare == 0 ? thisVariableCount.compareTo(thatVariableCount) : rawCompare;
      }
   }

   public static UriTemplate of(String uri) {
      return new UriTemplate(uri);
   }

   protected UriTemplate nest(CharSequence uriTemplate, Object... parserArguments) {
      if (uriTemplate == null) {
         return this;
      } else {
         int len = uriTemplate.length();
         if (len == 0) {
            return this;
         } else {
            List<UriTemplate.PathSegment> newSegments = this.buildNestedSegments(uriTemplate, len, parserArguments);
            return this.newUriTemplate(uriTemplate, newSegments);
         }
      }
   }

   protected UriTemplate newUriTemplate(CharSequence uriTemplate, List<UriTemplate.PathSegment> newSegments) {
      return new UriTemplate(this.normalizeNested(this.templateString, uriTemplate), newSegments);
   }

   protected String normalizeNested(String uri, CharSequence nested) {
      if (StringUtils.isEmpty(nested)) {
         return uri;
      } else {
         String nestedStr = nested.toString();
         char firstNested = nestedStr.charAt(0);
         int len = nestedStr.length();
         if (len == 1 && firstNested == '/') {
            return uri;
         } else {
            switch(firstNested) {
               case '/':
                  if (uri.endsWith("/")) {
                     return uri + nestedStr.substring(1);
                  }

                  return uri + nestedStr;
               case '{':
                  if (len > 1) {
                     switch(nested.charAt(1)) {
                        case '#':
                        case '&':
                        case '/':
                        case '?':
                           if (uri.endsWith("/")) {
                              return uri.substring(0, uri.length() - 1) + nestedStr;
                           }

                           return uri + nestedStr;
                        default:
                           if (!uri.endsWith("/")) {
                              return uri + "/" + nestedStr;
                           }

                           return uri + nestedStr;
                     }
                  }

                  return uri;
               default:
                  return uri.endsWith("/") ? uri + nestedStr : uri + "/" + nestedStr;
            }
         }
      }
   }

   protected List<UriTemplate.PathSegment> buildNestedSegments(CharSequence uriTemplate, int len, Object... parserArguments) {
      List<UriTemplate.PathSegment> newSegments = new ArrayList();
      List<UriTemplate.PathSegment> querySegments = new ArrayList();

      for(UriTemplate.PathSegment segment : this.segments) {
         if (!segment.isQuerySegment()) {
            newSegments.add(segment);
         } else {
            querySegments.add(segment);
         }
      }

      String templateString = uriTemplate.toString();
      if (this.shouldPrependSlash(templateString, len)) {
         templateString = '/' + templateString;
      } else if (!this.segments.isEmpty() && templateString.startsWith("/")) {
         if (len == 1 && uriTemplate.charAt(0) == '/') {
            templateString = "";
         } else {
            UriTemplate.PathSegment last = (UriTemplate.PathSegment)this.segments.get(this.segments.size() - 1);
            if (last instanceof UriTemplate.UriTemplateParser.RawPathSegment) {
               String v = ((UriTemplate.UriTemplateParser.RawPathSegment)last).value;
               if (v.endsWith("/")) {
                  templateString = templateString.substring(1);
               } else {
                  templateString = this.normalizeNested("/", templateString.substring(1));
               }
            }
         }
      }

      this.createParser(templateString, parserArguments).parse(newSegments);
      newSegments.addAll(querySegments);
      return newSegments;
   }

   protected UriTemplate.UriTemplateParser createParser(String templateString, Object... parserArguments) {
      return new UriTemplate.UriTemplateParser(templateString);
   }

   protected String toString(Predicate<UriTemplate.PathSegment> filter) {
      StringBuilder builder = new StringBuilder(this.templateString.length());
      UriTemplate.UriTemplateParser.VariablePathSegment previousVariable = null;

      for(UriTemplate.PathSegment segment : this.segments) {
         if (filter.test(segment)) {
            boolean isVar = segment instanceof UriTemplate.UriTemplateParser.VariablePathSegment;
            if (previousVariable != null && isVar) {
               UriTemplate.UriTemplateParser.VariablePathSegment varSeg = (UriTemplate.UriTemplateParser.VariablePathSegment)segment;
               if (varSeg.operator == previousVariable.operator && varSeg.modifierChar != '*') {
                  builder.append(varSeg.delimiter);
               } else {
                  builder.append('}');
                  builder.append('{');
                  char op = varSeg.operator;
                  if ('0' != op) {
                     builder.append(op);
                  }
               }

               builder.append(segment.toString());
               previousVariable = varSeg;
            } else if (isVar) {
               previousVariable = (UriTemplate.UriTemplateParser.VariablePathSegment)segment;
               builder.append('{');
               char op = previousVariable.operator;
               if ('0' != op) {
                  builder.append(op);
               }

               builder.append(segment.toString());
            } else {
               if (previousVariable != null) {
                  builder.append('}');
                  previousVariable = null;
               }

               builder.append(segment.toString());
            }
         }
      }

      if (previousVariable != null) {
         builder.append('}');
      }

      return builder.toString();
   }

   private boolean shouldPrependSlash(String templateString, int len) {
      String parentString = this.templateString;
      int parentLen = parentString.length();
      return parentLen > 0 && parentString.charAt(parentLen - 1) != '/' && templateString.charAt(0) != '/' && this.isAdditionalPathVar(templateString, len);
   }

   private boolean isAdditionalPathVar(String templateString, int len) {
      if (len > 1) {
         boolean isVar = templateString.charAt(0) == '{';
         if (isVar) {
            switch(templateString.charAt(1)) {
               case '#':
               case '/':
               case '?':
                  return false;
               default:
                  return true;
            }
         }
      }

      return templateString.charAt(0) != '/';
   }

   protected interface PathSegment extends CharSequence {
      default boolean isQuerySegment() {
         return false;
      }

      default Optional<String> getVariable() {
         return Optional.empty();
      }

      default boolean isVariable() {
         return this.getVariable().isPresent();
      }

      String expand(Map<String, Object> parameters, boolean previousHasContent, boolean anyPreviousHasOperator);
   }

   protected static class UriTemplateParser {
      private static final int STATE_TEXT = 0;
      private static final int STATE_VAR_START = 1;
      private static final int STATE_VAR_CONTENT = 2;
      private static final int STATE_VAR_NEXT = 11;
      private static final int STATE_VAR_MODIFIER = 12;
      private static final int STATE_VAR_NEXT_MODIFIER = 13;
      String templateText;
      private int state = 0;
      private char operator = '0';
      private char modifier = '0';
      private String varDelimiter;
      private boolean isQuerySegment = false;

      UriTemplateParser(String templateText) {
         this.templateText = templateText;
      }

      protected void parse(List<UriTemplate.PathSegment> segments) {
         char[] chars = this.templateText.toCharArray();
         StringBuilder buff = new StringBuilder();
         StringBuilder modBuff = new StringBuilder();
         int varCount = 0;

         for(char c : chars) {
            switch(this.state) {
               case 0:
                  if (c == '{') {
                     if (buff.length() > 0) {
                        String val = buff.toString();
                        this.addRawContentSegment(segments, val, this.isQuerySegment);
                     }

                     buff.delete(0, buff.length());
                     this.state = 1;
                     break;
                  }

                  if (c == '?' || c == '#') {
                     this.isQuerySegment = true;
                  }

                  buff.append(c);
                  break;
               case 1:
                  switch(c) {
                     case ' ':
                        break;
                     case '#':
                     case '&':
                     case ';':
                     case '?':
                        this.isQuerySegment = true;
                     case '+':
                     case '.':
                     case '/':
                        this.operator = c;
                        this.state = 2;
                        break;
                     default:
                        this.state = 2;
                        buff.append(c);
                  }
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
               default:
                  break;
               case 12:
               case 13:
                  if (c == ' ') {
                     break;
                  }
               case 2:
               case 11:
                  switch(c) {
                     case '*':
                     case ':':
                        if (this.state != 12 && this.state != 13) {
                           this.modifier = c;
                           this.state = this.state == 11 ? 13 : 12;
                           continue;
                        }

                        modBuff.append(c);
                        continue;
                     case ',':
                        this.state = 11;
                     case '}':
                        break;
                     default:
                        switch(this.modifier) {
                           case '*':
                              throw new IllegalStateException("Expansion modifier * must be immediately followed by a closing brace '}'");
                           case ':':
                              modBuff.append(c);
                              continue;
                           default:
                              buff.append(c);
                              continue;
                        }
                  }

                  if (buff.length() > 0) {
                     String val = buff.toString();
                     String prefix;
                     String delimiter;
                     boolean encode;
                     boolean repeatPrefix;
                     switch(this.operator) {
                        case '#':
                           encode = false;
                           repeatPrefix = varCount < 1;
                           prefix = String.valueOf(this.operator);
                           delimiter = ",";
                           break;
                        case '&':
                        case '?':
                           encode = true;
                           repeatPrefix = true;
                           prefix = varCount < 1 ? this.operator + val + '=' : val + "=";
                           delimiter = this.modifier == '*' ? '&' + val + '=' : ",";
                           break;
                        case '+':
                           encode = false;
                           prefix = null;
                           delimiter = ",";
                           repeatPrefix = varCount < 1;
                           break;
                        case '.':
                        case '/':
                           encode = true;
                           repeatPrefix = varCount < 1;
                           prefix = String.valueOf(this.operator);
                           delimiter = this.modifier == '*' ? prefix : ",";
                           break;
                        case ';':
                           encode = true;
                           repeatPrefix = true;
                           prefix = this.operator + val + '=';
                           delimiter = this.modifier == '*' ? prefix : ",";
                           break;
                        default:
                           repeatPrefix = varCount < 1;
                           encode = true;
                           prefix = null;
                           delimiter = ",";
                     }

                     String modifierStr = modBuff.toString();
                     char modifierChar = this.modifier;
                     String previous = this.state != 11 && this.state != 13 ? null : this.varDelimiter;
                     this.addVariableSegment(
                        segments, val, prefix, delimiter, encode, repeatPrefix, modifierStr, modifierChar, this.operator, previous, this.isQuerySegment
                     );
                  }

                  boolean hasAnotherVar = this.state == 11 && c != '}';
                  if (hasAnotherVar) {
                     String delimiter;
                     switch(this.operator) {
                        case '&':
                        case '?':
                           delimiter = "&";
                           break;
                        case '.':
                        case '/':
                           delimiter = String.valueOf(this.operator);
                           break;
                        case ';':
                           delimiter = null;
                           break;
                        default:
                           delimiter = ",";
                     }

                     this.varDelimiter = delimiter;
                     ++varCount;
                  } else {
                     varCount = 0;
                  }

                  this.state = hasAnotherVar ? 11 : 0;
                  modBuff.delete(0, modBuff.length());
                  buff.delete(0, buff.length());
                  this.modifier = '0';
                  if (!hasAnotherVar) {
                     this.operator = '0';
                  }
            }
         }

         if (this.state == 0 && buff.length() > 0) {
            String val = buff.toString();
            this.addRawContentSegment(segments, val, this.isQuerySegment);
         }

      }

      protected void addRawContentSegment(List<UriTemplate.PathSegment> segments, String value, boolean isQuerySegment) {
         segments.add(new UriTemplate.UriTemplateParser.RawPathSegment(isQuerySegment, value));
      }

      protected void addVariableSegment(
         List<UriTemplate.PathSegment> segments,
         String variable,
         String prefix,
         String delimiter,
         boolean encode,
         boolean repeatPrefix,
         String modifierStr,
         char modifierChar,
         char operator,
         String previousDelimiter,
         boolean isQuerySegment
      ) {
         segments.add(
            new UriTemplate.UriTemplateParser.VariablePathSegment(
               isQuerySegment, variable, prefix, delimiter, encode, modifierChar, operator, modifierStr, previousDelimiter, repeatPrefix
            )
         );
      }

      private static class RawPathSegment implements UriTemplate.PathSegment {
         private final boolean isQuerySegment;
         private final String value;

         public RawPathSegment(boolean isQuerySegment, String value) {
            this.isQuerySegment = isQuerySegment;
            this.value = value;
         }

         @Override
         public boolean isQuerySegment() {
            return this.isQuerySegment;
         }

         @Override
         public String expand(Map<String, Object> parameters, boolean previousHasContent, boolean anyPreviousHasOperator) {
            return this.value;
         }

         public boolean equals(Object o) {
            if (this == o) {
               return true;
            } else if (o != null && this.getClass() == o.getClass()) {
               UriTemplate.UriTemplateParser.RawPathSegment that = (UriTemplate.UriTemplateParser.RawPathSegment)o;
               if (this.isQuerySegment != that.isQuerySegment) {
                  return false;
               } else {
                  return this.value != null ? this.value.equals(that.value) : that.value == null;
               }
            } else {
               return false;
            }
         }

         public int hashCode() {
            int result = this.isQuerySegment ? 1 : 0;
            return 31 * result + (this.value != null ? this.value.hashCode() : 0);
         }

         public int length() {
            return this.value.length();
         }

         public char charAt(int index) {
            return this.value.charAt(index);
         }

         public CharSequence subSequence(int start, int end) {
            return this.value.subSequence(start, end);
         }

         public String toString() {
            return this.value;
         }
      }

      private class VariablePathSegment implements UriTemplate.PathSegment {
         private final boolean isQuerySegment;
         private final String variable;
         private final String prefix;
         private final String delimiter;
         private final boolean encode;
         private final char modifierChar;
         private final char operator;
         private final String modifierStr;
         private final String previousDelimiter;
         private final boolean repeatPrefix;

         public VariablePathSegment(
            boolean isQuerySegment,
            String variable,
            String prefix,
            String delimiter,
            boolean encode,
            char modifierChar,
            char operator,
            String modifierStr,
            String previousDelimiter,
            boolean repeatPrefix
         ) {
            this.isQuerySegment = isQuerySegment;
            this.variable = variable;
            this.prefix = prefix;
            this.delimiter = delimiter;
            this.encode = encode;
            this.modifierChar = modifierChar;
            this.operator = operator;
            this.modifierStr = modifierStr;
            this.previousDelimiter = previousDelimiter;
            this.repeatPrefix = repeatPrefix;
         }

         @Override
         public Optional<String> getVariable() {
            return Optional.of(this.variable);
         }

         public char getOperator() {
            return this.operator;
         }

         @Override
         public boolean isQuerySegment() {
            return this.isQuerySegment;
         }

         public int length() {
            return this.toString().length();
         }

         public char charAt(int index) {
            return this.toString().charAt(index);
         }

         public CharSequence subSequence(int start, int end) {
            return this.toString().subSequence(start, end);
         }

         public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.variable);
            if (this.modifierChar != '0') {
               builder.append(this.modifierChar);
               if (null != this.modifierStr) {
                  builder.append(this.modifierStr);
               }
            }

            return builder.toString();
         }

         private String escape(String v) {
            return v.replace("%", "%25").replaceAll("\\s", "%20");
         }

         @Override
         public String expand(Map<String, Object> parameters, boolean previousHasContent, boolean anyPreviousHasOperator) {
            Object found = parameters.get(this.variable);
            boolean isOptional = found instanceof Optional;
            if (found != null && (!isOptional || ((Optional)found).isPresent())) {
               if (isOptional) {
                  found = ((Optional)found).get();
               }

               String prefixToUse = this.prefix;
               if (this.operator == '?' && !anyPreviousHasOperator && this.prefix != null && !this.prefix.startsWith(String.valueOf(this.operator))) {
                  prefixToUse = this.operator + this.prefix;
               }

               if (found.getClass().isArray()) {
                  found = Arrays.asList(found);
               }

               boolean isQuery = this.operator == '?';
               if (this.modifierChar == '*') {
                  found = this.expandPOJO(found);
               }

               String result;
               if (found instanceof Iterable) {
                  Iterable iter = (Iterable)found;
                  if (iter instanceof Collection && ((Collection)iter).isEmpty()) {
                     return "";
                  }

                  StringJoiner joiner = new StringJoiner(this.delimiter);

                  for(Object o : iter) {
                     if (o != null) {
                        String v = o.toString();
                        joiner.add(this.encode ? this.encode(v, isQuery) : this.escape(v));
                     }
                  }

                  result = joiner.toString();
               } else if (found instanceof Map) {
                  Map<Object, Object> map = (Map)found;
                  map.values().removeIf(Objects::isNull);
                  if (map.isEmpty()) {
                     return "";
                  }

                  StringJoiner joiner;
                  if (this.modifierChar == '*') {
                     switch(this.operator) {
                        case '&':
                        case '?':
                           prefixToUse = String.valueOf(anyPreviousHasOperator ? '&' : this.operator);
                           joiner = new StringJoiner(String.valueOf('&'));
                           break;
                        case ';':
                           prefixToUse = String.valueOf(this.operator);
                           joiner = new StringJoiner(String.valueOf(prefixToUse));
                           break;
                        default:
                           joiner = new StringJoiner(this.delimiter);
                     }
                  } else {
                     joiner = new StringJoiner(this.delimiter);
                  }

                  map.forEach((key, some) -> {
                     String ks = key.toString();

                     for(Object value : some instanceof Iterable ? (Iterable)some : Collections.singletonList(some)) {
                        if (value != null) {
                           String vs = value.toString();
                           String ek = this.encode ? this.encode(ks, isQuery) : this.escape(ks);
                           String ev = this.encode ? this.encode(vs, isQuery) : this.escape(vs);
                           if (this.modifierChar == '*') {
                              String finalValue = ek + '=' + ev;
                              joiner.add(finalValue);
                           } else {
                              joiner.add(ek);
                              joiner.add(ev);
                           }
                        }
                     }

                  });
                  result = joiner.toString();
               } else {
                  String str = found.toString();
                  str = this.applyModifier(this.modifierStr, this.modifierChar, str, str.length());
                  result = this.encode ? this.encode(str, isQuery) : this.escape(str);
               }

               int len = result.length();
               StringBuilder finalResult = new StringBuilder(previousHasContent && this.previousDelimiter != null ? this.previousDelimiter : "");
               if (len == 0) {
                  switch(this.operator) {
                     case '/':
                        break;
                     case ';':
                        if (prefixToUse != null && prefixToUse.endsWith("=")) {
                           finalResult.append(prefixToUse.substring(0, prefixToUse.length() - 1)).append(result);
                           break;
                        }
                     default:
                        if (prefixToUse != null) {
                           finalResult.append(prefixToUse).append(result);
                        } else {
                           finalResult.append(result);
                        }
                  }
               } else if (prefixToUse != null && this.repeatPrefix) {
                  finalResult.append(prefixToUse).append(result);
               } else {
                  finalResult.append(result);
               }

               return finalResult.toString();
            } else {
               switch(this.operator) {
                  case '/':
                     return null;
                  default:
                     return "";
               }
            }
         }

         private String applyModifier(String modifierStr, char modifierChar, String result, int len) {
            if (modifierChar == ':' && modifierStr.length() > 0 && Character.isDigit(modifierStr.charAt(0))) {
               try {
                  int subResult = Integer.parseInt(modifierStr.trim(), 10);
                  if (subResult < len) {
                     result = result.substring(0, subResult);
                  }
               } catch (NumberFormatException var6) {
                  result = ":" + modifierStr;
               }
            }

            return result;
         }

         private String encode(String str, boolean query) {
            try {
               String encoded = URLEncoder.encode(str, "UTF-8");
               return query ? encoded : encoded.replace("+", "%20");
            } catch (UnsupportedEncodingException var4) {
               throw new IllegalStateException("No available encoding", var4);
            }
         }

         private Object expandPOJO(Object found) {
            if (found instanceof Iterable || found instanceof Map) {
               return found;
            } else {
               return found != null && !ClassUtils.isJavaLangType(found.getClass()) ? BeanMap.of(found) : found;
            }
         }
      }
   }
}
