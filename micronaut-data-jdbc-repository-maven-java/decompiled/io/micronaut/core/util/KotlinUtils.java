package io.micronaut.core.util;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import kotlin.coroutines.intrinsics.IntrinsicsKt;

@Internal
public class KotlinUtils {
   public static final boolean KOTLIN_COROUTINES_SUPPORTED;
   public static final Object COROUTINE_SUSPENDED;

   public static boolean isKotlinCoroutineSuspended(@Nullable Object obj) {
      return KOTLIN_COROUTINES_SUPPORTED && obj == COROUTINE_SUSPENDED;
   }

   static {
      boolean areKotlinCoroutinesSupportedCandidate;
      Object coroutineSuspendedCandidate;
      try {
         coroutineSuspendedCandidate = IntrinsicsKt.getCOROUTINE_SUSPENDED();
         areKotlinCoroutinesSupportedCandidate = true;
      } catch (NoClassDefFoundError var3) {
         coroutineSuspendedCandidate = null;
         areKotlinCoroutinesSupportedCandidate = false;
      }

      KOTLIN_COROUTINES_SUPPORTED = areKotlinCoroutinesSupportedCandidate;
      COROUTINE_SUSPENDED = coroutineSuspendedCandidate;
   }
}
