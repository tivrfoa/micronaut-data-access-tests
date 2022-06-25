package ch.qos.logback.core.pattern.util;

public class AlmostAsIsEscapeUtil extends RestrictedEscapeUtil {
   @Override
   public void escape(String escapeChars, StringBuffer buf, char next, int pointer) {
      super.escape("%)", buf, next, pointer);
   }
}
