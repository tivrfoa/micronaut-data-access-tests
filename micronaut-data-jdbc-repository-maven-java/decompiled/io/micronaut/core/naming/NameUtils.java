package io.micronaut.core.naming;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameUtils {
   private static final int IS_LENGTH = 2;
   private static final Pattern DOT_UPPER = Pattern.compile("\\.[A-Z\\$]");
   private static final Pattern SERVICE_ID_REGEX = Pattern.compile("[\\p{javaLowerCase}\\d-]+");
   private static final String PREFIX_GET = "get";
   private static final String PREFIX_SET = "set";
   private static final String PREFIX_IS = "is";
   private static final Pattern ENVIRONMENT_VAR_SEQUENCE = Pattern.compile("^[\\p{Lu}_{0-9}]+");
   private static final Pattern KEBAB_CASE_SEQUENCE = Pattern.compile("^(([a-z0-9])+([-.:])?)*([a-z0-9])+$");
   private static final Pattern KEBAB_REPLACEMENTS = Pattern.compile("[_ ]");

   public static boolean isHyphenatedLowerCase(String name) {
      return StringUtils.isNotEmpty(name) && SERVICE_ID_REGEX.matcher(name).matches() && Character.isLetter(name.charAt(0));
   }

   public static String decapitalizeWithoutSuffix(String name, String... suffixes) {
      String decapitalized = decapitalize(name);
      return trimSuffix(decapitalized, suffixes);
   }

   public static String trimSuffix(String string, String... suffixes) {
      if (suffixes != null) {
         for(String suffix : suffixes) {
            if (string.endsWith(suffix)) {
               return string.substring(0, string.length() - suffix.length());
            }
         }
      }

      return string;
   }

   public static String capitalize(String name) {
      String rest = name.substring(1);
      return Character.isLowerCase(name.charAt(0)) && rest.length() > 0 && Character.isUpperCase(rest.charAt(0))
         ? name
         : name.substring(0, 1).toUpperCase(Locale.ENGLISH) + rest;
   }

   public static String hyphenate(String name) {
      return hyphenate(name, true);
   }

   public static String hyphenate(String name, boolean lowerCase) {
      if (isHyphenatedLowerCase(name)) {
         return KEBAB_REPLACEMENTS.matcher(name).replaceAll("-");
      } else {
         char separatorChar = '-';
         return separateCamelCase(KEBAB_REPLACEMENTS.matcher(name).replaceAll("-"), lowerCase, separatorChar);
      }
   }

   public static String dehyphenate(String name) {
      StringBuilder sb = new StringBuilder(name.length());

      for(String token : StringUtils.splitOmitEmptyStrings(name, '-')) {
         if (token.length() > 0 && Character.isLetter(token.charAt(0))) {
            sb.append(Character.toUpperCase(token.charAt(0)));
            sb.append(token.substring(1));
         } else {
            sb.append(token);
         }
      }

      return sb.toString();
   }

   public static String getPackageName(String className) {
      Matcher matcher = DOT_UPPER.matcher(className);
      if (matcher.find()) {
         int position = matcher.start();
         return className.substring(0, position);
      } else {
         return "";
      }
   }

   public static String underscoreSeparate(String camelCase) {
      return separateCamelCase(camelCase.replace('-', '_'), false, '_');
   }

   public static String environmentName(String camelCase) {
      return separateCamelCase(camelCase.replace('-', '_').replace('.', '_'), false, '_').toUpperCase(Locale.ENGLISH);
   }

   public static String getSimpleName(String className) {
      Matcher matcher = DOT_UPPER.matcher(className);
      if (matcher.find()) {
         int position = matcher.start();
         return className.substring(position + 1);
      } else {
         return className;
      }
   }

   public static boolean isSetterName(String methodName) {
      return isWriterName(methodName, "set");
   }

   public static boolean isWriterName(@NonNull String methodName, @NonNull String writePrefix) {
      return isWriterName(methodName, new String[]{writePrefix});
   }

   public static boolean isWriterName(@NonNull String methodName, @NonNull String[] writePrefixes) {
      boolean isValid = false;

      for(String writePrefix : writePrefixes) {
         if (writePrefix.length() == 0) {
            return true;
         }

         int len = methodName.length();
         int prefixLength = writePrefix.length();
         if (len > prefixLength && methodName.startsWith(writePrefix)) {
            isValid = Character.isUpperCase(methodName.charAt(prefixLength));
         }

         if (isValid) {
            break;
         }
      }

      return isValid;
   }

   public static String getPropertyNameForSetter(String setterName) {
      return getPropertyNameForSetter(setterName, "set");
   }

   @NonNull
   public static String getPropertyNameForSetter(@NonNull String setterName, @NonNull String writePrefix) {
      return getPropertyNameForSetter(setterName, new String[]{writePrefix});
   }

   @NonNull
   public static String getPropertyNameForSetter(@NonNull String setterName, @NonNull String[] writePrefixes) {
      for(String writePrefix : writePrefixes) {
         if (isWriterName(setterName, writePrefix)) {
            return decapitalize(setterName.substring(writePrefix.length()));
         }
      }

      return setterName;
   }

   @NonNull
   public static String setterNameFor(@NonNull String propertyName) {
      return setterNameFor(propertyName, "set");
   }

   @NonNull
   public static String setterNameFor(@NonNull String propertyName, @NonNull String[] prefixes) {
      return prefixes.length == 0 ? setterNameFor(propertyName, "") : setterNameFor(propertyName, prefixes[0]);
   }

   @NonNull
   public static String setterNameFor(@NonNull String propertyName, @NonNull String prefix) {
      ArgumentUtils.requireNonNull("propertyName", propertyName);
      ArgumentUtils.requireNonNull("prefix", prefix);
      return nameFor(prefix, propertyName);
   }

   public static boolean isGetterName(String methodName) {
      return isReaderName(methodName, "get");
   }

   public static boolean isReaderName(@NonNull String methodName, @NonNull String readPrefix) {
      return isReaderName(methodName, new String[]{readPrefix});
   }

   public static boolean isReaderName(@NonNull String methodName, @NonNull String[] readPrefixes) {
      boolean isValid = false;

      for(String readPrefix : readPrefixes) {
         int prefixLength = 0;
         if (readPrefix.length() == 0) {
            return true;
         }

         if (methodName.startsWith(readPrefix)) {
            prefixLength = readPrefix.length();
         } else if (methodName.startsWith("is") && readPrefix.equals("get")) {
            prefixLength = 2;
         }

         int len = methodName.length();
         if (len > prefixLength) {
            isValid = Character.isUpperCase(methodName.charAt(prefixLength));
         }

         if (isValid) {
            break;
         }
      }

      return isValid;
   }

   public static String getPropertyNameForGetter(String getterName) {
      return getPropertyNameForGetter(getterName, "get");
   }

   @NonNull
   public static String getPropertyNameForGetter(@NonNull String getterName, @NonNull String readPrefix) {
      return getPropertyNameForGetter(getterName, new String[]{readPrefix});
   }

   @NonNull
   public static String getPropertyNameForGetter(@NonNull String getterName, @NonNull String[] readPrefixes) {
      for(String readPrefix : readPrefixes) {
         if (isReaderName(getterName, readPrefix)) {
            int prefixLength = 0;
            if (getterName.startsWith(readPrefix)) {
               prefixLength = readPrefix.length();
            }

            if (getterName.startsWith("is") && readPrefix.equals("get")) {
               prefixLength = 2;
            }

            return decapitalize(getterName.substring(prefixLength));
         }
      }

      return getterName;
   }

   @NonNull
   public static String getterNameFor(@NonNull String propertyName) {
      return getterNameFor(propertyName, "get");
   }

   @NonNull
   public static String getterNameFor(@NonNull String propertyName, @NonNull String[] prefixes) {
      return prefixes.length == 0 ? getterNameFor(propertyName, "") : getterNameFor(propertyName, prefixes[0]);
   }

   @NonNull
   public static String getterNameFor(@NonNull String propertyName, @NonNull String prefix) {
      ArgumentUtils.requireNonNull("propertyName", propertyName);
      ArgumentUtils.requireNonNull("prefix", prefix);
      return nameFor(prefix, propertyName);
   }

   @NonNull
   public static String getterNameFor(@NonNull String propertyName, @NonNull Class<?> type) {
      ArgumentUtils.requireNonNull("propertyName", propertyName);
      boolean isBoolean = type == Boolean.TYPE;
      return getterNameFor(propertyName, isBoolean);
   }

   public static String getterNameFor(@NonNull String propertyName, boolean isBoolean) {
      return nameFor(isBoolean ? "is" : "get", propertyName);
   }

   private static String nameFor(String prefix, @NonNull String propertyName) {
      if (prefix.length() == 0) {
         return propertyName;
      } else {
         int len = propertyName.length();
         switch(len) {
            case 0:
               return propertyName;
            case 1:
               return prefix + propertyName.toUpperCase(Locale.ENGLISH);
            default:
               return prefix + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
         }
      }
   }

   public static String decapitalize(String name) {
      if (name == null) {
         return null;
      } else {
         int length = name.length();
         if (length == 0) {
            return name;
         } else {
            boolean firstUpper = Character.isUpperCase(name.charAt(0));
            if (firstUpper) {
               if (length == 1) {
                  return Character.toString(Character.toLowerCase(name.charAt(0)));
               }

               for(int i = 1; i < Math.min(length, 3); ++i) {
                  if (!Character.isUpperCase(name.charAt(i))) {
                     char[] chars = name.toCharArray();
                     chars[0] = Character.toLowerCase(chars[0]);
                     return new String(chars);
                  }
               }
            }

            return name;
         }
      }
   }

   private static String separateCamelCase(String name, boolean lowerCase, char separatorChar) {
      if (!lowerCase) {
         StringBuilder newName = new StringBuilder();
         boolean first = true;
         char last = '0';

         for(char c : name.toCharArray()) {
            if (first) {
               newName.append(c);
               first = false;
            } else if (Character.isUpperCase(c) && !Character.isUpperCase(last)) {
               if (c != separatorChar) {
                  newName.append(separatorChar);
               }

               newName.append(c);
            } else {
               if (c == '.') {
                  first = true;
               }

               if (c != separatorChar) {
                  if (last == separatorChar) {
                     newName.append(separatorChar);
                  }

                  newName.append(c);
               }
            }

            last = c;
         }

         return newName.toString();
      } else {
         StringBuilder newName = new StringBuilder();
         char[] chars = name.toCharArray();
         boolean first = true;
         char last = '0';
         char secondLast = separatorChar;

         for(int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (!Character.isLowerCase(c) && Character.isLetter(c)) {
               char lowerCaseChar = Character.toLowerCase(c);
               if (first) {
                  first = false;
                  newName.append(lowerCaseChar);
               } else if (!Character.isUpperCase(last) && last != '.') {
                  if (!Character.isDigit(last) || !Character.isUpperCase(secondLast) && secondLast != separatorChar) {
                     newName.append(separatorChar).append(lowerCaseChar);
                  } else {
                     newName.append(lowerCaseChar);
                  }
               } else {
                  newName.append(lowerCaseChar);
               }
            } else {
               first = false;
               if (c != separatorChar) {
                  if (last == separatorChar) {
                     newName.append(separatorChar);
                  }

                  newName.append(c);
               }
            }

            if (i > 1) {
               secondLast = last;
            }

            last = c;
         }

         return newName.toString();
      }
   }

   public static String extension(String filename) {
      int extensionPos = filename.lastIndexOf(46);
      int lastUnixPos = filename.lastIndexOf(47);
      int lastWindowsPos = filename.lastIndexOf(92);
      int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
      int index = lastSeparator > extensionPos ? -1 : extensionPos;
      return index == -1 ? "" : filename.substring(index + 1);
   }

   public static String camelCase(String str) {
      return camelCase(str, true);
   }

   public static String camelCase(String str, boolean lowerCaseFirstLetter) {
      StringBuilder sb = new StringBuilder(str.length());

      for(String s : str.split("[\\s_-]")) {
         String capitalize = capitalize(s);
         sb.append(capitalize);
      }

      String result = sb.toString();
      return lowerCaseFirstLetter ? decapitalize(result) : result;
   }

   public static String filename(String path) {
      int extensionPos = path.lastIndexOf(46);
      int lastUnixPos = path.lastIndexOf(47);
      int lastWindowsPos = path.lastIndexOf(92);
      int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
      int index = lastSeparator > extensionPos ? path.length() : extensionPos;
      return index == -1 ? "" : path.substring(lastSeparator + 1, index);
   }

   public static boolean isValidHyphenatedPropertyName(String str) {
      return KEBAB_CASE_SEQUENCE.matcher(str).matches();
   }

   public static boolean isEnvironmentName(String str) {
      return ENVIRONMENT_VAR_SEQUENCE.matcher(str).matches();
   }
}
