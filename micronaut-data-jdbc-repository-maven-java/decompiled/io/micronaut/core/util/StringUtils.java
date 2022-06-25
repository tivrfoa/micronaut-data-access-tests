package io.micronaut.core.util;

import io.micronaut.core.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class StringUtils {
   public static final String TRUE = "true";
   public static final String FALSE = "false";
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   public static final String EMPTY_STRING = "";
   public static final char SPACE = ' ';
   private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

   public static boolean isEmpty(@Nullable CharSequence str) {
      return str == null || str.length() == 0;
   }

   public static boolean isNotEmpty(@Nullable CharSequence str) {
      return !isEmpty(str);
   }

   public static boolean hasText(@Nullable CharSequence str) {
      if (isEmpty(str)) {
         return false;
      } else {
         int strLen = str.length();

         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return true;
            }
         }

         return false;
      }
   }

   public static List<String> internListOf(Object... objects) {
      if (objects != null && objects.length != 0) {
         List<String> strings = new ArrayList(objects.length);

         for(Object object : objects) {
            strings.add(object.toString());
         }

         return Collections.unmodifiableList(strings);
      } else {
         return Collections.emptyList();
      }
   }

   public static Map<String, Object> internMapOf(Object... values) {
      if (values == null) {
         return Collections.emptyMap();
      } else {
         int len = values.length;
         if (len % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values");
         } else {
            Map<String, Object> answer = new HashMap((int)((double)(len / 2) / 0.75));
            int i = 0;

            while(i < values.length - 1) {
               String key = values[i++].toString();
               Object val = values[i++];
               answer.put(key, val);
            }

            return answer;
         }
      }
   }

   public static boolean isDigits(String str) {
      return isNotEmpty(str) && DIGIT_PATTERN.matcher(str).matches();
   }

   @Nullable
   public static Locale parseLocale(String localeValue) {
      String[] tokens = tokenizeToStringArray(localeValue, "_ ", false, false);
      if (tokens.length == 1) {
         validateLocalePart(localeValue);
         Locale resolved = Locale.forLanguageTag(localeValue);
         if (resolved.getLanguage().length() > 0) {
            return resolved;
         }
      }

      String language = tokens.length > 0 ? tokens[0] : "";
      String country = tokens.length > 1 ? tokens[1] : "";
      validateLocalePart(language);
      validateLocalePart(country);
      String variant = "";
      if (tokens.length > 2) {
         int endIndexOfCountryCode = localeValue.indexOf(country, language.length()) + country.length();
         variant = trimLeadingWhitespace(localeValue.substring(endIndexOfCountryCode));
         if (variant.startsWith("_")) {
            variant = trimLeadingCharacter(variant, '_');
         }
      }

      if (variant.isEmpty() && country.startsWith("#")) {
         variant = country;
         country = "";
      }

      return language.length() > 0 ? new Locale(language, country, variant) : null;
   }

   private static void validateLocalePart(String localePart) {
      for(int i = 0; i < localePart.length(); ++i) {
         char ch = localePart.charAt(i);
         if (ch != ' ' && ch != '_' && ch != '-' && ch != '#' && !Character.isLetterOrDigit(ch)) {
            throw new IllegalArgumentException("Locale part \"" + localePart + "\" contains invalid characters");
         }
      }

   }

   public static String trimLeadingWhitespace(String str) {
      return trimLeading(str, Character::isWhitespace);
   }

   public static String trimLeadingCharacter(String str, char c) {
      return trimLeading(str, character -> character == c);
   }

   public static String trimLeading(String str, Predicate<Character> predicate) {
      if (isEmpty(str)) {
         return str;
      } else {
         for(int i = 0; i < str.length(); ++i) {
            if (!predicate.test(str.charAt(i))) {
               return str.substring(i);
            }
         }

         return str;
      }
   }

   public static String[] tokenizeToStringArray(String str, String delimiters) {
      return tokenizeToStringArray(str, delimiters, true, true);
   }

   public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
      if (str == null) {
         return null;
      } else {
         StringTokenizer st = new StringTokenizer(str, delimiters);
         List<String> tokens = new ArrayList();

         while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
               token = token.trim();
            }

            if (!ignoreEmptyTokens || token.length() > 0) {
               tokens.add(token);
            }
         }

         return (String[])tokens.toArray(new String[0]);
      }
   }

   public static String convertDotToUnderscore(String dottedProperty) {
      return convertDotToUnderscore(dottedProperty, true);
   }

   public static String convertDotToUnderscore(String dottedProperty, boolean uppercase) {
      if (dottedProperty == null) {
         return dottedProperty;
      } else {
         dottedProperty = dottedProperty.replace('.', '_');
         return uppercase ? dottedProperty.toUpperCase() : dottedProperty;
      }
   }

   public static String prependUri(String baseUri, String uri) {
      if (!uri.startsWith("/") && !uri.startsWith("?")) {
         uri = "/" + uri;
      }

      if (uri.length() == 1 && uri.charAt(0) == '/') {
         uri = "";
      }

      uri = baseUri + uri;
      return uri.startsWith("/") ? uri.replaceAll("/{2,}", "/") : uri.replaceAll("(?<=[^:])/{2,}", "/");
   }

   public static String capitalize(String str) {
      char[] array = str.toCharArray();
      if (array.length > 0) {
         array[0] = Character.toUpperCase(array[0]);
      }

      return new String(array);
   }

   @Nullable
   public static String trimToNull(@Nullable String string) {
      return (String)Optional.ofNullable(string).map(String::trim).filter(StringUtils::isNotEmpty).orElse(null);
   }

   public static boolean isTrue(String booleanString) {
      if (booleanString == null) {
         return false;
      } else {
         switch(booleanString) {
            case "yes":
            case "y":
            case "on":
            case "true":
               return true;
            default:
               return false;
         }
      }
   }

   public static Iterable<String> splitOmitEmptyStrings(final CharSequence sequence, final char splitCharacter) {
      return () -> new StringUtils.SplitOmitEmptyIterator(sequence, splitCharacter);
   }

   public static List<String> splitOmitEmptyStringsList(final CharSequence sequence, final char splitCharacter) {
      int count = 0;

      for(int i = 0; i < sequence.length(); ++i) {
         if (sequence.charAt(i) == splitCharacter) {
            ++count;
         }
      }

      List<String> result = new ArrayList(count + 1);
      StringUtils.SplitOmitEmptyIterator iterator = new StringUtils.SplitOmitEmptyIterator(sequence, splitCharacter);

      while(iterator.hasNext()) {
         result.add(iterator.next());
      }

      return result;
   }

   public static Iterator<String> splitOmitEmptyStringsIterator(final CharSequence sequence, final char splitCharacter) {
      return new StringUtils.SplitOmitEmptyIterator(sequence, splitCharacter);
   }

   private static final class SplitOmitEmptyIterator implements Iterator<String> {
      private final CharSequence sequence;
      private final char splitCharacter;
      private final int length;
      private int index = 0;
      private int fromIndex = 0;
      private int toIndex = 0;
      private boolean end = false;
      private boolean hasNext = true;
      private boolean adjust = true;

      private SplitOmitEmptyIterator(CharSequence sequence, char splitCharacter) {
         this.sequence = sequence;
         this.splitCharacter = splitCharacter;
         this.length = sequence.length();
      }

      public boolean hasNext() {
         if (this.adjust) {
            this.adjust();
         }

         return this.hasNext;
      }

      public String next() {
         if (this.adjust) {
            this.adjust();
         }

         if (!this.hasNext) {
            throw new NoSuchElementException();
         } else {
            this.hasNext = false;
            this.adjust = !this.end;
            return this.fromIndex == 0 && this.toIndex == this.length
               ? this.sequence.toString()
               : this.sequence.subSequence(this.fromIndex, this.toIndex).toString();
         }
      }

      private void adjust() {
         this.adjust = false;
         this.hasNext = false;
         this.fromIndex = this.index;

         while(this.index < this.length) {
            if (this.sequence.charAt(this.index) == this.splitCharacter) {
               if (this.fromIndex != this.index) {
                  this.hasNext = true;
                  this.toIndex = this.index++;
                  return;
               }

               ++this.index;
               this.fromIndex = this.index;
            } else {
               ++this.index;
            }
         }

         if (this.fromIndex != this.index) {
            this.toIndex = this.length;
            this.hasNext = true;
         }

         this.end = true;
      }
   }
}
