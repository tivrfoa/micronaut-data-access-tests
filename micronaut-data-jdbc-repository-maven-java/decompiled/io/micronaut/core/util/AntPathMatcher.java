package io.micronaut.core.util;

public class AntPathMatcher implements PathMatcher {
   public static final String DEFAULT_PATH_SEPARATOR = "/";
   private String pathSeparator = "/";

   public void setPathSeparator(String pathSeparator) {
      this.pathSeparator = pathSeparator != null ? pathSeparator : "/";
   }

   public boolean isPattern(String path) {
      return path.indexOf(42) != -1 || path.indexOf(63) != -1;
   }

   @Override
   public boolean matches(String pattern, String source) {
      return this.doMatch(pattern, source, true);
   }

   protected boolean doMatch(String pattern, String path, boolean fullMatch) {
      if (path != null && pattern != null && path.startsWith(this.pathSeparator) == pattern.startsWith(this.pathSeparator)) {
         String[] pattDirs = StringUtils.tokenizeToStringArray(pattern, this.pathSeparator);
         String[] pathDirs = StringUtils.tokenizeToStringArray(path, this.pathSeparator);
         int pattIdxStart = 0;
         int pattIdxEnd = pattDirs.length - 1;
         int pathIdxStart = 0;

         int pathIdxEnd;
         for(pathIdxEnd = pathDirs.length - 1; pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd; ++pathIdxStart) {
            String patDir = pattDirs[pattIdxStart];
            if ("**".equals(patDir)) {
               break;
            }

            if (!this.matchStrings(patDir, pathDirs[pathIdxStart])) {
               return false;
            }

            ++pattIdxStart;
         }

         if (pathIdxStart > pathIdxEnd) {
            if (pattIdxStart > pattIdxEnd) {
               return pattern.endsWith(this.pathSeparator) ? path.endsWith(this.pathSeparator) : !path.endsWith(this.pathSeparator);
            } else if (!fullMatch) {
               return true;
            } else if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(this.pathSeparator)) {
               return true;
            } else {
               for(int i = pattIdxStart; i <= pattIdxEnd; ++i) {
                  if (!pattDirs[i].equals("**")) {
                     return false;
                  }
               }

               return true;
            }
         } else if (pattIdxStart > pattIdxEnd) {
            return false;
         } else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
            return true;
         } else {
            while(pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
               String patDir = pattDirs[pattIdxEnd];
               if (patDir.equals("**")) {
                  break;
               }

               if (!this.matchStrings(patDir, pathDirs[pathIdxEnd])) {
                  return false;
               }

               --pattIdxEnd;
               --pathIdxEnd;
            }

            if (pathIdxStart > pathIdxEnd) {
               for(int i = pattIdxStart; i <= pattIdxEnd; ++i) {
                  if (!pattDirs[i].equals("**")) {
                     return false;
                  }
               }

               return true;
            } else {
               while(pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
                  int patIdxTmp = -1;

                  for(int i = pattIdxStart + 1; i <= pattIdxEnd; ++i) {
                     if (pattDirs[i].equals("**")) {
                        patIdxTmp = i;
                        break;
                     }
                  }

                  if (patIdxTmp == pattIdxStart + 1) {
                     ++pattIdxStart;
                  } else {
                     int patLength = patIdxTmp - pattIdxStart - 1;
                     int strLength = pathIdxEnd - pathIdxStart + 1;
                     int foundIdx = -1;
                     int i = 0;

                     label140:
                     while(i <= strLength - patLength) {
                        for(int j = 0; j < patLength; ++j) {
                           String subPat = pattDirs[pattIdxStart + j + 1];
                           String subStr = pathDirs[pathIdxStart + i + j];
                           if (!this.matchStrings(subPat, subStr)) {
                              ++i;
                              continue label140;
                           }
                        }

                        foundIdx = pathIdxStart + i;
                        break;
                     }

                     if (foundIdx == -1) {
                        return false;
                     }

                     pattIdxStart = patIdxTmp;
                     pathIdxStart = foundIdx + patLength;
                  }
               }

               for(int i = pattIdxStart; i <= pattIdxEnd; ++i) {
                  if (!pattDirs[i].equals("**")) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean matchStrings(String pattern, String str) {
      char[] patArr = pattern.toCharArray();
      char[] strArr = str.toCharArray();
      int patIdxStart = 0;
      int patIdxEnd = patArr.length - 1;
      int strIdxStart = 0;
      int strIdxEnd = strArr.length - 1;
      boolean containsStar = false;

      for(char aPatArr : patArr) {
         if (aPatArr == '*') {
            containsStar = true;
            break;
         }
      }

      if (!containsStar) {
         if (patIdxEnd != strIdxEnd) {
            return false;
         } else {
            for(int i = 0; i <= patIdxEnd; ++i) {
               char ch = patArr[i];
               if (ch != '?' && ch != strArr[i]) {
                  return false;
               }
            }

            return true;
         }
      } else if (patIdxEnd == 0) {
         return true;
      } else {
         char ch;
         while((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && ch != strArr[strIdxStart]) {
               return false;
            }

            ++patIdxStart;
            ++strIdxStart;
         }

         if (strIdxStart > strIdxEnd) {
            for(int i = patIdxStart; i <= patIdxEnd; ++i) {
               if (patArr[i] != '*') {
                  return false;
               }
            }

            return true;
         } else {
            while((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
               if (ch != '?' && ch != strArr[strIdxEnd]) {
                  return false;
               }

               --patIdxEnd;
               --strIdxEnd;
            }

            if (strIdxStart > strIdxEnd) {
               for(int i = patIdxStart; i <= patIdxEnd; ++i) {
                  if (patArr[i] != '*') {
                     return false;
                  }
               }

               return true;
            } else {
               while(patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
                  int patIdxTmp = -1;

                  for(int i = patIdxStart + 1; i <= patIdxEnd; ++i) {
                     if (patArr[i] == '*') {
                        patIdxTmp = i;
                        break;
                     }
                  }

                  if (patIdxTmp == patIdxStart + 1) {
                     ++patIdxStart;
                  } else {
                     int patLength = patIdxTmp - patIdxStart - 1;
                     int strLength = strIdxEnd - strIdxStart + 1;
                     int foundIdx = -1;
                     int i = 0;

                     label132:
                     while(i <= strLength - patLength) {
                        for(int j = 0; j < patLength; ++j) {
                           ch = patArr[patIdxStart + j + 1];
                           if (ch != '?' && ch != strArr[strIdxStart + i + j]) {
                              ++i;
                              continue label132;
                           }
                        }

                        foundIdx = strIdxStart + i;
                        break;
                     }

                     if (foundIdx == -1) {
                        return false;
                     }

                     patIdxStart = patIdxTmp;
                     strIdxStart = foundIdx + patLength;
                  }
               }

               for(int i = patIdxStart; i <= patIdxEnd; ++i) {
                  if (patArr[i] != '*') {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public String extractPathWithinPattern(String pattern, String path) {
      String[] patternParts = StringUtils.tokenizeToStringArray(pattern, this.pathSeparator);
      String[] pathParts = StringUtils.tokenizeToStringArray(path, this.pathSeparator);
      StringBuilder buffer = new StringBuilder();
      int puts = 0;

      for(int i = 0; i < patternParts.length; ++i) {
         String patternPart = patternParts[i];
         if ((patternPart.indexOf(42) > -1 || patternPart.indexOf(63) > -1) && pathParts.length >= i + 1) {
            if (puts > 0 || i == 0 && !pattern.startsWith(this.pathSeparator)) {
               buffer.append(this.pathSeparator);
            }

            buffer.append(pathParts[i]);
            ++puts;
         }
      }

      for(int i = patternParts.length; i < pathParts.length; ++i) {
         if (puts > 0 || i > 0) {
            buffer.append(this.pathSeparator);
         }

         buffer.append(pathParts[i]);
      }

      return buffer.toString();
   }
}
