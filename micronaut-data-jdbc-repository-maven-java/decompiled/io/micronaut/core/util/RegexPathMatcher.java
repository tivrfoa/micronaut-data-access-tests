package io.micronaut.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RegexPathMatcher implements PathMatcher {
   private final Map<String, Pattern> compiledPatterns = new ConcurrentHashMap();

   @Override
   public boolean matches(String pattern, String source) {
      return pattern != null && source != null ? ((Pattern)this.compiledPatterns.computeIfAbsent(pattern, Pattern::compile)).matcher(source).matches() : false;
   }
}
