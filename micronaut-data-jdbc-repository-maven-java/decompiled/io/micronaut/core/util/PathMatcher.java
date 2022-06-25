package io.micronaut.core.util;

public interface PathMatcher {
   AntPathMatcher ANT = new AntPathMatcher();
   RegexPathMatcher REGEX = new RegexPathMatcher();

   boolean matches(String pattern, String source);
}
