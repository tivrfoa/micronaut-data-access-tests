package com.mysql.cj.util;

import com.mysql.cj.Messages;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class StringInspector {
   private static final int NON_COMMENTS_MYSQL_VERSION_REF_LENGTH = 5;
   private String source = null;
   private String openingMarkers = null;
   private String closingMarkers = null;
   private String overridingMarkers = null;
   private Set<SearchMode> defaultSearchMode = null;
   private int srcLen = 0;
   private int pos = 0;
   private int stopAt = 0;
   private boolean escaped = false;
   private boolean inMysqlBlock = false;
   private int markedPos = this.pos;
   private int markedStopAt = this.stopAt;
   private boolean markedEscape = this.escaped;
   private boolean markedInMysqlBlock = this.inMysqlBlock;

   public StringInspector(String source, String openingMarkers, String closingMarkers, String overridingMarkers, Set<SearchMode> searchMode) {
      this(source, 0, openingMarkers, closingMarkers, overridingMarkers, searchMode);
   }

   public StringInspector(
      String source, int startingPosition, String openingMarkers, String closingMarkers, String overridingMarkers, Set<SearchMode> searchMode
   ) {
      if (source == null) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.1"));
      } else {
         this.source = source;
         this.openingMarkers = openingMarkers;
         this.closingMarkers = closingMarkers;
         this.overridingMarkers = overridingMarkers;
         this.defaultSearchMode = searchMode;
         if (this.defaultSearchMode.contains(SearchMode.SKIP_BETWEEN_MARKERS)) {
            if (this.openingMarkers == null || this.closingMarkers == null || this.openingMarkers.length() != this.closingMarkers.length()) {
               throw new IllegalArgumentException(Messages.getString("StringInspector.2", new String[]{this.openingMarkers, this.closingMarkers}));
            }

            if (this.overridingMarkers == null) {
               throw new IllegalArgumentException(Messages.getString("StringInspector.3", new String[]{this.overridingMarkers, this.openingMarkers}));
            }

            for(char c : this.overridingMarkers.toCharArray()) {
               if (this.openingMarkers.indexOf(c) == -1) {
                  throw new IllegalArgumentException(Messages.getString("StringInspector.3", new String[]{this.overridingMarkers, this.openingMarkers}));
               }
            }
         }

         this.srcLen = source.length();
         this.pos = 0;
         this.stopAt = this.srcLen;
         this.setStartPosition(startingPosition);
      }
   }

   public int setStartPosition(int pos) {
      if (pos < 0) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.4"));
      } else if (pos > this.stopAt) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.5"));
      } else {
         int prevPos = this.pos;
         this.pos = pos;
         this.resetEscaped();
         this.inMysqlBlock = false;
         return prevPos;
      }
   }

   public int setStopPosition(int pos) {
      if (pos < 0) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.6"));
      } else if (pos > this.srcLen) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.7"));
      } else {
         int prevPos = this.stopAt;
         this.stopAt = pos;
         return prevPos;
      }
   }

   public void mark() {
      this.markedPos = this.pos;
      this.markedStopAt = this.stopAt;
      this.markedEscape = this.escaped;
      this.markedInMysqlBlock = this.inMysqlBlock;
   }

   public void reset() {
      this.pos = this.markedPos;
      this.stopAt = this.markedStopAt;
      this.escaped = this.markedEscape;
      this.inMysqlBlock = this.markedInMysqlBlock;
   }

   public void restart() {
      this.pos = 0;
      this.stopAt = this.srcLen;
      this.escaped = false;
      this.inMysqlBlock = false;
   }

   public char getChar() {
      return this.pos >= this.stopAt ? '\u0000' : this.source.charAt(this.pos);
   }

   public int getPosition() {
      return this.pos;
   }

   public int incrementPosition() {
      return this.incrementPosition(this.defaultSearchMode);
   }

   public int incrementPosition(Set<SearchMode> searchMode) {
      if (this.pos >= this.stopAt) {
         return -1;
      } else {
         if (searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && this.getChar() == '\\') {
            this.escaped = !this.escaped;
         } else if (this.escaped) {
            this.escaped = false;
         }

         return ++this.pos;
      }
   }

   public int incrementPosition(int by) {
      return this.incrementPosition(by, this.defaultSearchMode);
   }

   public int incrementPosition(int by, Set<SearchMode> searchMode) {
      for(int i = 0; i < by; ++i) {
         if (this.incrementPosition(searchMode) == -1) {
            return -1;
         }
      }

      return this.pos;
   }

   private void resetEscaped() {
      this.escaped = false;
      if (this.defaultSearchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE)) {
         for(int i = this.pos - 1; i >= 0 && this.source.charAt(i) == '\\'; --i) {
            this.escaped = !this.escaped;
         }
      }

   }

   public int indexOfNextChar() {
      return this.indexOfNextChar(this.defaultSearchMode);
   }

   private int indexOfNextChar(Set<SearchMode> searchMode) {
      if (this.source == null) {
         return -1;
      } else if (this.pos >= this.stopAt) {
         return -1;
      } else {
         char c0 = '\u0000';
         char c1 = this.source.charAt(this.pos);

         for(char c2 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0; this.pos < this.stopAt; ++this.pos) {
            c0 = c1;
            c1 = c2;
            c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
            boolean dashDashCommentImmediateEnd = false;
            boolean checkSkipConditions = !searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) || !this.escaped;
            if (checkSkipConditions && searchMode.contains(SearchMode.SKIP_BETWEEN_MARKERS) && this.openingMarkers.indexOf(c0) != -1) {
               this.indexOfClosingMarker(searchMode);
               if (this.pos >= this.stopAt) {
                  --this.pos;
               } else {
                  c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
                  c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
               }
            } else if (checkSkipConditions && searchMode.contains(SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '/' && c1 == '*' && c2 != '!' && c2 != '+') {
               ++this.pos;

               while(
                  ++this.pos < this.stopAt
                     && (this.source.charAt(this.pos) != '*' || (this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0) != '/')
               ) {
               }

               if (this.pos >= this.stopAt) {
                  --this.pos;
               } else {
                  ++this.pos;
               }

               c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
               c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
            } else if (!checkSkipConditions
               || !searchMode.contains(SearchMode.SKIP_LINE_COMMENTS)
               || (c0 != '-' || c1 != '-' || !Character.isWhitespace(c2) && !(dashDashCommentImmediateEnd = c2 == ';') && c2 != 0) && c0 != '#') {
               if (checkSkipConditions && searchMode.contains(SearchMode.SKIP_HINT_BLOCKS) && c0 == '/' && c1 == '*' && c2 == '+') {
                  ++this.pos;
                  ++this.pos;

                  while(
                     ++this.pos < this.stopAt
                        && (this.source.charAt(this.pos) != '*' || (this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0) != '/')
                  ) {
                  }

                  if (this.pos >= this.stopAt) {
                     --this.pos;
                  } else {
                     ++this.pos;
                  }

                  c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
                  c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
               } else if (checkSkipConditions && searchMode.contains(SearchMode.SKIP_MYSQL_MARKERS) && c0 == '/' && c1 == '*' && c2 == '!') {
                  ++this.pos;
                  ++this.pos;
                  if (c2 == '!') {
                     int i = 0;

                     while(i < 5 && this.pos + 1 + i < this.srcLen && Character.isDigit(this.source.charAt(this.pos + 1 + i))) {
                        ++i;
                     }

                     if (i == 5) {
                        this.pos += 5;
                        if (this.pos >= this.stopAt) {
                           this.pos = this.stopAt - 1;
                        }
                     }
                  }

                  c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
                  c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
                  this.inMysqlBlock = true;
               } else if (this.inMysqlBlock && checkSkipConditions && searchMode.contains(SearchMode.SKIP_MYSQL_MARKERS) && c0 == '*' && c1 == '/') {
                  ++this.pos;
                  c1 = c2;
                  c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
                  this.inMysqlBlock = false;
               } else if (!searchMode.contains(SearchMode.SKIP_WHITE_SPACE) || !Character.isWhitespace(c0)) {
                  return this.pos;
               }
            } else if (dashDashCommentImmediateEnd) {
               ++this.pos;
               ++this.pos;
               c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
               c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
            } else {
               while(++this.pos < this.stopAt && (c0 = this.source.charAt(this.pos)) != '\n' && c0 != '\r') {
               }

               if (this.pos >= this.stopAt) {
                  --this.pos;
               } else {
                  c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
                  if (c0 == '\r' && c1 == '\n') {
                     ++this.pos;
                     c1 = this.pos + 1 < this.srcLen ? this.source.charAt(this.pos + 1) : 0;
                  }

                  c2 = this.pos + 2 < this.srcLen ? this.source.charAt(this.pos + 2) : 0;
               }
            }

            this.escaped = false;
         }

         return -1;
      }
   }

   private int indexOfClosingMarker(Set<SearchMode> searchMode) {
      if (this.source == null) {
         return -1;
      } else if (this.pos >= this.stopAt) {
         return -1;
      } else {
         char c0 = this.source.charAt(this.pos);
         int markerIndex = this.openingMarkers.indexOf(c0);
         if (markerIndex == -1) {
            return this.pos;
         } else {
            int nestedMarkersCount = 0;
            char openingMarker = c0;
            char closingMarker = this.closingMarkers.charAt(markerIndex);
            boolean outerIsAnOverridingMarker = this.overridingMarkers.indexOf(c0) != -1;

            while(++this.pos < this.stopAt && ((c0 = this.source.charAt(this.pos)) != closingMarker || nestedMarkersCount != 0)) {
               if (!outerIsAnOverridingMarker && this.overridingMarkers.indexOf(c0) != -1) {
                  int overridingMarkerIndex = this.openingMarkers.indexOf(c0);
                  int overridingNestedMarkersCount = 0;
                  char overridingOpeningMarker = c0;
                  char overridingClosingMarker = this.closingMarkers.charAt(overridingMarkerIndex);

                  while(++this.pos < this.stopAt && ((c0 = this.source.charAt(this.pos)) != overridingClosingMarker || overridingNestedMarkersCount != 0)) {
                     if (c0 == overridingOpeningMarker) {
                        ++overridingNestedMarkersCount;
                     } else if (c0 == overridingClosingMarker) {
                        --overridingNestedMarkersCount;
                     } else if (searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                        ++this.pos;
                     }
                  }

                  if (this.pos >= this.stopAt) {
                     --this.pos;
                  }
               } else if (c0 == openingMarker) {
                  ++nestedMarkersCount;
               } else if (c0 == closingMarker) {
                  --nestedMarkersCount;
               } else if (searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                  ++this.pos;
               }
            }

            return this.pos;
         }
      }
   }

   public int indexOfNextAlphanumericChar() {
      if (this.source == null) {
         return -1;
      } else if (this.pos >= this.stopAt) {
         return -1;
      } else {
         Set<SearchMode> searchMode = this.defaultSearchMode;
         if (!this.defaultSearchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(this.defaultSearchMode);
            searchMode.add(SearchMode.SKIP_WHITE_SPACE);
         }

         while(this.pos < this.stopAt) {
            int prevPos = this.pos;
            if (this.indexOfNextChar(searchMode) == -1) {
               return -1;
            }

            if (Character.isLetterOrDigit(this.source.charAt(this.pos))) {
               return this.pos;
            }

            if (this.pos == prevPos) {
               this.incrementPosition(searchMode);
            }
         }

         return -1;
      }
   }

   public int indexOfNextNonWsChar() {
      if (this.source == null) {
         return -1;
      } else if (this.pos >= this.stopAt) {
         return -1;
      } else {
         Set<SearchMode> searchMode = this.defaultSearchMode;
         if (!this.defaultSearchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(this.defaultSearchMode);
            searchMode.add(SearchMode.SKIP_WHITE_SPACE);
         }

         return this.indexOfNextChar(searchMode);
      }
   }

   public int indexOfNextWsChar() {
      if (this.source == null) {
         return -1;
      } else if (this.pos >= this.stopAt) {
         return -1;
      } else {
         Set<SearchMode> searchMode = this.defaultSearchMode;
         if (this.defaultSearchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(this.defaultSearchMode);
            searchMode.remove(SearchMode.SKIP_WHITE_SPACE);
         }

         while(this.pos < this.stopAt) {
            int prevPos = this.pos;
            if (this.indexOfNextChar(searchMode) == -1) {
               return -1;
            }

            if (Character.isWhitespace(this.source.charAt(this.pos))) {
               return this.pos;
            }

            if (this.pos == prevPos) {
               this.incrementPosition(searchMode);
            }
         }

         return -1;
      }
   }

   public int indexOfIgnoreCase(String searchFor) {
      return this.indexOfIgnoreCase(searchFor, this.defaultSearchMode);
   }

   public int indexOfIgnoreCase(String searchFor, Set<SearchMode> searchMode) {
      if (searchFor == null) {
         return -1;
      } else {
         int searchForLength = searchFor.length();
         int localStopAt = this.srcLen - searchForLength + 1;
         if (localStopAt > this.stopAt) {
            localStopAt = this.stopAt;
         }

         if (this.pos < localStopAt && searchForLength != 0) {
            char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
            char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
            Set<SearchMode> localSearchMode = searchMode;
            if (Character.isWhitespace(firstCharOfSearchForLc) && this.defaultSearchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
               localSearchMode = EnumSet.copyOf(this.defaultSearchMode);
               localSearchMode.remove(SearchMode.SKIP_WHITE_SPACE);
            }

            while(this.pos < localStopAt) {
               if (this.indexOfNextChar(localSearchMode) == -1) {
                  return -1;
               }

               if (StringUtils.isCharEqualIgnoreCase(this.getChar(), firstCharOfSearchForUc, firstCharOfSearchForLc)
                  && StringUtils.regionMatchesIgnoreCase(this.source, this.pos, searchFor)) {
                  return this.pos;
               }

               this.incrementPosition(localSearchMode);
            }

            return -1;
         } else {
            return -1;
         }
      }
   }

   public int indexOfIgnoreCase(String... searchFor) {
      if (searchFor == null) {
         return -1;
      } else {
         int searchForLength = 0;

         for(String searchForPart : searchFor) {
            searchForLength += searchForPart.length();
         }

         if (searchForLength == 0) {
            return -1;
         } else {
            int searchForWordsCount = searchFor.length;
            searchForLength += searchForWordsCount > 0 ? searchForWordsCount - 1 : 0;
            int localStopAt = this.srcLen - searchForLength + 1;
            if (localStopAt > this.stopAt) {
               localStopAt = this.stopAt;
            }

            if (this.pos >= localStopAt) {
               return -1;
            } else {
               Set<SearchMode> searchMode1 = this.defaultSearchMode;
               if (Character.isWhitespace(searchFor[0].charAt(0)) && this.defaultSearchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
                  searchMode1 = EnumSet.copyOf(this.defaultSearchMode);
                  searchMode1.remove(SearchMode.SKIP_WHITE_SPACE);
               }

               Set<SearchMode> searchMode2 = EnumSet.copyOf(this.defaultSearchMode);
               searchMode2.add(SearchMode.SKIP_WHITE_SPACE);
               searchMode2.remove(SearchMode.SKIP_BETWEEN_MARKERS);

               while(this.pos < localStopAt) {
                  int positionOfFirstWord = this.indexOfIgnoreCase(searchFor[0], searchMode1);
                  if (positionOfFirstWord == -1 || positionOfFirstWord >= localStopAt) {
                     return -1;
                  }

                  this.mark();
                  int startingPositionForNextWord = this.incrementPosition(searchFor[0].length(), searchMode2);
                  int wc = 0;
                  boolean match = true;

                  while(true) {
                     ++wc;
                     if (wc >= searchForWordsCount || !match) {
                        if (match) {
                           this.reset();
                           return positionOfFirstWord;
                        }
                        break;
                     }

                     if (this.indexOfNextChar(searchMode2) != -1
                        && startingPositionForNextWord != this.pos
                        && StringUtils.regionMatchesIgnoreCase(this.source, this.pos, searchFor[wc])) {
                        startingPositionForNextWord = this.incrementPosition(searchFor[wc].length(), searchMode2);
                     } else {
                        match = false;
                     }
                  }
               }

               return -1;
            }
         }
      }
   }

   public int matchesIgnoreCase(String toMatch) {
      if (toMatch == null) {
         return -1;
      } else {
         int toMatchLength = toMatch.length();
         int localStopAt = this.srcLen - toMatchLength + 1;
         if (localStopAt > this.stopAt) {
            localStopAt = this.stopAt;
         }

         if (this.pos < localStopAt && toMatchLength != 0) {
            char firstCharOfToMatchUc = Character.toUpperCase(toMatch.charAt(0));
            char firstCharOfToMatchLc = Character.toLowerCase(toMatch.charAt(0));
            return StringUtils.isCharEqualIgnoreCase(this.getChar(), firstCharOfToMatchUc, firstCharOfToMatchLc)
                  && StringUtils.regionMatchesIgnoreCase(this.source, this.pos, toMatch)
               ? this.pos + toMatchLength
               : -1;
         } else {
            return -1;
         }
      }
   }

   public int matchesIgnoreCase(String... toMatch) {
      if (toMatch == null) {
         return -1;
      } else {
         int toMatchLength = 0;

         for(String toMatchPart : toMatch) {
            toMatchLength += toMatchPart.length();
         }

         if (toMatchLength == 0) {
            return -1;
         } else {
            int toMatchWordsCount = toMatch.length;
            toMatchLength += toMatchWordsCount > 0 ? toMatchWordsCount - 1 : 0;
            int localStopAt = this.srcLen - toMatchLength + 1;
            if (localStopAt > this.stopAt) {
               localStopAt = this.stopAt;
            }

            if (this.pos >= localStopAt) {
               return -1;
            } else {
               Set<SearchMode> searchMode2 = EnumSet.copyOf(this.defaultSearchMode);
               searchMode2.add(SearchMode.SKIP_WHITE_SPACE);
               searchMode2.remove(SearchMode.SKIP_BETWEEN_MARKERS);
               this.mark();
               int endOfMatch = -1;
               int startingPositionForNextWord = -1;

               for(String searchForPart : toMatch) {
                  if (this.getPosition() == startingPositionForNextWord) {
                     this.reset();
                     return -1;
                  }

                  endOfMatch = this.matchesIgnoreCase(searchForPart);
                  if (endOfMatch == -1) {
                     this.reset();
                     return -1;
                  }

                  startingPositionForNextWord = this.incrementPosition(searchForPart.length(), searchMode2);
                  this.indexOfNextChar(searchMode2);
               }

               this.reset();
               return endOfMatch;
            }
         }
      }
   }

   public String stripCommentsAndHints() {
      this.restart();
      Set<SearchMode> searchMode = EnumSet.of(SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_HINT_BLOCKS);
      if (this.defaultSearchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE)) {
         searchMode.add(SearchMode.ALLOW_BACKSLASH_ESCAPE);
      }

      StringBuilder noCommsStr = new StringBuilder(this.source.length());

      while(this.pos < this.stopAt) {
         int prevPos = this.pos;
         if (this.indexOfNextChar(searchMode) == -1) {
            return noCommsStr.toString();
         }

         if (!this.escaped && this.openingMarkers.indexOf(this.getChar()) != -1) {
            int idxOpMrkr = this.pos;
            if (this.indexOfClosingMarker(searchMode) < this.srcLen) {
               this.incrementPosition(searchMode);
            }

            noCommsStr.append(this.source, idxOpMrkr, this.pos);
         } else {
            if (this.pos - prevPos > 1
               && prevPos > 0
               && !Character.isWhitespace(this.source.charAt(prevPos - 1))
               && !Character.isWhitespace(this.source.charAt(this.pos))) {
               noCommsStr.append(" ");
            }

            noCommsStr.append(this.getChar());
            this.incrementPosition(searchMode);
         }
      }

      return noCommsStr.toString();
   }

   public List<String> split(String delimiter, boolean trim) {
      if (delimiter == null) {
         throw new IllegalArgumentException(Messages.getString("StringInspector.8"));
      } else {
         this.restart();
         int startPos = 0;

         List<String> splitParts;
         for(splitParts = new ArrayList(); this.indexOfIgnoreCase(delimiter) != -1; startPos = this.incrementPosition(delimiter.length())) {
            this.indexOfIgnoreCase(delimiter);
            String part = this.source.substring(startPos, this.pos);
            if (trim) {
               part = part.trim();
            }

            splitParts.add(part);
         }

         String token = this.source.substring(startPos);
         if (trim) {
            token = token.trim();
         }

         splitParts.add(token);
         return splitParts;
      }
   }
}
