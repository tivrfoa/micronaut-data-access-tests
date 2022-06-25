package ch.qos.logback.core.util;

import java.util.ArrayList;
import java.util.List;

public class DatePatternToRegexUtil {
   final String datePattern;
   final int datePatternLength;
   final CharSequenceToRegexMapper regexMapper = new CharSequenceToRegexMapper();

   public DatePatternToRegexUtil(String datePattern) {
      this.datePattern = datePattern;
      this.datePatternLength = datePattern.length();
   }

   public String toRegex() {
      List<CharSequenceState> charSequenceList = this.tokenize();
      StringBuilder sb = new StringBuilder();

      for(CharSequenceState seq : charSequenceList) {
         sb.append(this.regexMapper.toRegex(seq));
      }

      return sb.toString();
   }

   private List<CharSequenceState> tokenize() {
      List<CharSequenceState> sequenceList = new ArrayList();
      CharSequenceState lastCharSequenceState = null;

      for(int i = 0; i < this.datePatternLength; ++i) {
         char t = this.datePattern.charAt(i);
         if (lastCharSequenceState != null && lastCharSequenceState.c == t) {
            lastCharSequenceState.incrementOccurrences();
         } else {
            lastCharSequenceState = new CharSequenceState(t);
            sequenceList.add(lastCharSequenceState);
         }
      }

      return sequenceList;
   }
}
