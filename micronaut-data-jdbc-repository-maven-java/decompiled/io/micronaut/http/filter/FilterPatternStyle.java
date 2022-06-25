package io.micronaut.http.filter;

import io.micronaut.core.util.PathMatcher;

public enum FilterPatternStyle {
   ANT,
   REGEX;

   public PathMatcher getPathMatcher() {
      return (PathMatcher)(this.equals(REGEX) ? PathMatcher.REGEX : PathMatcher.ANT);
   }

   public static FilterPatternStyle defaultStyle() {
      return ANT;
   }
}
