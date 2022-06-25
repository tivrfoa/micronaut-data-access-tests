package io.micronaut.http.bind.binders;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.context.ServerRequestContext;
import kotlin.Metadata;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.CoroutineContext.Element;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.ThreadContextElement;
import kotlinx.coroutines.ThreadContextElement.DefaultImpls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0002\u0018\u0000 \u000f2\u000e\u0012\n\u0012\b\u0012\u0002\b\u0003\u0018\u00010\u00020\u0001:\u0001\u000fB\u0011\u0012\n\u0010\u0003\u001a\u0006\u0012\u0002\b\u00030\u0002¢\u0006\u0002\u0010\u0004J\u001e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0002\b\u0003\u0018\u00010\u0002H\u0016J\u0016\u0010\u000e\u001a\b\u0012\u0002\b\u0003\u0018\u00010\u00022\u0006\u0010\u000b\u001a\u00020\fH\u0016R\u0012\u0010\u0003\u001a\u0006\u0012\u0002\b\u00030\u0002X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00000\u00068VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\b¨\u0006\u0010"},
   d2 = {"Lio/micronaut/http/bind/binders/ServerRequestScopeHandler;", "Lkotlinx/coroutines/ThreadContextElement;", "Lio/micronaut/http/HttpRequest;", "httpRequest", "(Lio/micronaut/http/HttpRequest;)V", "key", "Lkotlin/coroutines/CoroutineContext$Key;", "getKey", "()Lkotlin/coroutines/CoroutineContext$Key;", "restoreThreadContext", "", "context", "Lkotlin/coroutines/CoroutineContext;", "oldState", "updateThreadContext", "Key", "http"}
)
final class ServerRequestScopeHandler implements ThreadContextElement<HttpRequest<?>> {
   @NotNull
   public static final ServerRequestScopeHandler.Key Key = new ServerRequestScopeHandler.Key(null);
   @NotNull
   private final HttpRequest<?> httpRequest;

   public ServerRequestScopeHandler(@NotNull HttpRequest<?> httpRequest) {
      Intrinsics.checkNotNullParameter(httpRequest, "httpRequest");
      super();
      this.httpRequest = httpRequest;
   }

   @NotNull
   public kotlin.coroutines.CoroutineContext.Key<ServerRequestScopeHandler> getKey() {
      return Key;
   }

   @Nullable
   public HttpRequest<?> updateThreadContext(@NotNull CoroutineContext context) {
      Intrinsics.checkNotNullParameter(context, "context");
      HttpRequest previous = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      ServerRequestContext.set(this.httpRequest);
      return previous;
   }

   public void restoreThreadContext(@NotNull CoroutineContext context, @Nullable HttpRequest<?> oldState) {
      Intrinsics.checkNotNullParameter(context, "context");
      ServerRequestContext.set(oldState);
   }

   public <R> R fold(R initial, @NotNull Function2<? super R, ? super Element, ? extends R> operation) {
      return (R)DefaultImpls.fold(this, initial, operation);
   }

   @Nullable
   public <E extends Element> E get(@NotNull kotlin.coroutines.CoroutineContext.Key<E> key) {
      return (E)DefaultImpls.get(this, key);
   }

   @NotNull
   public CoroutineContext minusKey(@NotNull kotlin.coroutines.CoroutineContext.Key<?> key) {
      return DefaultImpls.minusKey(this, key);
   }

   @NotNull
   public CoroutineContext plus(@NotNull CoroutineContext context) {
      return DefaultImpls.plus(this, context);
   }

   @Metadata(
      mv = {1, 6, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0003¨\u0006\u0004"},
      d2 = {"Lio/micronaut/http/bind/binders/ServerRequestScopeHandler$Key;", "Lkotlin/coroutines/CoroutineContext$Key;", "Lio/micronaut/http/bind/binders/ServerRequestScopeHandler;", "()V", "http"}
   )
   public static final class Key implements kotlin.coroutines.CoroutineContext.Key<ServerRequestScopeHandler> {
      private Key() {
      }
   }
}
