package io.micronaut.http.bind.binders;

import kotlin.Metadata;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.CoroutineContext.DefaultImpls;
import kotlin.coroutines.CoroutineContext.Element;
import kotlin.coroutines.CoroutineContext.Key;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J5\u0010\b\u001a\u0002H\t\"\u0004\b\u0000\u0010\t2\u0006\u0010\n\u001a\u0002H\t2\u0018\u0010\u000b\u001a\u0014\u0012\u0004\u0012\u0002H\t\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u0002H\t0\fH\u0016¢\u0006\u0002\u0010\u000eJ(\u0010\u000f\u001a\u0004\u0018\u0001H\u0010\"\b\b\u0000\u0010\u0010*\u00020\r2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u0002H\u00100\u0012H\u0096\u0002¢\u0006\u0002\u0010\u0013J\u0014\u0010\u0014\u001a\u00020\u00012\n\u0010\u0011\u001a\u0006\u0012\u0002\b\u00030\u0012H\u0016R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0001X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007¨\u0006\u0015"},
   d2 = {"Lio/micronaut/http/bind/binders/DelegatingCoroutineContext;", "Lkotlin/coroutines/CoroutineContext;", "()V", "delegatingCoroutineContext", "getDelegatingCoroutineContext", "()Lkotlin/coroutines/CoroutineContext;", "setDelegatingCoroutineContext", "(Lkotlin/coroutines/CoroutineContext;)V", "fold", "R", "initial", "operation", "Lkotlin/Function2;", "Lkotlin/coroutines/CoroutineContext$Element;", "(Ljava/lang/Object;Lkotlin/jvm/functions/Function2;)Ljava/lang/Object;", "get", "E", "key", "Lkotlin/coroutines/CoroutineContext$Key;", "(Lkotlin/coroutines/CoroutineContext$Key;)Lkotlin/coroutines/CoroutineContext$Element;", "minusKey", "http"}
)
public final class DelegatingCoroutineContext implements CoroutineContext {
   @Nullable
   private CoroutineContext delegatingCoroutineContext;

   @Nullable
   public final CoroutineContext getDelegatingCoroutineContext() {
      return this.delegatingCoroutineContext;
   }

   public final void setDelegatingCoroutineContext(@Nullable CoroutineContext var1) {
      this.delegatingCoroutineContext = <set-?>;
   }

   public <R> R fold(R initial, @NotNull Function2<? super R, ? super Element, ? extends R> operation) {
      Intrinsics.checkNotNullParameter(operation, "operation");
      CoroutineContext var10000 = this.delegatingCoroutineContext;
      Intrinsics.checkNotNull(this.delegatingCoroutineContext);
      return (R)var10000.fold(initial, operation);
   }

   @Nullable
   public <E extends Element> E get(@NotNull Key<E> key) {
      Intrinsics.checkNotNullParameter(key, "key");
      CoroutineContext var10000 = this.delegatingCoroutineContext;
      Intrinsics.checkNotNull(this.delegatingCoroutineContext);
      return (E)var10000.get(key);
   }

   @NotNull
   public CoroutineContext minusKey(@NotNull Key<?> key) {
      Intrinsics.checkNotNullParameter(key, "key");
      CoroutineContext var10000 = this.delegatingCoroutineContext;
      Intrinsics.checkNotNull(this.delegatingCoroutineContext);
      return var10000.minusKey(key);
   }

   @NotNull
   public CoroutineContext plus(@NotNull CoroutineContext context) {
      return DefaultImpls.plus(this, context);
   }
}
