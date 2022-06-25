package io.micronaut.aop.util;

import io.micronaut.core.annotation.Internal;
import kotlin.Metadata;
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
   d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00012\u00020\u0003B\u001d\u0012\u000e\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0001\u0012\u0006\u0010\u0005\u001a\u00020\u0006¢\u0006\u0002\u0010\u0007J\n\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0016J \u0010\u0010\u001a\u00020\u00112\u000e\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0013H\u0016ø\u0001\u0000¢\u0006\u0002\u0010\u0014R\u0016\u0010\b\u001a\u0004\u0018\u00010\u00038VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0014\u0010\u000b\u001a\u00020\u00068VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\f\u0010\rR\u0016\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0015"},
   d2 = {"Lio/micronaut/aop/util/DelegatingContextContinuation;", "Lkotlin/coroutines/Continuation;", "", "Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "continuation", "coroutineContext", "Lkotlin/coroutines/CoroutineContext;", "(Lkotlin/coroutines/Continuation;Lkotlin/coroutines/CoroutineContext;)V", "callerFrame", "getCallerFrame", "()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "context", "getContext", "()Lkotlin/coroutines/CoroutineContext;", "getStackTraceElement", "Ljava/lang/StackTraceElement;", "resumeWith", "", "result", "Lkotlin/Result;", "(Ljava/lang/Object;)V", "aop"}
)
public final class DelegatingContextContinuation implements Continuation<Object>, CoroutineStackFrame {
   @NotNull
   private final Continuation<Object> continuation;
   @NotNull
   private final CoroutineContext coroutineContext;

   public DelegatingContextContinuation(@NotNull Continuation<Object> continuation, @NotNull CoroutineContext coroutineContext) {
      Intrinsics.checkNotNullParameter(continuation, "continuation");
      Intrinsics.checkNotNullParameter(coroutineContext, "coroutineContext");
      super();
      this.continuation = continuation;
      this.coroutineContext = coroutineContext;
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
      return this.coroutineContext;
   }

   public void resumeWith(@NotNull Object result) {
      this.continuation.resumeWith(result);
   }
}
