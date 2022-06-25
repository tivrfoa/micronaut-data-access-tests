package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
   private static final String WHITESPACE_CHARS = " \t\n\f\r";

   public static String trimOrPad(String str, int length) {
      return trimOrPad(str, length, ' ');
   }

   public static String trimOrPad(String str, int length, char padChar) {
      StringBuilder result;
      if (str == null) {
         result = new StringBuilder();
      } else {
         result = new StringBuilder(str);
      }

      if (result.length() > length) {
         return result.substring(0, length);
      } else {
         while(result.length() < length) {
            result.append(padChar);
         }

         return result.toString();
      }
   }

   public static String trimOrLeftPad(String str, int length, char padChar) {
      if (str == null) {
         str = "";
      }

      return str.length() > length ? str.substring(0, length) : leftPad(str, length, padChar);
   }

   public static String leftPad(String original, int length, char padChar) {
      StringBuilder result = new StringBuilder(original);

      while(result.length() < length) {
         result.insert(0, padChar);
      }

      return result.toString();
   }

   public static String rightPad(String original, int length, char padChar) {
      StringBuilder result = new StringBuilder(original);

      while(result.length() < length) {
         result.append(padChar);
      }

      return result.toString();
   }

   public static String collapseWhitespace(String str) {
      StringBuilder result = new StringBuilder();
      char previous = 0;

      for(int i = 0; i < str.length(); ++i) {
         char c = str.charAt(i);
         if (isCharAnyOf(c, " \t\n\f\r")) {
            if (previous != ' ') {
               result.append(' ');
            }

            previous = ' ';
         } else {
            result.append(c);
            previous = c;
         }
      }

      return result.toString();
   }

   public static String left(String str, int count) {
      if (str == null) {
         return null;
      } else {
         return str.length() < count ? str : str.substring(0, count);
      }
   }

   public static String replaceAll(String str, String originalToken, String replacementToken) {
      return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
   }

   public static boolean hasLength(String str) {
      return str != null && str.length() > 0;
   }

   public static String arrayToCommaDelimitedString(Object[] strings) {
      return arrayToDelimitedString(",", strings);
   }

   public static String arrayToDelimitedString(String delimiter, Object[] strings) {
      if (strings == null) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();

         for(int i = 0; i < strings.length; ++i) {
            if (i > 0) {
               builder.append(delimiter);
            }

            builder.append(strings[i]);
         }

         return builder.toString();
      }
   }

   public static boolean hasText(String s) {
      return s != null && s.trim().length() > 0;
   }

   public static String[] tokenizeToStringArray(String str, String delimiters) {
      if (str == null) {
         return null;
      } else {
         Collection<String> tokens = tokenizeToStringCollection(str, delimiters);
         return (String[])tokens.toArray(new String[0]);
      }
   }

   public static List<String> tokenizeToStringCollection(String str, String delimiters) {
      if (str == null) {
         return null;
      } else {
         List<String> tokens = new ArrayList(str.length() / 5);
         char[] delimiterChars = delimiters.toCharArray();
         int start = 0;
         int end = 0;

         for(int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            boolean delimiter = false;

            for(char d : delimiterChars) {
               if (c == d) {
                  tokens.add(str.substring(start, end));
                  start = i + 1;
                  end = start;
                  delimiter = true;
                  break;
               }
            }

            if (!delimiter) {
               if (i == start && c == ' ') {
                  ++start;
                  ++end;
               }

               if (i >= start && c != ' ') {
                  end = i + 1;
               }
            }
         }

         if (start < end) {
            tokens.add(str.substring(start, end));
         }

         return tokens;
      }
   }

   public static List<String> tokenizeToStringCollection(String str, char delimiterChar, char groupDelimiterChar) {
      if (str == null) {
         return null;
      } else {
         List<String> tokens = new ArrayList(str.length() / 5);
         int start = 0;
         int end = 0;
         boolean inGroup = false;

         for(int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == groupDelimiterChar) {
               inGroup = !inGroup;
               addToken(tokens, str, start, end);
               start = i + 1;
               end = start;
            } else if (!inGroup && c == delimiterChar) {
               addToken(tokens, str, start, end);
               start = i + 1;
               end = start;
            } else if (i == start && c == ' ') {
               ++start;
               ++end;
            } else if (i >= start && c != ' ') {
               end = i + 1;
            }
         }

         addToken(tokens, str, start, end);
         return tokens;
      }
   }

   private static void addToken(List<String> tokens, String str, int start, int end) {
      if (start < end) {
         tokens.add(str.substring(start, end));
      }

   }

   public static String replace(String inString, String oldPattern, String newPattern) {
      if (hasLength(inString) && hasLength(oldPattern) && newPattern != null) {
         StringBuilder sb = new StringBuilder();
         int pos = 0;
         int index = inString.indexOf(oldPattern);

         for(int patLen = oldPattern.length(); index >= 0; index = inString.indexOf(oldPattern, pos)) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
         }

         sb.append(inString.substring(pos));
         return sb.toString();
      } else {
         return inString;
      }
   }

   public static String collectionToCommaDelimitedString(Collection<?> collection) {
      return collectionToDelimitedString(collection, ", ");
   }

   public static String collectionToDelimitedString(Collection<?> collection, String delimiter) {
      if (collection == null) {
         return "";
      } else {
         StringBuilder sb = new StringBuilder();
         Iterator it = collection.iterator();

         while(it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
               sb.append(delimiter);
            }
         }

         return sb.toString();
      }
   }

   public static String trimLeadingCharacter(String str, char character) {
      StringBuilder buf = new StringBuilder(str);

      while(buf.length() > 0 && character == buf.charAt(0)) {
         buf.deleteCharAt(0);
      }

      return buf.toString();
   }

   public static boolean startsAndEndsWith(String str, String prefix, String... suffixes) {
      if (hasLength(prefix) && !str.startsWith(prefix)) {
         return false;
      } else {
         for(String suffix : suffixes) {
            if (str.endsWith(suffix) && str.length() > (prefix + suffix).length()) {
               return true;
            }
         }

         return false;
      }
   }

   public static String wrap(String str, int lineSize) {
      if (str.length() < lineSize) {
         return str;
      } else {
         StringBuilder result = new StringBuilder();
         int oldPos = 0;

         for(int pos = lineSize; pos < str.length(); pos += lineSize) {
            result.append(str, oldPos, pos).append("\n");
            oldPos = pos;
         }

         result.append(str.substring(oldPos));
         return result.toString();
      }
   }

   public static String wordWrap(String str, int lineSize) {
      if (str.length() < lineSize) {
         return str;
      } else {
         StringBuilder result = new StringBuilder();
         int oldPos = 0;
         int pos = lineSize;

         while(pos < str.length()) {
            if (Character.isWhitespace(str.charAt(pos))) {
               ++pos;
            } else {
               String part = str.substring(oldPos, pos);
               int spacePos = part.lastIndexOf(32);
               if (spacePos > 0) {
                  pos = oldPos + spacePos + 1;
               }

               result.append(str.substring(oldPos, pos).trim()).append("\n");
               oldPos = pos;
               pos += lineSize;
            }
         }

         result.append(str.substring(oldPos));
         return result.toString();
      }
   }

   public static boolean isCharAnyOf(char c, String chars) {
      for(int i = 0; i < chars.length(); ++i) {
         if (chars.charAt(i) == c) {
            return true;
         }
      }

      return false;
   }

   public static String getFileExtension(String path) {
      String[] foldersSplit = path.split("[|/]");
      String fileNameAndExtension = foldersSplit[foldersSplit.length - 1];
      String[] nameExtensionSplit = fileNameAndExtension.split("\\.");
      return nameExtensionSplit.length < 2 ? "" : nameExtensionSplit[nameExtensionSplit.length - 1];
   }

   private StringUtils() {
   }
}
