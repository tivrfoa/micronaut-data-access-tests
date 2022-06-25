package io.micronaut.aop.util;

import io.micronaut.core.annotation.Internal;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.Result.Companion;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.SafeContinuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.JvmStatic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\bÁ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J'\u0010\u0003\u001a\u0004\u0018\u00010\u00012\n\u0010\u0004\u001a\u0006\u0012\u0002\b\u00030\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0087@ø\u0001\u0000¢\u0006\u0002\u0010\b\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\t"},
   d2 = {"Lio/micronaut/aop/util/KotlinInterceptedMethodHelper;", "", "()V", "handleResult", "result", "Ljava/util/concurrent/CompletionStage;", "isUnitValueType", "", "(Ljava/util/concurrent/CompletionStage;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "aop"}
)
public final class KotlinInterceptedMethodHelper {
   @NotNull
   public static final KotlinInterceptedMethodHelper INSTANCE = new KotlinInterceptedMethodHelper();

   private KotlinInterceptedMethodHelper() {
   }

   @JvmStatic
   @Nullable
   public static final Object handleResult(@NotNull CompletionStage<?> result, final boolean isUnitValueType, @NotNull Continuation<Object> $completion) {
      SafeContinuation var4 = new SafeContinuation(IntrinsicsKt.intercepted($completion));
      final Continuation continuation = (Continuation)var4;
      int $i$a$-suspendCoroutine-KotlinInterceptedMethodHelper$handleResult$2 = 0;
      result.whenComplete(new BiConsumer() {
         public final void accept(@Nullable Object value, @Nullable Throwable throwable) {
            if (throwable == null) {
               Companion var10000 = Result.Companion;
               Object var5 = value;
               if (value == null) {
                  var5 = isUnitValueType ? Unit.INSTANCE : null;
               }

               Object result = Result.constructor-impl(var5);
               continuation.resumeWith(result);
            } else {
               Throwable var6;
               if (throwable instanceof CompletionException) {
                  var6 = ((CompletionException)throwable).getCause();
                  if (var6 == null) {
                     var6 = throwable;
                  }
               } else {
                  var6 = throwable;
               }

               Throwable exception = var6;
               Companion var10001 = Result.Companion;
               continuation.resumeWith(Result.constructor-impl(ResultKt.createFailure(exception)));
            }

         }
      });
      Object var10000 = var4.getOrThrow();
      if (var10000 == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
         DebugProbesKt.probeCoroutineSuspended($completion);
      }

      return var10000;
   }
}
