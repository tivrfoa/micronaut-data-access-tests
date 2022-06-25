package io.micronaut.http.bind.binders;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0002\u0018\u00002\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00012\u00020\u00032\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00050\u0004B\u0005¢\u0006\u0002\u0010\u0006J\u0010\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0005H\u0016J\n\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J \u0010\u0017\u001a\u00020\u00182\u000e\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u001aH\u0016ø\u0001\u0000¢\u0006\u0002\u0010\u001bR\u0016\u0010\u0007\u001a\u0004\u0018\u00010\u0003X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u00020\fX\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000e\"\u0004\b\u0013\u0010\u0010\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001c"},
   d2 = {"Lio/micronaut/http/bind/binders/CustomContinuation;", "Lkotlin/coroutines/Continuation;", "", "Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "Ljava/util/function/Supplier;", "Ljava/util/concurrent/CompletableFuture;", "()V", "callerFrame", "getCallerFrame", "()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "completableFuture", "context", "Lio/micronaut/http/bind/binders/DelegatingCoroutineContext;", "getContext", "()Lio/micronaut/http/bind/binders/DelegatingCoroutineContext;", "setContext", "(Lio/micronaut/http/bind/binders/DelegatingCoroutineContext;)V", "coroutineContext", "getCoroutineContext", "setCoroutineContext", "get", "getStackTraceElement", "Ljava/lang/StackTraceElement;", "resumeWith", "", "result", "Lkotlin/Result;", "(Ljava/lang/Object;)V", "http"}
)
final class CustomContinuation implements Continuation<Object>, CoroutineStackFrame, Supplier<CompletableFuture<?>> {
   @NotNull
   private DelegatingCoroutineContext coroutineContext = new DelegatingCoroutineContext();
   @NotNull
   private final CompletableFuture<Object> completableFuture = new CompletableFuture();
   @NotNull
   private DelegatingCoroutineContext context = this.coroutineContext;
   @Nullable
   private final CoroutineStackFrame callerFrame;

   public CustomContinuation() {
   }

   @NotNull
   public final DelegatingCoroutineContext getCoroutineContext() {
      return this.coroutineContext;
   }

   public final void setCoroutineContext(@NotNull DelegatingCoroutineContext var1) {
      Intrinsics.checkNotNullParameter(<set-?>, "<set-?>");
      this.coroutineContext = <set-?>;
   }

   @NotNull
   public CompletableFuture<Object> get() {
      return this.completableFuture;
   }

   @NotNull
   public DelegatingCoroutineContext getContext() {
      return this.context;
   }

   public void setContext(@NotNull DelegatingCoroutineContext var1) {
      Intrinsics.checkNotNullParameter(<set-?>, "<set-?>");
      this.context = <set-?>;
   }

   public void resumeWith(@NotNull Object result) {
      if (Result.isSuccess-impl(result)) {
         this.completableFuture.complete(Result.isFailure-impl(result) ? null : result);
      } else {
         this.completableFuture.completeExceptionally(Result.exceptionOrNull-impl(result));
      }

   }

   @Nullable
   public CoroutineStackFrame getCallerFrame() {
      return this.callerFrame;
   }

   @Nullable
   public StackTraceElement getStackTraceElement() {
      return null;
   }
}
