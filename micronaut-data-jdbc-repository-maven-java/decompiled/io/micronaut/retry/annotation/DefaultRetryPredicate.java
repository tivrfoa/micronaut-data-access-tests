package io.micronaut.retry.annotation;

import io.micronaut.core.annotation.Introspected;
import java.util.Collections;
import java.util.List;

@Introspected
public class DefaultRetryPredicate implements RetryPredicate {
   private final List<Class<? extends Throwable>> includes;
   private final List<Class<? extends Throwable>> excludes;
   private final boolean hasIncludes;
   private final boolean hasExcludes;

   public DefaultRetryPredicate(List<Class<? extends Throwable>> includes, List<Class<? extends Throwable>> excludes) {
      this.includes = includes;
      this.excludes = excludes;
      this.hasIncludes = !includes.isEmpty();
      this.hasExcludes = !excludes.isEmpty();
   }

   public DefaultRetryPredicate() {
      this(Collections.emptyList(), Collections.emptyList());
   }

   public boolean test(Throwable exception) {
      if (this.hasIncludes && this.includes.stream().noneMatch(cls -> cls.isInstance(exception))) {
         return false;
      } else {
         return !this.hasExcludes || !this.excludes.stream().anyMatch(cls -> cls.isInstance(exception));
      }
   }
}
