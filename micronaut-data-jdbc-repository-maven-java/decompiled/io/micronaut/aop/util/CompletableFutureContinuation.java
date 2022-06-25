package io.micronaut.aop.util;

import io.micronaut.core.annotation.Internal;
import java.util.concurrent.CompletableFuture;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u001a2\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00012\u00020\u0003:\u0001\u001aB\u0015\u0012\u000e\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0001¢\u0006\u0002\u0010\u0005J\n\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J \u0010\u0015\u001a\u00020\u00162\u000e\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0018H\u0016ø\u0001\u0000¢\u0006\u0002\u0010\u0019R\u0016\u0010\u0006\u001a\u0004\u0018\u00010\u00038VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\"\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u0014\u0010\u000f\u001a\u00020\u00108VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0011\u0010\u0012R\u0016\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001b"},
   d2 = {"Lio/micronaut/aop/util/CompletableFutureContinuation;", "Lkotlin/coroutines/Continuation;", "", "Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "continuation", "(Lkotlin/coroutines/Continuation;)V", "callerFrame", "getCallerFrame", "()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "completableFuture", "Ljava/util/concurrent/CompletableFuture;", "getCompletableFuture", "()Ljava/util/concurrent/CompletableFuture;", "setCompletableFuture", "(Ljava/util/concurrent/CompletableFuture;)V", "context", "Lkotlin/coroutines/CoroutineContext;", "getContext", "()Lkotlin/coroutines/CoroutineContext;", "getStackTraceElement", "Ljava/lang/StackTraceElement;", "resumeWith", "", "result", "Lkotlin/Result;", "(Ljava/lang/Object;)V", "Companion", "aop"}
)
public final class CompletableFutureContinuation implements Continuation<Object>, CoroutineStackFrame {
   @NotNull
   public static final CompletableFutureContinuation.Companion Companion = new CompletableFutureContinuation.Companion(null);
   @NotNull
   private final Continuation<Object> continuation;
   @NotNull
   private CompletableFuture<Object> completableFuture;

   public CompletableFutureContinuation(@NotNull Continuation<Object> continuation) {
      Intrinsics.checkNotNullParameter(continuation, "continuation");
      super();
      this.continuation = continuation;
      this.completableFuture = new CompletableFuture();
   }

   @NotNull
   public final CompletableFuture<Object> getCompletableFuture() {
      return this.completableFuture;
   }

   public final void setCompletableFuture(@NotNull CompletableFuture<Object> var1) {
      Intrinsics.checkNotNullParameter(<set-?>, "<set-?>");
      this.completableFuture = <set-?>;
   }

   @Nullable
   public CoroutineStackFrame getCallerFrame() {
      Continuation var1 = this.continuation;
      return var1 instanceof CoroutineStackFrame ? (CoroutineStackFrame)var1 : null;
   }

   @Nullable
   public StackTraceElement getStackTraceElement() {
      return null;
   }

   @NotNull
   public CoroutineContext getContext() {
      return this.continuation.getContext();
   }

   public void resumeWith(@NotNull Object result) {
      if (Result.isSuccess-impl(result)) {
         this.completableFuture.complete(Result.isFailure-impl(result) ? null : result);
      } else {
         this.completableFuture.completeExceptionally(Result.exceptionOrNull-impl(result));
      }

   }

   @Metadata(
      mv = {1, 6, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u001e\u0010\u0003\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00062\u0006\u0010\u0007\u001a\u00020\bJ \u0010\t\u001a\u00020\u00042\u000e\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00062\b\u0010\n\u001a\u0004\u0018\u00010\u0001¨\u0006\u000b"},
      d2 = {"Lio/micronaut/aop/util/CompletableFutureContinuation$Companion;", "", "()V", "completeExceptionally", "", "continuation", "Lkotlin/coroutines/Continuation;", "exception", "", "completeSuccess", "result", "aop"}
   )
   public static final class Companion {
      private Companion() {
      }

      public final void completeSuccess(@NotNull Continuation<Object> continuation, @Nullable Object result) {
         Intrinsics.checkNotNullParameter(continuation, "continuation");
         kotlin.Result.Companion var10001 = Result.Companion;
         continuation.resumeWith(Result.constructor-impl(result));
      }

      public final void completeExceptionally(@NotNull Continuation<Object> continuation, @NotNull Throwable exception) {
         Intrinsics.checkNotNullParameter(continuation, "continuation");
         Intrinsics.checkNotNullParameter(exception, "exception");
         kotlin.Result.Companion var10001 = Result.Companion;
         continuation.resumeWith(Result.constructor-impl(ResultKt.createFailure(exception)));
      }
   }
}
