package io.micronaut.core.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;

@TargetClass(
   className = "io.micronaut.caffeine.cache.UnsafeRefArrayAccess"
)
final class Target_io_micronaut_caffeine_cache_UnsafeRefArrayAccess {
   @Alias
   @RecomputeFieldValue(
      kind = Kind.ArrayIndexShift,
      declClass = Object.class
   )
   public static int REF_ELEMENT_SHIFT;
}
